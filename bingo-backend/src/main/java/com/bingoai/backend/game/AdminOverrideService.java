package com.bingoai.backend.game;

import com.bingoai.backend.ticket.Ticket;
import com.bingoai.backend.ticket.TicketRepository;
import com.bingoai.backend.ticket.TicketResponseDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.lang.NonNull;

@Service
@Transactional
public class AdminOverrideService {

    private final TicketRepository ticketRepository;

    public AdminOverrideService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    /**
     * Resolves and maps a ticket entity by its unique serial string.
     */
    @Transactional(readOnly = true)
    public TicketResponseDto findTicketBySerial(String serial) {
        Ticket ticket = ticketRepository.findBySerial(serial.toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Cartela não encontrada com o serial: " + serial));

        return new TicketResponseDto(
                ticket.getId(),
                ticket.getEvent().getId(),
                ticket.getSerial(),
                ticket.getMatrix(),
                ticket.isActivated(),
                ticket.getMarkedOverrides()
        );
    }

    /**
     * Toggles a forced number marking (override) on a specific ticket card.
     * Guaranteed to persist and return the updated collection of overrides.
     */
    public List<Integer> applyOverride(@NonNull UUID ticketId, int ballNumber, String action) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Cartela não encontrada com o ID: " + ticketId));

        List<Integer> overrides = new ArrayList<>(ticket.getMarkedOverrides());

        if ("MARK".equalsIgnoreCase(action)) {
            if (!overrides.contains(ballNumber)) {
                overrides.add(ballNumber);
            }
        } else if ("UNMARK".equalsIgnoreCase(action)) {
            overrides.remove(Integer.valueOf(ballNumber));
        } else {
            throw new IllegalArgumentException("Ação de override inválida: " + action);
        }

        ticket.setMarkedOverrides(overrides);
        ticketRepository.save(ticket);

        return overrides;
    }
}
