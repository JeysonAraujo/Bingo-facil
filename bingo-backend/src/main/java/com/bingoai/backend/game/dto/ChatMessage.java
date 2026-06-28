package com.bingoai.backend.game.dto;

import java.time.Instant;

public class ChatMessage {
    private String eventType = "CHAT_MESSAGE";
    private String sender;
    private String text;
    private String timestamp = Instant.now().toString();

    public ChatMessage() {
    }

    public ChatMessage(String sender, String text) {
        this.sender = sender;
        this.text = text;
    }

    public String getEventType() { 
        return eventType; 
    }
    
    public void setEventType(String eventType) { 
        this.eventType = eventType; 
    }
    
    public String getSender() { 
        return sender; 
    }
    
    public void setSender(String sender) { 
        this.sender = sender; 
    }
    
    public String getText() { 
        return text; 
    }
    
    public void setText(String text) { 
        this.text = text; 
    }
    
    public String getTimestamp() { 
        return timestamp; 
    }
    
    public void setTimestamp(String timestamp) { 
        this.timestamp = timestamp; 
    }
}
