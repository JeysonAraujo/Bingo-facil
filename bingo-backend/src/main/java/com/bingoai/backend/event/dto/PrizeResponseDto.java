package com.bingoai.backend.event.dto;

import java.util.UUID;

public class PrizeResponseDto {
    private UUID id;
    private String name;
    private com.bingoai.backend.event.PrizeType type;
    private String drawPattern;

    public PrizeResponseDto() {
    }

    public PrizeResponseDto(UUID id, String name, com.bingoai.backend.event.PrizeType type, String drawPattern) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.drawPattern = drawPattern;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public com.bingoai.backend.event.PrizeType getType() {
        return type;
    }

    public void setType(com.bingoai.backend.event.PrizeType type) {
        this.type = type;
    }

    public String getDrawPattern() {
        return drawPattern;
    }

    public void setDrawPattern(String drawPattern) {
        this.drawPattern = drawPattern;
    }
}
