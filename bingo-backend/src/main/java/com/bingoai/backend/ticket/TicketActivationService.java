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
    public TicketActivationResponseDto activateTicket(String serial, String playerName, String playerPhone) {
        Ticket ticket = ticketRepository.findWithLockBySerial(serial.toUpperCase())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Cartela não encontrada."));

        String sessionToken;
        if (!ticket.isActivated()) {
            // Primeiro acesso / Ativação
            sessionToken = UUID.randomUUID().toString();
            ticket.setSessionToken(sessionToken);
            ticket.setPlayerName(playerName);
            ticket.setPlayerPhone(playerPhone);
            ticket.setActivated(true);
        } else {
            // Acesso subsequente / Login por desconexão
            String dbName = ticket.getPlayerName() != null ? ticket.getPlayerName().trim().toLowerCase() : "";
            String dbPhone = ticket.getPlayerPhone() != null ? ticket.getPlayerPhone().trim().replaceAll("[^0-9]", "") : "";

            String reqName = playerName != null ? playerName.trim().toLowerCase() : "";
            String reqPhone = playerPhone != null ? playerPhone.trim().replaceAll("[^0-9]", "") : "";

            if (!dbName.equals(reqName) || !dbPhone.equals(reqPhone)) {
                throw new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Esta cartela já está vinculada a outro jogador.");
            }

            // Sempre gera um novo token de alta entropia na reativação
            sessionToken = UUID.randomUUID().toString();
            ticket.setSessionToken(sessionToken);
        }

        ticketRepository.save(ticket);

        return new TicketActivationResponseDto(
                ticket.getId(),
                ticket.getEvent().getId(),
                sessionToken,
                ticket.getSerial(),
                ticket.getMatrix()
        );
    }

    public TicketValidationResponseDto validateTicket(String serial) {
        Ticket ticket = ticketRepository.findBySerial(serial.toUpperCase())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Cartela não encontrada."));

        return new TicketValidationResponseDto(
                ticket.getSerial(),
                ticket.getEvent().getId(),
                ticket.getEvent().getTitle(),
                ticket.getEvent().getHeaderImageUrl(),
                ticket.isActivated()
        );
    }
}
