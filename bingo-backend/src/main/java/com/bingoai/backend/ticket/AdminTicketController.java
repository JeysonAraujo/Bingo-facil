package com.bingoai.backend.ticket;

import com.bingoai.backend.common.dto.ApiResponse;
import com.bingoai.backend.game.AdminOverrideService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tickets")
public class AdminTicketController {

    private final AdminOverrideService adminOverrideService;

    public AdminTicketController(AdminOverrideService adminOverrideService) {
        this.adminOverrideService = adminOverrideService;
    }

    /**
     * Resolves and searches a ticket layout dynamically by its unique serial.
     * Used by coordinators to run card audits and forced overrides.
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<TicketResponseDto>> searchTicket(@RequestParam String serial) {
        try {
            TicketResponseDto ticket = adminOverrideService.findTicketBySerial(serial);
            return ResponseEntity.ok(ApiResponse.success(ticket));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("NOT_FOUND", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("SERVER_ERROR", e.getMessage()));
        }
    }
}
