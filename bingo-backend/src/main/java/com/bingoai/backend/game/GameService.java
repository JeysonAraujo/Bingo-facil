package com.bingoai.backend.game;

import com.bingoai.backend.event.Event;
import com.bingoai.backend.event.EventRepository;
import com.bingoai.backend.event.Round;
import com.bingoai.backend.event.RoundRepository;
import com.bingoai.backend.game.dto.DrawBallMessage;
import com.bingoai.backend.game.dto.StreamDelayMessage;
import com.bingoai.backend.ticket.TicketRepository;
import com.bingoai.backend.game.WinVerificationEngine;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class GameService {

    private final EventRepository eventRepository;
    private final RoundRepository roundRepository;
    private final DrawRepository drawRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final TicketRepository ticketRepository;
    private final ClaimRepository claimRepository;
    private final WinVerificationEngine winVerificationEngine;

    public GameService(EventRepository eventRepository,
                       RoundRepository roundRepository,
                       DrawRepository drawRepository,
                       SimpMessagingTemplate messagingTemplate,
                       TicketRepository ticketRepository,
                       ClaimRepository claimRepository,
                       WinVerificationEngine winVerificationEngine) {
        this.eventRepository = eventRepository;
        this.roundRepository = roundRepository;
        this.drawRepository = drawRepository;
        this.messagingTemplate = messagingTemplate;
        this.ticketRepository = ticketRepository;
        this.claimRepository = claimRepository;
        this.winVerificationEngine = winVerificationEngine;
    }

    /**
     * Resolves the active round for the given event ID.
     * If a round is in IN_PROGRESS, returns it.
     * Otherwise, finds the first round, sets its status to IN_PROGRESS, and returns it.
     */
    public Round getOrCreateActiveRound(UUID eventId) {
        // Ensure event exists
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with ID: " + eventId));

        return roundRepository.findFirstByEventIdAndStatus(eventId, "IN_PROGRESS")
                .orElseGet(() -> {
                    Round r = roundRepository.findFirstByEventIdOrderByRoundNumberAsc(eventId)
                            .orElseThrow(() -> new IllegalArgumentException("No rounds defined for event: " + eventId));
                    if ("PENDING".equals(r.getStatus())) {
                        r.setStatus("IN_PROGRESS");
                        roundRepository.save(r);
                    }
                    return r;
                });
    }

    /**
     * Retrieves the chronological sorted list of drawn balls for the active round.
     */
    @Transactional(readOnly = true)
    public List<DrawBallMessage> getRoundDraws(UUID eventId) {
        Round round = getOrCreateActiveRound(eventId);
        List<Draw> draws = drawRepository.findByRoundIdOrderByDrawnSequenceAsc(round.getId());

        return draws.stream()
                .map(draw -> new DrawBallMessage(draw.getBallNumber(), draw.getDrawnSequence(), draw.getCreatedAt().toInstant()))
                .collect(Collectors.toList());
    }

    /**
     * Records a new ball draw in the database for the active round.
     */
    public DrawBallMessage recordDraw(UUID eventId, int ballNumber, int sequence) {
        Round round = getOrCreateActiveRound(eventId);

        if (drawRepository.existsByRoundIdAndBallNumber(round.getId(), ballNumber)) {
            throw new IllegalStateException("Ball " + ballNumber + " has already been drawn in this round");
        }

        Draw draw = new Draw(round, ballNumber, sequence);
        Draw saved = drawRepository.save(draw);

        try {
            checkAndGenerateAutoClaims(eventId, round);
        } catch (Exception e) {
            System.err.println("[AutoClaim] Error running auto claim verification: " + e.getMessage());
            e.printStackTrace();
        }

        return new DrawBallMessage(saved.getBallNumber(), saved.getDrawnSequence(), saved.getCreatedAt().toInstant());
    }

    private void checkAndGenerateAutoClaims(UUID eventId, Round round) {
        List<DrawBallMessage> draws = getRoundDraws(eventId);
        java.util.Set<Integer> marked = draws.stream()
                .map(draw -> draw.getPayload().getBallNumber())
                .collect(Collectors.toSet());

        List<String> patterns = round.getPrizes().stream()
                .map(com.bingoai.backend.event.Prize::getDrawPattern)
                .collect(Collectors.toList());
        if (patterns.isEmpty()) {
            patterns.add("FULL_CARD");
        }

        List<com.bingoai.backend.ticket.Ticket> activeTickets = ticketRepository.findByEventIdAndActivatedTrue(eventId);

        int naBoaCount = 0;

        for (com.bingoai.backend.ticket.Ticket ticket : activeTickets) {
            java.util.Set<Integer> ticketMarked = new java.util.HashSet<>(marked);
            if (ticket.getMarkedOverrides() != null) {
                ticketMarked.addAll(ticket.getMarkedOverrides());
            }

            boolean isTicketNaBoa = false;

            List<com.bingoai.backend.ticket.CardMatrix> cards = ticket.getMatrix().getCards();
            for (int cardIndex = 0; cardIndex < cards.size(); cardIndex++) {
                boolean claimExists = claimRepository.existsByRoundIdAndTicketIdAndCardIndex(round.getId(), ticket.getId(), cardIndex);
                if (!claimExists) {
                    com.bingoai.backend.ticket.CardMatrix card = cards.get(cardIndex);
                    boolean isWinner = winVerificationEngine.verifyWin(card, ticketMarked, patterns);
                    if (isWinner) {
                        Claim claim = new Claim(ticket, round, cardIndex, true);
                        Claim savedClaim = claimRepository.save(claim);

                        com.bingoai.backend.game.dto.BingoClaimMessage claimMessage = new com.bingoai.backend.game.dto.BingoClaimMessage(
                                ticket.getId().toString(),
                                ticket.getSessionToken() != null ? ticket.getSessionToken() : "",
                                cardIndex,
                                savedClaim.getId().toString(),
                                savedClaim.getStatus(),
                                ticket.getSerial(),
                                true
                        );
                        messagingTemplate.convertAndSend(
                                "/topic/events/" + eventId + "/claims",
                                claimMessage
                        );
                    } else if (!isTicketNaBoa) {
                        int distance = winVerificationEngine.getDistanceToWin(card, ticketMarked, patterns);
                        if (distance == 1) {
                            isTicketNaBoa = true;
                        }
                    }
                }
            }
            if (isTicketNaBoa) {
                naBoaCount++;
            }
        }

        com.bingoai.backend.game.dto.NaBoaMessage naBoaMessage = new com.bingoai.backend.game.dto.NaBoaMessage(naBoaCount);
        messagingTemplate.convertAndSend(
                "/topic/events/" + eventId + "/na-boa",
                naBoaMessage
        );
    }

    /**
     * Deletes the last drawn ball (with the highest sequence number) from the database
     * for the active round, returning its representation for visual retraction broadcast.
     */
    public DrawBallMessage retractLastDraw(UUID eventId) {
        Round round = getOrCreateActiveRound(eventId);

        Draw lastDraw = drawRepository.findTopByRoundIdOrderByDrawnSequenceDesc(round.getId())
                .orElseThrow(() -> new IllegalStateException("No draws found to retract in active round"));

        drawRepository.delete(lastDraw);

        return new DrawBallMessage(lastDraw.getBallNumber(), lastDraw.getDrawnSequence(), lastDraw.getCreatedAt().toInstant());
    }

    /**
     * Updates the stream delay for the event and broadcasts it over WebSockets.
     */
    public void updateStreamDelay(UUID eventId, int delay) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with ID: " + eventId));
        event.setStreamDelaySeconds(delay);
        eventRepository.save(event);

        StreamDelayMessage message = new StreamDelayMessage(delay);
        messagingTemplate.convertAndSend(
                "/topic/events/" + eventId + "/delay",
                message
        );
    }

    @Transactional(readOnly = true)
    public boolean checkEventAdmin(UUID eventId, com.bingoai.backend.admin.Admin admin) {
        if (admin == null) {
            return false;
        }
        if ("ROLE_SUPER_ADMIN".equals(admin.getRole())) {
            return true;
        }
        Event event = eventRepository.findById(eventId).orElse(null);
        return event != null && event.getAdmin() != null && event.getAdmin().getId().equals(admin.getId());
    }

    @Transactional(readOnly = true)
    public boolean checkTicketAdmin(UUID ticketId, com.bingoai.backend.admin.Admin admin) {
        if (admin == null) {
            return false;
        }
        if ("ROLE_SUPER_ADMIN".equals(admin.getRole())) {
            return true;
        }
        com.bingoai.backend.ticket.Ticket ticket = ticketRepository.findById(ticketId).orElse(null);
        if (ticket == null) {
            return false;
        }
        Event event = ticket.getEvent();
        return event != null && event.getAdmin() != null && event.getAdmin().getId().equals(admin.getId());
    }
}
