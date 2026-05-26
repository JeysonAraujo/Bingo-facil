package com.bingoai.backend.ticket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TicketMatrixConverter implements AttributeConverter<TicketMatrix, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(TicketMatrix attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error converting TicketMatrix to JSON", e);
        }
    }

    @Override
    public TicketMatrix convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(dbData, TicketMatrix.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error converting JSON to TicketMatrix", e);
        }
    }
}
