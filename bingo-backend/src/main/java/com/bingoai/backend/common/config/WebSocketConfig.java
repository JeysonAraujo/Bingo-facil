package com.bingoai.backend.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
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

    @Value("${app.activation-base-url:http://localhost:3000}")
    private String allowedOrigin;

    private final com.bingoai.backend.admin.AdminSessionRepository adminSessionRepository;

    public WebSocketConfig(com.bingoai.backend.admin.AdminSessionRepository adminSessionRepository) {
        this.adminSessionRepository = adminSessionRepository;
    }

    @Override
    public void configureMessageBroker(@NonNull MessageBrokerRegistry config) {
        // Enable simple in-memory broker to route messages to /topic destinations
        config.enableSimpleBroker("/topic");
        // Prefix for messages routed to @MessageMapping controller methods
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {
        // Native WebSocket endpoint
        registry.addEndpoint("/ws/bingo")
                .setAllowedOriginPatterns("*");
        // SockJS fallback endpoint (also enables /ws/bingo/websocket for native WS transport)
        registry.addEndpoint("/ws/bingo")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(@NonNull org.springframework.messaging.simp.config.ChannelRegistration registration) {
        registration.interceptors(new org.springframework.messaging.support.ChannelInterceptor() {
            @Override
            public org.springframework.messaging.Message<?> preSend(
                    @NonNull org.springframework.messaging.Message<?> message,
                    @NonNull org.springframework.messaging.MessageChannel channel) {
                
                try {
                    org.springframework.messaging.simp.stomp.StompHeaderAccessor accessor =
                            org.springframework.messaging.support.MessageHeaderAccessor.getAccessor(
                                    message, org.springframework.messaging.simp.stomp.StompHeaderAccessor.class);
                    
                    if (accessor != null && org.springframework.messaging.simp.stomp.StompCommand.CONNECT.equals(accessor.getCommand())) {
                        String authHeader = accessor.getFirstNativeHeader("Authorization");
                        if (authHeader != null && authHeader.startsWith("Bearer ")) {
                            String token = authHeader.substring(7);
                            java.util.Optional<com.bingoai.backend.admin.AdminSession> sessionOpt =
                                    adminSessionRepository.findByToken(token);
                            
                            if (sessionOpt.isPresent()) {
                                com.bingoai.backend.admin.AdminSession session = sessionOpt.get();
                                if (session.getExpiresAt().isAfter(java.time.OffsetDateTime.now())) {
                                    com.bingoai.backend.admin.Admin admin = session.getAdmin();
                                    org.springframework.security.authentication.UsernamePasswordAuthenticationToken auth =
                                            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                                                    admin,
                                                    null,
                                                    java.util.Collections.singletonList(
                                                            new org.springframework.security.core.authority.SimpleGrantedAuthority(admin.getRole())
                                                    )
                                            );
                                    accessor.setUser(auth);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    // Suppress transient DB/auth errors during channel intercepting to prevent dropping connection
                }
                return message;
            }
        });
    }
}
