package com.bingoai.backend.event;

import com.bingoai.backend.common.dto.ApiResponse;
import com.bingoai.backend.event.dto.CreateEventRequest;
import com.bingoai.backend.event.dto.EventResponseDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponseDto>> updateEvent(@PathVariable UUID id, @Valid @RequestBody CreateEventRequest request) {
        EventResponseDto updated = eventService.updateEvent(id, request);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(@PathVariable UUID id) {
        eventService.deleteEvent(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{id}/rounds")
    public ResponseEntity<ApiResponse<EventResponseDto>> addRound(
            @PathVariable UUID id,
            @Valid @RequestBody com.bingoai.backend.event.dto.CreateRoundRequest request) {
        EventResponseDto updated = eventService.addRoundToEvent(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(updated));
    }

    @PutMapping("/{id}/rounds/{roundId}")
    public ResponseEntity<ApiResponse<EventResponseDto>> updateRound(
            @PathVariable UUID id,
            @PathVariable UUID roundId,
            @Valid @RequestBody com.bingoai.backend.event.dto.CreateRoundRequest request) {
        EventResponseDto updated = eventService.updateRound(id, roundId, request);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<EventResponseDto>> updateEventStatus(@PathVariable UUID id, @RequestParam String status) {
        EventResponseDto updated = eventService.updateEventStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @PostMapping("/{id}/header-image")
    public ResponseEntity<ApiResponse<EventResponseDto>> uploadHeaderImage(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file) {
        EventResponseDto updated = eventService.saveHeaderImage(id, file);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }
}
