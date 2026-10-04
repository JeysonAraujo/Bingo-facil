package com.bingoai.backend.game;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;

/**
 * Service responsible for broadcasting WebSocket messages across multiple Spring Boot instances
 * using Redis Pub/Sub backplane. Ensures real-time STOMP state synchronization when Cloud Run
 * scales horizontally to >1 container instance.
 */
@Service
public class RedisPubSubService implements MessageListener {

    private static final Logger log = LoggerFactory.getLogger(RedisPubSubService.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ChannelTopic websocketTopic;
    private final ObjectMapper objectMapper;
    private final RedisMessageListenerContainer container;

    @Value("${app.redis.enabled:false}")
    private boolean redisEnabled;

    public RedisPubSubService(SimpMessagingTemplate messagingTemplate,
                              RedisTemplate<String, Object> redisTemplate,
                              ChannelTopic websocketTopic,
                              ObjectMapper objectMapper,
                              RedisMessageListenerContainer container) {
        this.messagingTemplate = messagingTemplate;
        this.redisTemplate = redisTemplate;
        this.websocketTopic = websocketTopic;
        this.objectMapper = objectMapper;
        this.container = container;
    }

    @PostConstruct
    public void init() {
        if (redisEnabled) {
            try {
                container.addMessageListener(this, websocketTopic);
                log.info("Redis Pub/Sub WebSocket Message Listener initialized on topic: {}", websocketTopic.getTopic());
            } catch (Exception e) {
                log.warn("Failed to subscribe Redis MessageListener (falling back to local WS broker): {}", e.getMessage());
                redisEnabled = false;
            }
        } else {
            log.info("Redis Pub/Sub is disabled. WebSocket broadcasts will run in local single-instance mode.");
        }
    }

    /**
     * Publishes a message to Redis Pub/Sub for multi-instance dispatching, or dispatches directly
     * to local WebSockets if Redis is disabled.
     */
    public void publishOrBroadcast(String destination, Object payload) {
        if (redisEnabled) {
            try {
                WebSocketMessageEnvelope envelope = new WebSocketMessageEnvelope(destination, objectMapper.writeValueAsString(payload));
                String messageJson = objectMapper.writeValueAsString(envelope);
                redisTemplate.convertAndSend(websocketTopic.getTopic(), messageJson);
                return;
            } catch (Exception e) {
                log.error("Failed to publish WebSocket message to Redis. Falling back to local broadcast: {}", e.getMessage());
            }
        }
        
        // Fallback or Single-Instance mode
        messagingTemplate.convertAndSend(destination, payload);
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String jsonContent = new String(message.getBody(), StandardCharsets.UTF_8);
            WebSocketMessageEnvelope envelope = objectMapper.readValue(jsonContent, WebSocketMessageEnvelope.class);
            
            if (envelope != null && envelope.getDestination() != null && envelope.getPayloadJson() != null) {
                Object parsedPayload = objectMapper.readValue(envelope.getPayloadJson(), Object.class);
                messagingTemplate.convertAndSend(envelope.getDestination(), parsedPayload);
            }
        } catch (Exception e) {
            log.error("Failed to parse and relay Redis Pub/Sub WebSocket message: {}", e.getMessage());
        }
    }

    public static class WebSocketMessageEnvelope {
        private String destination;
        private String payloadJson;

        public WebSocketMessageEnvelope() {}

        public WebSocketMessageEnvelope(String destination, String payloadJson) {
            this.destination = destination;
            this.payloadJson = payloadJson;
        }

        public String getDestination() { return destination; }
        public void setDestination(String destination) { this.destination = destination; }

        public String getPayloadJson() { return payloadJson; }
        public void setPayloadJson(String payloadJson) { this.payloadJson = payloadJson; }
    }
}
