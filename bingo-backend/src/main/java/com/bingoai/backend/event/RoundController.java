package com.bingoai.backend.event;

import com.bingoai.backend.common.dto.ApiResponse;
import com.bingoai.backend.game.DrawRepository;
import com.bingoai.backend.game.GameWebSocketController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/rounds")
public class RoundController {

    private final RoundRepository roundRepository;
    private final DrawRepository drawRepository;
    private final GameWebSocketController gameWebSocketController;

    public RoundController(RoundRepository roundRepository,
                           DrawRepository drawRepository,
                           GameWebSocketController gameWebSocketController) {
        this.roundRepository = roundRepository;
        this.drawRepository = drawRepository;
        this.gameWebSocketController = gameWebSocketController;
    }

    @PostMapping("/{roundId}/start")
    @Transactional
    public ResponseEntity<ApiResponse<String>> startRound(@PathVariable UUID roundId) {
        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Round not found"));

        Event event = round.getEvent();
        UUID eventId = event.getId();

        // 1. Inverte status de outras rodadas ativas do evento para FINISHED
        List<Round> siblingRounds = roundRepository.findByEventIdOrderByRoundNumberAsc(eventId);
        for (Round sibling : siblingRounds) {
            if (!sibling.getId().equals(roundId) && "IN_PROGRESS".equals(sibling.getStatus())) {
                sibling.setStatus("FINISHED");
                roundRepository.save(sibling);
                gameWebSocketController.broadcastRoundStatus(eventId.toString(), sibling.getId(), sibling.getRoundNumber() != null ? sibling.getRoundNumber() : 0, "FINISHED");
            }
        }

        // 2. Define o status da rodada atual como IN_PROGRESS
        round.setStatus("IN_PROGRESS");
        roundRepository.save(round);

        // 3. Zera os draws anteriores para esta rodada
        drawRepository.deleteByRoundId(roundId);

        // 4. Notifica via WebSocket
        gameWebSocketController.broadcastRoundStatus(eventId.toString(), round.getId(), round.getRoundNumber() != null ? round.getRoundNumber() : 0, "IN_PROGRESS");

        return ResponseEntity.ok(ApiResponse.success("Round started successfully"));
    }

    @PostMapping("/{roundId}/finish")
    @Transactional
    public ResponseEntity<ApiResponse<String>> finishRound(@PathVariable UUID roundId) {
        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Round not found"));

        round.setStatus("FINISHED");
        roundRepository.save(round);

        gameWebSocketController.broadcastRoundStatus(round.getEvent().getId().toString(), round.getId(), round.getRoundNumber() != null ? round.getRoundNumber() : 0, "FINISHED");

        return ResponseEntity.ok(ApiResponse.success("Round finished successfully"));
    }

    @PostMapping("/{roundId}/cancel")
    @Transactional
    public ResponseEntity<ApiResponse<String>> cancelRound(@PathVariable UUID roundId) {
        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Round not found"));

        round.setStatus("PENDING");
        roundRepository.save(round);

        // Clear draws
        drawRepository.deleteByRoundId(roundId);

        gameWebSocketController.broadcastRoundStatus(round.getEvent().getId().toString(), round.getId(), round.getRoundNumber() != null ? round.getRoundNumber() : 0, "PENDING");

        return ResponseEntity.ok(ApiResponse.success("Round cancelled successfully"));
    }

    @DeleteMapping("/{roundId}")
    @Transactional
    public ResponseEntity<ApiResponse<String>> deleteRound(@PathVariable UUID roundId) {
        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Round not found"));

        roundRepository.delete(round);

        return ResponseEntity.ok(ApiResponse.success("Round deleted successfully"));
    }
}
