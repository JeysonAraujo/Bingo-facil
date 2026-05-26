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
        Ticket ticket = ticketRepository.findBySerial(serial.toUpperCase())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Cartela não encontrada."));

        if (ticket.isActivated()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Esta cartela já foi ativada.");
        }

        String sessionToken = UUID.randomUUID().toString();
        ticket.setActivated(true);
        ticket.setSessionToken(sessionToken);
        ticketRepository.save(ticket);

        return new TicketActivationResponseDto(
                ticket.getId(),
                sessionToken,
                ticket.getSerial(),
                ticket.getMatrix()
        );
    }
}
