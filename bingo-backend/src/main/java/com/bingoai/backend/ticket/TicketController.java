package com.bingoai.backend.ticket;

import com.bingoai.backend.event.Event;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events/{eventId}/tickets")
public class TicketController {

    private final TicketService ticketService;
    private final PdfGenerator pdfGenerator;

    public TicketController(TicketService ticketService, PdfGenerator pdfGenerator) {
        this.ticketService = ticketService;
        this.pdfGenerator = pdfGenerator;
    }

    /**
     * Generates a batch of tickets for the given event.
     * Returns a lightweight summary instead of serialising every ticket,
     * avoiding a potentially huge response payload (500+ objects).
     */
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateTickets(
            @PathVariable UUID eventId,
            @RequestParam(defaultValue = "100") int quantity,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.bingoai.backend.admin.Admin admin) {
        int generated = ticketService.generateBatch(eventId, quantity, admin);
        Map<String, Object> summary = Map.of(
                "eventId", eventId,
                "ticketsGenerated", generated,
                "status", "SUCCESS"
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(summary);
    }

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> downloadTicketsPdf(
            @PathVariable UUID eventId,
            @RequestParam(required = false) String prizes,
            @RequestParam(required = false) String customFooter,
            @RequestParam(defaultValue = "false") boolean hasCutStrip,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.bingoai.backend.admin.Admin admin) {
        Event event = ticketService.getEvent(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found: " + eventId));

        if (admin == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Acesso não autorizado.");
        }
        if (!"ROLE_SUPER_ADMIN".equals(admin.getRole())) {
            if (event.getAdmin() == null || !event.getAdmin().getId().equals(admin.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem permissão para acessar os recursos deste evento.");
            }
        }

        String eventTitle = event.getTitle();

        List<Ticket> tickets = ticketService.getTicketsForEvent(eventId);
        if (tickets.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        byte[] pdfBytes = pdfGenerator.generateTicketsPdf(tickets, eventTitle, prizes, customFooter, hasCutStrip, event.getHeaderImageUrl());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", "tickets-" + eventId + ".pdf");
        headers.setContentLength(pdfBytes.length);

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
