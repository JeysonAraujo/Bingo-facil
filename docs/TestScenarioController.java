package com.bingoai.backend.game;

import com.bingoai.backend.event.*;
import com.bingoai.backend.ticket.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@RestController
@RequestMapping("/api/test-scenario")
public class TestScenarioController {

    private final EventRepository eventRepository;
    private final TicketRepository ticketRepository;
    private final TicketGenerator ticketGenerator;

    public TestScenarioController(EventRepository eventRepository,
                                  TicketRepository ticketRepository,
                                  TicketGenerator ticketGenerator) {
        this.eventRepository = eventRepository;
        this.ticketRepository = ticketRepository;
        this.ticketGenerator = ticketGenerator;
    }

    @PostMapping("/tie")
    @Transactional
    public Map<String, Object> createTieScenario() {
        Event event = new Event();
        event.setTitle("Teste Empate");
        event.setStatus(EventStatus.AGUARDANDO);
        event.setScheduledAt(java.time.OffsetDateTime.now());
        
        Round round = new Round();
        round.setRoundNumber(1);
        Prize prize = new Prize();
        prize.setName("Prêmio Empate");
        prize.setType(PrizeType.FULL_CARD);
        prize.setDrawPattern("FULL_CARD");
        prize.setDivisible(true);
        round.addPrize(prize);
        event.addRound(round);
        
        event = eventRepository.save(event);

        List<CardMatrix> cards1 = new ArrayList<>();
        List<CardMatrix> cards2 = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            cards1.add(ticketGenerator.generateCard());
            cards2.add(ticketGenerator.generateCard());
        }
        // Force the first card to be identical for a tie
        cards2.set(0, cards1.get(0));

        TicketMatrix matrix1 = new TicketMatrix(cards1);
        TicketMatrix matrix2 = new TicketMatrix(cards2);

        Ticket t1 = new Ticket();
        t1.setEvent(event);
        t1.setSerial(ticketGenerator.generateSerial());
        t1.setMatrix(matrix1);
        t1.setActivated(true);
        t1.setPlayerName("Jogador Empate 1");
        t1.setPlayerPhone("11999999991");
        t1.setSessionToken(UUID.randomUUID().toString());

        Ticket t2 = new Ticket();
        t2.setEvent(event);
        t2.setSerial(ticketGenerator.generateSerial());
        t2.setMatrix(matrix2);
        t2.setActivated(true);
        t2.setPlayerName("Jogador Empate 2");
        t2.setPlayerPhone("11999999992");
        t2.setSessionToken(UUID.randomUUID().toString());

        ticketRepository.saveAll(Arrays.asList(t1, t2));

        Map<String, Object> response = new HashMap<>();
        response.put("eventId", event.getId());
        response.put("message", "Cenário de empate criado com sucesso. Dois jogadores possuem a mesma cartela.");
        response.put("player1", Map.of("name", t1.getPlayerName(), "serial", t1.getSerial()));
        response.put("player2", Map.of("name", t2.getPlayerName(), "serial", t2.getSerial()));
        return response;
    }
}
