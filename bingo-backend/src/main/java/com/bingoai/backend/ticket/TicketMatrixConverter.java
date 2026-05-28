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
            com.fasterxml.jackson.databind.JsonNode node = objectMapper.readTree(dbData);
            if (node.isTextual()) {
                return objectMapper.readValue(node.asText(), TicketMatrix.class);
            }
            return objectMapper.treeToValue(node, TicketMatrix.class);
        } catch (Exception e) {
            System.err.println("Error parsing dbData: " + dbData);
            return null;
        }
    }
}
