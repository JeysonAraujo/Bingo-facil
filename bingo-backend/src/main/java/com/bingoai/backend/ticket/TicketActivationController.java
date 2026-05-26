package com.bingoai.backend.ticket;

import com.bingoai.backend.common.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tickets")
public class TicketActivationController {

    private final TicketActivationService ticketActivationService;

    public TicketActivationController(TicketActivationService ticketActivationService) {
        this.ticketActivationService = ticketActivationService;
    }

    /**
     * Activates a ticket by serial code.
     * Returns the session token and the full ticket matrix for the player game room.
     */
    @PostMapping("/activate")
    public ResponseEntity<ApiResponse<TicketActivationResponseDto>> activateTicket(
            @Valid @RequestBody ActivateTicketRequest request) {
        TicketActivationResponseDto responseDto = ticketActivationService.activateTicket(request.getSerial());
        return ResponseEntity.ok(ApiResponse.success(responseDto));
    }
}
