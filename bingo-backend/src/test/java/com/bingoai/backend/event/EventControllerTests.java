package com.bingoai.backend.event;

import com.bingoai.backend.common.config.SecurityConfig;
import com.bingoai.backend.event.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventController.class)
@Import(SecurityConfig.class)
public class EventControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventService eventService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @SuppressWarnings("null")
    public void testCreateEvent_Success() throws Exception {
        CreateEventRequest request = new CreateEventRequest();
        request.setTitle("Campeonato de Bingo");
        request.setScheduledAt(OffsetDateTime.now().plusDays(1));

        CreateRoundRequest roundReq = new CreateRoundRequest();
        roundReq.setRoundNumber(1);

        CreatePrizeRequest prizeReq = new CreatePrizeRequest();
        prizeReq.setName("TV 50 Polegadas");
        prizeReq.setType("FULL_CARD");
        prizeReq.setDrawPattern("FULL_GRID");

        roundReq.setPrizes(Collections.singletonList(prizeReq));
        request.setRounds(Collections.singletonList(roundReq));

        EventResponseDto responseDto = new EventResponseDto(
                UUID.randomUUID(),
                request.getTitle(),
                request.getScheduledAt(),
                OffsetDateTime.now(),
                Collections.singletonList(
                        new RoundResponseDto(
                                UUID.randomUUID(),
                                1,
                                "PENDING",
                                Collections.singletonList(
                                        new PrizeResponseDto(UUID.randomUUID(), "TV 50 Polegadas", "FULL_CARD", "FULL_GRID")
                                )
                        )
                )
        );

        Mockito.when(eventService.createEvent(any(CreateEventRequest.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Campeonato de Bingo"))
                .andExpect(jsonPath("$.data.rounds[0].prizes[0].name").value("TV 50 Polegadas"))
                .andExpect(jsonPath("$.error").isEmpty());
    }

    @Test
    @SuppressWarnings("null")
    public void testCreateEvent_ValidationFailed() throws Exception {
        CreateEventRequest request = new CreateEventRequest();
        // Title is blank, which fails @NotBlank validation constraint

        mockMvc.perform(post("/api/v1/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"));
    }
}
