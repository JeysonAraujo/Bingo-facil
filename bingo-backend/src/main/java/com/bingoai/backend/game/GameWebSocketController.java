package com.bingoai.backend.game;

import com.bingoai.backend.game.dto.BingoClaimMessage;
import com.bingoai.backend.game.dto.DrawBallMessage;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * WebSocket STOMP controller for real-time game messaging.
 *
 * <p>IMPORTANT: Must be annotated with {@code @Controller} (NOT {@code @RestController}).
 * STOMP message handlers use Spring's messaging abstraction, not HTTP MVC.
 *
 * <p>Topic layout:
 * <ul>
 *   <li>Inbound:  {@code /app/events/{eventId}/draw}   → Admin draws a ball</li>
 *   <li>Outbound: {@code /topic/events/{eventId}/draws} → Broadcast to all subscribers</li>
 *   <li>Inbound:  {@code /app/events/{eventId}/claim}  → Player claims BINGO (Story 5.x)</li>
 * </ul>
 *
 * <p>Virtual threads (enabled in application.yml) ensure all 500+ concurrent WS sessions
 * are handled without exhausting the platform thread pool.
 */
@Controller
public class GameWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    public GameWebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Receives a draw command from the Admin panel and broadcasts the drawn ball
     * to all players subscribed to the event's draw topic.
     *
     * <p>Admin sends to: {@code /app/events/{eventId}/draw}
     * <p>Players receive at: {@code /topic/events/{eventId}/draws}
     *
     * @param eventId the UUID of the active event
     * @param message the draw request containing ballNumber and drawnSequence
     */
    @MessageMapping("/events/{eventId}/draw")
    public void drawBall(
            @DestinationVariable String eventId,
            @Payload DrawBallMessage message) {

        // Enrich timestamp on the server side for consistency across clients
        DrawBallMessage broadcast = new DrawBallMessage(
                message.getPayload().getBallNumber(),
                message.getPayload().getDrawnSequence()
        );

        messagingTemplate.convertAndSend(
                "/topic/events/" + eventId + "/draws",
                broadcast
        );
    }

    /**
     * Receives a BINGO claim from a player.
     * Full server-side verification is implemented in Story 5.x (WinVerificationEngine).
     * This stub acknowledges receipt and will be expanded in Epic 5.
     *
     * <p>Player sends to:   {@code /app/events/{eventId}/claim}
     * <p>Admin receives at: {@code /topic/events/{eventId}/claims} (Story 5.x)
     *
     * @param eventId the UUID of the active event
     * @param claim   the claim containing ticketId and sessionToken
     */
    @MessageMapping("/events/{eventId}/claim")
    public void handleClaim(
            @DestinationVariable String eventId,
            @Payload BingoClaimMessage claim) {
        // TODO: Story 5.x will wire WinVerificationEngine here and broadcast to /topic/events/{eventId}/claims
        // Stub is intentionally left minimal to avoid premature implementation
    }
}
