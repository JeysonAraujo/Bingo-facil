package com.bingoai.backend.game;

import com.bingoai.backend.common.config.SecurityConfig;
import com.bingoai.backend.event.Event;
import com.bingoai.backend.event.Round;
import com.bingoai.backend.game.dto.ClaimRequestDto;
import com.bingoai.backend.ticket.Ticket;
import com.bingoai.backend.ticket.TicketRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebMvcTest(ClaimController.class)
@Import(SecurityConfig.class)
@SuppressWarnings("null")
public class ConcurrencyClaimTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TicketRepository ticketRepository;

    @MockBean
    private ClaimRepository claimRepository;

    @MockBean
    private GameService gameService;

    @MockBean
    private com.bingoai.backend.admin.AdminSessionRepository adminSessionRepository;

    @MockBean
    private WinVerificationEngine winVerificationEngine;

    @MockBean
    private SimpMessagingTemplate messagingTemplate;

    @MockBean
    private com.bingoai.backend.audit.AuditLogService auditLogService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testConcurrentClaims_OnlyOneSucceeds() throws Exception {
        UUID ticketId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        String sessionToken = "session-token-123";

        Event mockEvent = new Event();
        mockEvent.setId(eventId);

        Ticket mockTicket = new Ticket();
        mockTicket.setId(ticketId);
        mockTicket.setEvent(mockEvent);
        mockTicket.setSessionToken(sessionToken);

        com.bingoai.backend.ticket.TicketMatrix matrix = new com.bingoai.backend.ticket.TicketMatrix();
        matrix.setCards(java.util.List.of(new com.bingoai.backend.ticket.CardMatrix()));
        mockTicket.setMatrix(matrix);

        Round mockRound = new Round();
        mockRound.setId(UUID.randomUUID());
        mockRound.setPrizes(Collections.emptyList());

        Mockito.when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(mockTicket));
        Mockito.when(gameService.getOrCreateActiveRound(eventId)).thenReturn(mockRound);
        Mockito.when(gameService.getRoundDraws(eventId)).thenReturn(Collections.emptyList());
        Mockito.when(winVerificationEngine.verifyWin(any(), any(), any())).thenReturn(true);

        // Simulate database Unique Constraint Violation for duplicate claims
        AtomicInteger saveCount = new AtomicInteger(0);
        Mockito.when(claimRepository.save(any(Claim.class))).thenAnswer(invocation -> {
            if (saveCount.getAndIncrement() > 0) {
                throw new DataIntegrityViolationException("Unique index or primary key violation");
            }
            Claim claim = invocation.getArgument(0);
            claim.setId(UUID.randomUUID());
            return claim;
        });

        ClaimRequestDto requestDto = new ClaimRequestDto(ticketId, sessionToken, 0);
        String jsonPayload = objectMapper.writeValueAsString(requestDto);

        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    startLatch.await();
                    int status = mockMvc.perform(post("/api/v1/claims")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonPayload))
                            .andReturn()
                            .getResponse()
                            .getStatus();
                    if (status == 201) {
                        successCount.incrementAndGet();
                    } else {
                        errorCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        // Release all threads simultaneously
        startLatch.countDown();
        doneLatch.await();
        executorService.shutdown();

        // Exactly 1 request should succeed (201 Created), others should fail (500/409 due to DataIntegrityViolation)
        org.junit.jupiter.api.Assertions.assertEquals(1, successCount.get());
        org.junit.jupiter.api.Assertions.assertEquals(threadCount - 1, errorCount.get());
    }
}
