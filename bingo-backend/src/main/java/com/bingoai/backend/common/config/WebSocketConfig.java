package com.bingoai.backend.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket STOMP configuration.
 *
 * <p>Registers the /ws/bingo endpoint with SockJS fallback for clients that do not
 * support native WebSocket (e.g., older browsers, certain proxies). The in-memory
 * simple broker is used for MVP; a full broker relay (RabbitMQ/ActiveMQ) can be
 * swapped in for production scale.
 *
 * <p>Topic layout (from architecture.md):
 * <ul>
 *   <li>{@code /topic/events/{eventId}/draws}  — broadcast of newly drawn numbers</li>
 *   <li>{@code /topic/events/{eventId}/claims} — broadcast of verified BINGOs (Story 5.x)</li>
 *   <li>{@code /app/events/{eventId}/claim}    — inbound win submissions (Story 5.x)</li>
 * </ul>
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple in-memory broker to route messages to /topic destinations
        config.enableSimpleBroker("/topic");
        // Prefix for messages routed to @MessageMapping controller methods
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Primary WebSocket endpoint with SockJS fallback for broad client compatibility
        registry.addEndpoint("/ws/bingo")
                .setAllowedOrigins("http://localhost:3000")
                .withSockJS();
    }
}
