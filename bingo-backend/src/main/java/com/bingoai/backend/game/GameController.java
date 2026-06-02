package com.bingoai.backend.game;

import com.bingoai.backend.common.dto.ApiResponse;
import com.bingoai.backend.game.dto.DrawBallMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    /**
     * Retrieves the list of drawn balls for the active round of the event.
     * Used by player clients to recover state when joining or refreshing.
     */
    @GetMapping("/{eventId}/draws")
    public ResponseEntity<ApiResponse<List<DrawBallMessage>>> getRoundDraws(@PathVariable UUID eventId) {
        try {
            List<DrawBallMessage> draws = gameService.getRoundDraws(eventId);
            return ResponseEntity.ok(ApiResponse.success(draws));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("NOT_FOUND", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("SERVER_ERROR", e.getMessage()));
        }
    }

    /**
     * Updates the stream delay (latency alignment) for the event.
     */
    @PutMapping("/{eventId}/delay")
    public ResponseEntity<ApiResponse<Void>> updateStreamDelay(
            @PathVariable UUID eventId,
            @RequestParam int delay) {
        try {
            gameService.updateStreamDelay(eventId, delay);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("NOT_FOUND", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("SERVER_ERROR", e.getMessage()));
        }
    }
}
