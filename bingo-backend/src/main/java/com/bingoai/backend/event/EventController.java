package com.bingoai.backend.event;

import com.bingoai.backend.common.dto.ApiResponse;
import com.bingoai.backend.event.dto.CreateEventRequest;
import com.bingoai.backend.event.dto.EventResponseDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EventResponseDto>> createEvent(@Valid @RequestBody CreateEventRequest request) {
        EventResponseDto createdEvent = eventService.createEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(createdEvent));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<EventResponseDto>>> getAllEvents() {
        List<EventResponseDto> events = eventService.getAllEvents();
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponseDto>> getEventById(@PathVariable UUID id) {
        try {
            EventResponseDto event = eventService.getEventById(id);
            return ResponseEntity.ok(ApiResponse.success(event));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("NOT_FOUND", e.getMessage()));
        }
    }
}
