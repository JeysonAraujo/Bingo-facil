package com.bingoai.backend.game;

import com.bingoai.backend.event.Event;
import com.bingoai.backend.event.EventRepository;
import com.bingoai.backend.event.Round;
import com.bingoai.backend.event.RoundRepository;
import com.bingoai.backend.game.dto.DrawBallMessage;
import com.bingoai.backend.game.dto.StreamDelayMessage;
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

    public GameService(EventRepository eventRepository,
                       RoundRepository roundRepository,
                       DrawRepository drawRepository,
                       SimpMessagingTemplate messagingTemplate) {
        this.eventRepository = eventRepository;
        this.roundRepository = roundRepository;
        this.drawRepository = drawRepository;
        this.messagingTemplate = messagingTemplate;
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

        return new DrawBallMessage(saved.getBallNumber(), saved.getDrawnSequence(), saved.getCreatedAt().toInstant());
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
}
