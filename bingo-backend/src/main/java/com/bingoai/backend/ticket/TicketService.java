package com.bingoai.backend.ticket;

import com.bingoai.backend.event.Event;
import com.bingoai.backend.event.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.lang.NonNull;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketGenerator ticketGenerator;
    private final EventRepository eventRepository;
    private final jakarta.persistence.EntityManager entityManager;

    public TicketService(TicketRepository ticketRepository,
                         TicketGenerator ticketGenerator,
                         EventRepository eventRepository,
                         jakarta.persistence.EntityManager entityManager) {
        this.ticketRepository = ticketRepository;
        this.ticketGenerator = ticketGenerator;
        this.eventRepository = eventRepository;
        this.entityManager = entityManager;
    }

    /**
     * Generates a batch of N tickets for the event and persists them in a single transaction.
     *
     * @return the number of tickets actually persisted.
     */
    @Transactional
    public int generateBatch(UUID eventId, int quantity, com.bingoai.backend.admin.Admin admin) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Evento não encontrado com o ID: " + eventId));

        if (admin == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Acesso não autorizado.");
        }
        if (!"ROLE_SUPER_ADMIN".equals(admin.getRole())) {
            if (event.getAdmin() == null || !event.getAdmin().getId().equals(admin.getId())) {
                throw new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.FORBIDDEN, "Você não tem permissão para acessar este evento.");
            }
        }

        if (event.getStatus() != com.bingoai.backend.event.EventStatus.AGUARDANDO) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "As cartelas só podem ser geradas se o evento estiver com status AGUARDANDO.");
        }

        boolean ticketsAlreadyExist = ticketRepository.existsByEventId(eventId);
        if (ticketsAlreadyExist) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "As cartelas para este evento já foram geradas.");
        }

        if (quantity <= 0 || quantity > 5000) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "A quantidade de cartelas a ser gerada deve estar entre 1 e 5000.");
        }

        List<Ticket> tickets = new ArrayList<>(quantity);
        Set<String> usedSerials = new java.util.HashSet<>(quantity * 2);

        for (int i = 0; i < quantity; i++) {
            Ticket ticket = new Ticket();
            ticket.setEvent(event);

            // Retry until a unique serial is generated within this batch
            String serial;
            do {
                serial = ticketGenerator.generateSerial();
            } while (!usedSerials.add(serial));
            ticket.setSerial(serial);

            List<CardMatrix> cards = new ArrayList<>(4);
            for (int j = 0; j < 4; j++) {
                cards.add(ticketGenerator.generateCard());
            }
            ticket.setMatrix(new TicketMatrix(cards));
            ticket.setActivated(false);

            tickets.add(ticket);
        }

        ticketRepository.saveAll(tickets);
        return tickets.size();
    }

    public List<Ticket> getTicketsForEvent(UUID eventId) {
        return ticketRepository.findByEventId(eventId);
    }

    /**
     * Returns the title of the event without loading the LAZY-associated Event entity
     * from within Ticket, avoiding LazyInitializationException in the PDF generator.
     */
    public Optional<String> getEventTitle(@NonNull UUID eventId) {
        return eventRepository.findById(eventId).map(Event::getTitle);
    }

    public Optional<Event> getEvent(@NonNull UUID eventId) {
        return eventRepository.findById(eventId);
    }
}
