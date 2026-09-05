package com.bingoai.backend.game;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketEventListener {

    private final SimpMessagingTemplate messagingTemplate;
    // Map sessionId -> subscribed eventId
    private final Map<String, String> sessionToEventMap = new ConcurrentHashMap<>();
    // Map eventId -> count of active players
    private final Map<String, Integer> eventViewerCountMap = new ConcurrentHashMap<>();

    public WebSocketEventListener(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void handleSessionSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = headerAccessor.getDestination();
        String sessionId = headerAccessor.getSessionId();

        if (destination != null && destination.startsWith("/topic/events/") && destination.endsWith("/draws")) {
            // Extract eventId: /topic/events/{eventId}/draws
            String[] parts = destination.split("/");
            if (parts.length >= 4) {
                String eventId = parts[3];
                sessionToEventMap.put(sessionId, eventId);
                eventViewerCountMap.merge(eventId, 1, (a, b) -> a + b);
                broadcastViewerCount(eventId);
            }
        }
    }

    @EventListener
    public void handleSessionUnsubscribe(SessionUnsubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        removeSession(sessionId);
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        removeSession(sessionId);
    }

    private void removeSession(String sessionId) {
        String eventId = sessionToEventMap.remove(sessionId);
        if (eventId != null) {
            eventViewerCountMap.computeIfPresent(eventId, (key, val) -> {
                int newVal = val - 1;
                return newVal > 0 ? newVal : null; // Remove key if 0
            });
            broadcastViewerCount(eventId);
        }
    }

    private void broadcastViewerCount(String eventId) {
        int count = eventViewerCountMap.getOrDefault(eventId, 0);
        messagingTemplate.convertAndSend("/topic/events/" + eventId + "/viewers", java.util.Objects.requireNonNull(Map.of("viewerCount", count)));
    }
}
