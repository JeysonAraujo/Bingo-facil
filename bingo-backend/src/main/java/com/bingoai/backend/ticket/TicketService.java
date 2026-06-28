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
    public int generateBatch(UUID eventId, int quantity) {
        Event event = entityManager.getReference(Event.class, eventId);
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
