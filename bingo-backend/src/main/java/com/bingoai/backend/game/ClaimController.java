package com.bingoai.backend.game;

import com.bingoai.backend.common.dto.ApiResponse;
import com.bingoai.backend.event.Prize;
import com.bingoai.backend.event.Round;
import com.bingoai.backend.game.dto.BingoClaimMessage;
import com.bingoai.backend.game.dto.ClaimRequestDto;
import com.bingoai.backend.game.dto.ClaimResponseDto;
import com.bingoai.backend.game.dto.DrawBallMessage;
import com.bingoai.backend.game.dto.RoundStatusMessage;
import com.bingoai.backend.ticket.CardMatrix;
import com.bingoai.backend.ticket.Ticket;
import com.bingoai.backend.ticket.TicketRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.lang.NonNull;

@RestController
@RequestMapping("/api/v1/claims")
@Transactional
public class ClaimController {

    private final TicketRepository ticketRepository;
    private final ClaimRepository claimRepository;
    private final GameService gameService;
    private final WinVerificationEngine winVerificationEngine;
    private final SimpMessagingTemplate messagingTemplate;
    private final com.bingoai.backend.audit.AuditLogService auditLogService;

    public ClaimController(TicketRepository ticketRepository,
                           ClaimRepository claimRepository,
                           GameService gameService,
                           WinVerificationEngine winVerificationEngine,
                           SimpMessagingTemplate messagingTemplate,
                           com.bingoai.backend.audit.AuditLogService auditLogService) {
        this.ticketRepository = ticketRepository;
        this.claimRepository = claimRepository;
        this.gameService = gameService;
        this.winVerificationEngine = winVerificationEngine;
        this.messagingTemplate = messagingTemplate;
        this.auditLogService = auditLogService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ClaimResponseDto>> submitClaim(@Valid @RequestBody ClaimRequestDto request) {
        // 1. Fetch ticket and validate
        Ticket ticket = ticketRepository.findById(java.util.Objects.requireNonNull(request.getTicketId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));

        if (ticket.getSessionToken() == null || !ticket.getSessionToken().equals(request.getSessionToken())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid session token");
        }

        // 2. Resolve active round
        UUID eventId = ticket.getEvent().getId();
        Round round = gameService.getOrCreateActiveRound(eventId);

        // 3. Fetch drawn numbers for the round
        List<DrawBallMessage> draws = gameService.getRoundDraws(eventId);
        Set<Integer> marked = draws.stream()
                .map(draw -> draw.getPayload().getBallNumber())
                .collect(Collectors.toSet());

        // Add ticket overrides
        marked.addAll(ticket.getMarkedOverrides());

        // 4. Validate card index bounds
        List<CardMatrix> cards = ticket.getMatrix().getCards();
        if (request.getCardIndex() < 0 || request.getCardIndex() >= cards.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid card index: " + request.getCardIndex());
        }

        // 5. Get active round patterns
        List<String> patterns = round.getPrizes().stream()
                .map(p -> p.getDrawPattern())
                .collect(Collectors.toList());

        // Fallback to FULL_CARD if no patterns are explicitly configured
        if (patterns.isEmpty()) {
            patterns.add("FULL_CARD");
        }

        // 6. Verify win on the card matrix
        CardMatrix cardToVerify = cards.get(request.getCardIndex());
        boolean isWinner = winVerificationEngine.verifyWin(cardToVerify, marked, patterns);

        if (!isWinner) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The card does not satisfy any winning pattern for the active round.");
        }

        // 7. Persist claim
        Claim claim = new Claim(ticket, round, request.getCardIndex());
        Claim saved = claimRepository.save(claim);

        ClaimResponseDto responseDto = new ClaimResponseDto(
                saved.getId(),
                ticket.getId(),
                round.getId(),
                saved.getCardIndex(),
                saved.getStatus(),
                saved.getCreatedAt().toString(),
                ticket.getSerial()
        );

        // 8. Broadcast claim message over WebSockets for admin dashboard in real time
        BingoClaimMessage wsMessage = new BingoClaimMessage(
                ticket.getId().toString(),
                null, // Remove sessionToken
                saved.getCardIndex(),
                saved.getId().toString(),
                saved.getStatus(),
                ticket.getSerial()
        );
        wsMessage.getPayload().setPlayerName(ticket.getPlayerName());
        messagingTemplate.convertAndSend("/topic/events/" + eventId + "/claims", wsMessage);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(responseDto));
    }

    private void validateOwnership(com.bingoai.backend.event.Event event, com.bingoai.backend.admin.Admin admin) {
        if (admin == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Acesso não autorizado.");
        }
        if ("ROLE_SUPER_ADMIN".equals(admin.getRole())) {
            return;
        }
        if (event.getAdmin() == null || !event.getAdmin().getId().equals(admin.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem permissão para gerenciar os claims deste evento.");
        }
    }

    @GetMapping("/round/{roundId}")
    public ResponseEntity<ApiResponse<List<ClaimResponseDto>>> getClaimsByRound(@PathVariable @NonNull UUID roundId) {
        List<Claim> claims = claimRepository.findByRoundId(roundId);
        List<ClaimResponseDto> dtos = claims.stream()
                .map(c -> new ClaimResponseDto(
                        c.getId(),
                        c.getTicket().getId(),
                        c.getRound().getId(),
                        c.getCardIndex(),
                        c.getStatus(),
                        c.getCreatedAt().toString(),
                        c.getTicket().getSerial()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<ClaimResponseDto>> approveClaim(
            @PathVariable @NonNull UUID id,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.bingoai.backend.admin.Admin admin) {
        Claim claim = claimRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Claim not found"));

        validateOwnership(claim.getRound().getEvent(), admin);

        if (com.bingoai.backend.event.EventStatus.FINALIZADO.equals(claim.getRound().getEvent().getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não é possível aprovar claims de um evento finalizado.");
        }

        claim.setStatus("APPROVED");
        Claim saved = claimRepository.save(claim);
        Round round = saved.getRound();
        round.setStatus(com.bingoai.backend.event.RoundStatus.FINISHED);
        
        auditLogService.logAction(admin, "APPROVE_CLAIM", "Claim " + saved.getId() + " approved. Round " + round.getId() + " finished.");

        ClaimResponseDto responseDto = new ClaimResponseDto(
                saved.getId(),
                saved.getTicket().getId(),
                round.getId(),
                saved.getCardIndex(),
                saved.getStatus(),
                saved.getCreatedAt().toString(),
                saved.getTicket().getSerial()
        );

        UUID eventId = round.getEvent().getId();
        BingoClaimMessage wsMessage = new BingoClaimMessage(
                saved.getTicket().getId().toString(),
                null, // Remove sessionToken
                saved.getCardIndex(),
                saved.getId().toString(),
                saved.getStatus(),
                saved.getTicket().getSerial()
        );
        wsMessage.setEventType("BINGO_APPROVED");
        wsMessage.getPayload().setPlayerName(saved.getTicket().getPlayerName());
        messagingTemplate.convertAndSend("/topic/events/" + eventId + "/claims", wsMessage);

        RoundStatusMessage statusMessage = new RoundStatusMessage(round.getId(), round.getRoundNumber() != null ? round.getRoundNumber() : 0, "FINISHED");
        messagingTemplate.convertAndSend("/topic/events/" + eventId + "/rounds", statusMessage);

        return ResponseEntity.ok(ApiResponse.success(responseDto));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<ClaimResponseDto>> rejectClaim(
            @PathVariable @NonNull UUID id,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.bingoai.backend.admin.Admin admin) {
        Claim claim = claimRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Claim not found"));

        validateOwnership(claim.getRound().getEvent(), admin);

        if (com.bingoai.backend.event.EventStatus.FINALIZADO.equals(claim.getRound().getEvent().getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não é possível rejeitar claims de um evento finalizado.");
        }

        claim.setStatus("REJECTED");
        Claim saved = claimRepository.save(claim);
        
        auditLogService.logAction(admin, "REJECT_CLAIM", "Claim " + saved.getId() + " rejected.");

        ClaimResponseDto responseDto = new ClaimResponseDto(
                saved.getId(),
                saved.getTicket().getId(),
                saved.getRound().getId(),
                saved.getCardIndex(),
                saved.getStatus(),
                saved.getCreatedAt().toString(),
                saved.getTicket().getSerial()
        );

        UUID eventId = saved.getRound().getEvent().getId();
        BingoClaimMessage wsMessage = new BingoClaimMessage(
                saved.getTicket().getId().toString(),
                null, // Remove sessionToken
                saved.getCardIndex(),
                saved.getId().toString(),
                saved.getStatus(),
                saved.getTicket().getSerial()
        );
        wsMessage.setEventType("BINGO_REJECTED");
        wsMessage.getPayload().setPlayerName(saved.getTicket().getPlayerName());
        messagingTemplate.convertAndSend("/topic/events/" + eventId + "/claims", wsMessage);

        return ResponseEntity.ok(ApiResponse.success(responseDto));
    }
}
