package com.bingoai.backend.game;

import com.bingoai.backend.game.dto.AnnouncementMessage;
import com.bingoai.backend.game.dto.BingoClaimMessage;
import com.bingoai.backend.game.dto.DrawBallCommand;
import com.bingoai.backend.game.dto.DrawBallMessage;
import com.bingoai.backend.game.dto.TicketOverrideCommand;
import com.bingoai.backend.game.dto.TicketOverrideMessage;
import com.bingoai.backend.game.dto.StreamDelayMessage;
import jakarta.validation.Valid;
import java.util.UUID;
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
    private final GameService gameService;
    private final AdminOverrideService adminOverrideService;

    public GameWebSocketController(SimpMessagingTemplate messagingTemplate,
                                   GameService gameService,
                                   AdminOverrideService adminOverrideService) {
        this.messagingTemplate = messagingTemplate;
        this.gameService = gameService;
        this.adminOverrideService = adminOverrideService;
    }

    /**
     * Receives a draw command from the Admin panel, persists it in the database,
     * and broadcasts the drawn ball to all players subscribed to the event's draw topic.
     *
     * <p>Admin sends to: {@code /app/events/{eventId}/draw}
     * <p>Players receive at: {@code /topic/events/{eventId}/draws}
     *
     * @param eventId the UUID of the active event
     * @param command the draw command containing ballNumber and drawnSequence
     */
    @MessageMapping("/events/{eventId}/draw")
    public void drawBall(
            @DestinationVariable String eventId,
            @Payload @Valid DrawBallCommand command) {

        // Persist the drawn ball in the database for state recovery
        DrawBallMessage broadcast = gameService.recordDraw(
                UUID.fromString(eventId),
                command.getBallNumber(),
                command.getDrawnSequence()
        );

        messagingTemplate.convertAndSend(
                "/topic/events/" + eventId + "/draws",
                broadcast
        );
    }

    /**
     * Receives an undo command from the Admin panel, deletes the last drawn ball from
     * the database, and broadcasts a BALL_RETRACTED event to all connected players.
     *
     * <p>Admin sends to: {@code /app/events/{eventId}/undo}
     * <p>Players receive at: {@code /topic/events/{eventId}/draws}
     *
     * @param eventId the UUID of the active event
     */
    @MessageMapping("/events/{eventId}/undo")
    public void undoLastDraw(@DestinationVariable String eventId) {
        try {
            // Delete last draw and fetch its metadata
            DrawBallMessage retracted = gameService.retractLastDraw(UUID.fromString(eventId));
            // Override eventType to signal a retraction
            retracted.setEventType("BALL_RETRACTED");

            messagingTemplate.convertAndSend(
                    "/topic/events/" + eventId + "/draws",
                    retracted
            );
        } catch (Exception e) {
            // Fail silently or broadcast error if needed (keep robust for production)
            System.err.println("[WebSocket] Failed to retract last draw: " + e.getMessage());
        }
    }

    /**
     * Receives an announcement request from the Admin, enriches it inside the official
     * envelope, and broadcasts it over WebSockets to all players in the room.
     *
     * <p>Admin sends to:   {@code /app/events/{eventId}/announcement}
     * <p>Players receive at: {@code /topic/events/{eventId}/announcements}
     *
     * @param eventId      the UUID of the active event
     * @param announcement the payload containing the announcement text
     */
    @MessageMapping("/events/{eventId}/announcement")
    public void broadcastAnnouncement(
            @DestinationVariable String eventId,
            @Payload AnnouncementMessage.Payload announcement) {

        AnnouncementMessage broadcast = new AnnouncementMessage(announcement.getText());

        messagingTemplate.convertAndSend(
                "/topic/events/" + eventId + "/announcements",
                broadcast
        );
    }

    /**
     * Receives a forced ticket cell override request from the Admin, persists the
     * override in the database, and dispatches a targeted WebSocket update solely
     * to that specific player's overrides channel.
     *
     * <p>Admin sends to:   {@code /app/tickets/{ticketId}/override}
     * <p>Player receives at: {@code /topic/tickets/{ticketId}/overrides}
     *
     * @param ticketId the UUID of the player ticket being overridden
     * @param command  the payload containing the ball number and action (MARK/UNMARK)
     */
    @MessageMapping("/tickets/{ticketId}/override")
    public void applyTicketOverride(
            @DestinationVariable String ticketId,
            @Payload @Valid TicketOverrideCommand command) {

        try {
            // 1. Persist the forced override in the database
            adminOverrideService.applyOverride(
                    UUID.fromString(ticketId),
                    command.getBallNumber(),
                    command.getAction()
            );

            // 2. Dispatch targeted real-time message to that specific player client only
            TicketOverrideMessage message = new TicketOverrideMessage(
                    UUID.fromString(ticketId),
                    command.getBallNumber(),
                    command.getAction()
            );

            messagingTemplate.convertAndSend(
                    "/topic/tickets/" + ticketId + "/overrides",
                    message
            );
        } catch (Exception e) {
            System.err.println("[WebSocket] Failed to process ticket override: " + e.getMessage());
        }
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

    /**
     * Broadcasts the stream delay update to all subscribers on the event's delay topic.
     */
    public void broadcastStreamDelay(String eventId, int delaySeconds) {
        StreamDelayMessage message = new StreamDelayMessage(delaySeconds);
        messagingTemplate.convertAndSend(
                "/topic/events/" + eventId + "/delay",
                message
        );
    }
}
