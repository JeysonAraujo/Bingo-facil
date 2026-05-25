package com.bingoai.backend.event.dto;

import jakarta.validation.constraints.NotBlank;

public class CreatePrizeRequest {

    @NotBlank(message = "Prize name cannot be blank")
    private String name;

    @NotBlank(message = "Prize type cannot be blank")
    private String type;

    @NotBlank(message = "Prize draw pattern cannot be blank")
    private String drawPattern;

    public CreatePrizeRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDrawPattern() {
        return drawPattern;
    }

    public void setDrawPattern(String drawPattern) {
        this.drawPattern = drawPattern;
    }
}
