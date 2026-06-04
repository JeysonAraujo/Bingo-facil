package com.bingoai.backend.game;

import com.bingoai.backend.common.config.SecurityConfig;
import com.bingoai.backend.event.Event;
import com.bingoai.backend.event.Round;
import com.bingoai.backend.game.dto.BingoClaimMessage;
import com.bingoai.backend.ticket.Ticket;
import com.bingoai.backend.ticket.TicketRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClaimController.class)
@Import(SecurityConfig.class)
public class ClaimControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TicketRepository ticketRepository;

    @MockBean
    private ClaimRepository claimRepository;

    @MockBean
    private GameService gameService;

    @MockBean
    private WinVerificationEngine winVerificationEngine;

    @MockBean
    private SimpMessagingTemplate messagingTemplate;

    @Test
    public void testGetClaimsByRound_Success() throws Exception {
        UUID roundId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();
        UUID claimId = UUID.randomUUID();

        Ticket ticket = new Ticket();
        ticket.setId(ticketId);
        ticket.setSerial("ABC-123");

        Round round = new Round();
        round.setId(roundId);

        Claim claim = new Claim(ticket, round, 0);
        claim.setId(claimId);

        Mockito.when(claimRepository.findByRoundId(roundId))
                .thenReturn(Collections.singletonList(claim));

        mockMvc.perform(get("/api/v1/claims/round/" + roundId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].claimId").value(claimId.toString()))
                .andExpect(jsonPath("$.data[0].ticketId").value(ticketId.toString()))
                .andExpect(jsonPath("$.data[0].roundId").value(roundId.toString()))
                .andExpect(jsonPath("$.data[0].cardIndex").value(0))
                .andExpect(jsonPath("$.data[0].status").value("PENDING"))
                .andExpect(jsonPath("$.data[0].serial").value("ABC-123"));
    }

    @Test
    public void testApproveClaim_Success() throws Exception {
        UUID claimId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();
        UUID roundId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        Event event = new Event();
        event.setId(eventId);

        Round round = new Round();
        round.setId(roundId);
        round.setEvent(event);

        Ticket ticket = new Ticket();
        ticket.setId(ticketId);
        ticket.setSerial("XYZ-987");
        ticket.setSessionToken("sess-token");

        Claim claim = new Claim(ticket, round, 1);
        claim.setId(claimId);

        Mockito.when(claimRepository.findById(claimId)).thenReturn(Optional.of(claim));
        Mockito.when(claimRepository.save(any(Claim.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/api/v1/claims/" + claimId + "/approve")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.claimId").value(claimId.toString()))
                .andExpect(jsonPath("$.data.status").value("APPROVED"))
                .andExpect(jsonPath("$.data.serial").value("XYZ-987"));

        Mockito.verify(messagingTemplate).convertAndSend(
                Mockito.eq("/topic/events/" + eventId + "/claims"),
                any(BingoClaimMessage.class)
        );
    }

    @Test
    public void testRejectClaim_Success() throws Exception {
        UUID claimId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();
        UUID roundId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        Event event = new Event();
        event.setId(eventId);

        Round round = new Round();
        round.setId(roundId);
        round.setEvent(event);

        Ticket ticket = new Ticket();
        ticket.setId(ticketId);
        ticket.setSerial("XYZ-987");
        ticket.setSessionToken("sess-token");

        Claim claim = new Claim(ticket, round, 1);
        claim.setId(claimId);

        Mockito.when(claimRepository.findById(claimId)).thenReturn(Optional.of(claim));
        Mockito.when(claimRepository.save(any(Claim.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/api/v1/claims/" + claimId + "/reject")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.claimId").value(claimId.toString()))
                .andExpect(jsonPath("$.data.status").value("REJECTED"))
                .andExpect(jsonPath("$.data.serial").value("XYZ-987"));

        Mockito.verify(messagingTemplate).convertAndSend(
                Mockito.eq("/topic/events/" + eventId + "/claims"),
                any(BingoClaimMessage.class)
        );
    }
}
