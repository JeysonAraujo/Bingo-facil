package com.bingoai.backend.ticket;

import com.bingoai.backend.common.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import jakarta.servlet.http.HttpServletRequest;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/v1/tickets")
public class TicketActivationController {

    private final TicketActivationService ticketActivationService;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public TicketActivationController(TicketActivationService ticketActivationService) {
        this.ticketActivationService = ticketActivationService;
    }

    private Bucket resolveBucket(HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        if (ip == null) ip = "unknown";
        return buckets.computeIfAbsent(ip, k -> Bucket.builder()
                .addLimit(Bandwidth.classic(10, Refill.greedy(10, Duration.ofMinutes(1))))
                .build());
    }

    /**
     * Activates a ticket by serial code.
     * Returns the session token and the full ticket matrix for the player game room.
     */
    @PostMapping("/activate")
    public ResponseEntity<ApiResponse<TicketActivationResponseDto>> activateTicket(
            @Valid @RequestBody ActivateTicketRequest request,
            HttpServletRequest httpServletRequest) {
        if (!resolveBucket(httpServletRequest).tryConsume(1)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Muitas tentativas. Tente novamente mais tarde.");
        }

        TicketActivationResponseDto responseDto = ticketActivationService.activateTicket(
                request.getSerial(),
                request.getPlayerName(),
                request.getPlayerPhone()
        );
        return ResponseEntity.ok(ApiResponse.success(responseDto));
    }

    /**
     * Validates a ticket by serial code, returning basic event info.
     * Does NOT fully activate the ticket or issue a session token.
     */
    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<TicketValidationResponseDto>> validateTicket(
            @RequestParam String serial,
            HttpServletRequest httpServletRequest) {
        if (!resolveBucket(httpServletRequest).tryConsume(1)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Muitas tentativas. Tente novamente mais tarde.");
        }
        TicketValidationResponseDto responseDto = ticketActivationService.validateTicket(serial);
        return ResponseEntity.ok(ApiResponse.success(responseDto));
    }
}
