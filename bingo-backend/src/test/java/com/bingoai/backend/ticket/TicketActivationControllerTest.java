package com.bingoai.backend.ticket;

import com.bingoai.backend.event.Event;
import com.bingoai.backend.event.EventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
class TicketActivationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private EventRepository eventRepository;

    private String validSerial;
    private String activatedSerial;

    @BeforeEach
    void setUp() {
        // Create a test event
        Event event = new Event();
        event.setTitle("Test Event");
        event.setScheduledAt(OffsetDateTime.now().plusDays(7));
        eventRepository.save(event);

        // Create a valid unactivated ticket
        Ticket ticket = new Ticket();
        ticket.setEvent(event);
        ticket.setSerial("ABC123");
        ticket.setMatrix(new TicketMatrix(List.of(
                new CardMatrix(List.of(1, 2, 3, 4, 5), List.of(16, 17, 18, 19, 20),
                        List.of(31, 32, 0, 34, 35), List.of(46, 47, 48, 49, 50),
                        List.of(61, 62, 63, 64, 65)),
                new CardMatrix(List.of(6, 7, 8, 9, 10), List.of(21, 22, 23, 24, 25),
                        List.of(36, 37, 0, 38, 39), List.of(51, 52, 53, 54, 55),
                        List.of(66, 67, 68, 69, 70)),
                new CardMatrix(List.of(11, 12, 13, 14, 15), List.of(26, 27, 28, 29, 30),
                        List.of(40, 41, 0, 42, 43), List.of(56, 57, 58, 59, 60),
                        List.of(71, 72, 73, 74, 75)),
                new CardMatrix(List.of(2, 4, 6, 8, 10), List.of(17, 19, 21, 23, 25),
                        List.of(32, 34, 0, 36, 38), List.of(47, 49, 51, 53, 55),
                        List.of(62, 64, 66, 68, 70))
        )));
        ticket.setActivated(false);
        ticketRepository.save(ticket);
        validSerial = "ABC123";

        // Create an already-activated ticket
        Ticket activatedTicket = new Ticket();
        activatedTicket.setEvent(event);
        activatedTicket.setSerial("ZZZ999");
        activatedTicket.setMatrix(ticket.getMatrix());
        activatedTicket.setActivated(true);
        activatedTicket.setSessionToken(UUID.randomUUID().toString());
        ticketRepository.save(activatedTicket);
        activatedSerial = "ZZZ999";
    }

    @Test
    @SuppressWarnings("null")
    void activateTicket_withValidSerial_returnsSuccess() throws Exception {
        Map<String, String> body = Map.of("serial", validSerial);

        mockMvc.perform(post("/api/v1/tickets/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.serial").value(validSerial))
                .andExpect(jsonPath("$.data.sessionToken").isNotEmpty())
                .andExpect(jsonPath("$.data.ticketId").isNotEmpty())
                .andExpect(jsonPath("$.data.matrix").isNotEmpty());
    }

    @Test
    @SuppressWarnings("null")
    void activateTicket_withAlreadyActivatedSerial_returns409() throws Exception {
        Map<String, String> body = Map.of("serial", activatedSerial);

        mockMvc.perform(post("/api/v1/tickets/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.message").value("Esta cartela já foi ativada."));
    }

    @Test
    @SuppressWarnings("null")
    void activateTicket_withNonExistentSerial_returns404() throws Exception {
        Map<String, String> body = Map.of("serial", "XXXXXX");

        mockMvc.perform(post("/api/v1/tickets/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.message").value("Cartela não encontrada."));
    }

    @Test
    @SuppressWarnings("null")
    void activateTicket_withBlankSerial_returns400() throws Exception {
        Map<String, String> body = Map.of("serial", "");

        mockMvc.perform(post("/api/v1/tickets/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"));
    }
}
