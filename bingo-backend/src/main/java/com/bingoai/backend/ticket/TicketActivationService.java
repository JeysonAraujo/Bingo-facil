package com.bingoai.backend.ticket;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class TicketActivationService {

    private final TicketRepository ticketRepository;

    public TicketActivationService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Transactional
    public TicketActivationResponseDto activateTicket(String serial) {
        Ticket ticket = ticketRepository.findWithLockBySerial(serial.toUpperCase())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Cartela não encontrada."));

        String sessionToken = ticket.getSessionToken();
        if (sessionToken == null || sessionToken.isEmpty()) {
            sessionToken = UUID.randomUUID().toString();
            ticket.setSessionToken(sessionToken);
        }
        ticket.setActivated(true);
        ticketRepository.save(ticket);

        return new TicketActivationResponseDto(
                ticket.getId(),
                ticket.getEvent().getId(),
                sessionToken,
                ticket.getSerial(),
                ticket.getMatrix()
        );
    }
}
