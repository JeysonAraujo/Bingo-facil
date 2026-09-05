package com.bingoai.backend.ticket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.ArrayList;
import java.util.List;

@Converter
public class TicketOverridesConverter implements AttributeConverter<List<Integer>, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<Integer> attribute) {
        try {
            return objectMapper.writeValueAsString(attribute != null ? attribute : new ArrayList<>());
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to serialize marked overrides list to JSON string", e);
        }
    }

    @Override
    public List<Integer> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            com.fasterxml.jackson.databind.JsonNode node = objectMapper.readTree(dbData);
            String json = node.isTextual() ? node.asText() : dbData;
            return objectMapper.readValue(json, new TypeReference<List<Integer>>() {});
        } catch (Exception e) {
            System.err.println("Error parsing dbData in TicketOverridesConverter: " + dbData);
            return new ArrayList<>();
        }
    }
}
