package com.bingoai.backend.event;

import com.bingoai.backend.event.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.lang.NonNull;

@Service
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Transactional
    public EventResponseDto createEvent(CreateEventRequest request) {
        Event event = new Event();
        event.setTitle(request.getTitle());
        event.setScheduledAt(request.getScheduledAt());
        event.setYoutubeUrl(request.getYoutubeUrl());

        if (request.getRounds() != null) {
            for (CreateRoundRequest roundReq : request.getRounds()) {
                Round round = new Round();
                round.setRoundNumber(roundReq.getRoundNumber());

                if (roundReq.getPrizes() != null) {
                    for (CreatePrizeRequest prizeReq : roundReq.getPrizes()) {
                        Prize prize = new Prize();
                        prize.setName(prizeReq.getName());
                        prize.setType(prizeReq.getType());
                        prize.setDrawPattern(prizeReq.getDrawPattern());
                        round.addPrize(prize);
                    }
                }
                event.addRound(round);
            }
        }

        Event savedEvent = eventRepository.save(event);
        return mapToResponseDto(savedEvent);
    }

    public List<EventResponseDto> getAllEvents() {
        return eventRepository.findAll().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public EventResponseDto getEventById(@NonNull UUID id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with ID: " + id));
        return mapToResponseDto(event);
    }

    private EventResponseDto mapToResponseDto(Event event) {
        List<RoundResponseDto> roundDtos = event.getRounds().stream().map(round -> {
            List<PrizeResponseDto> prizeDtos = round.getPrizes().stream()
                    .map(prize -> new PrizeResponseDto(prize.getId(), prize.getName(), prize.getType(), prize.getDrawPattern()))
                    .collect(Collectors.toList());
            return new RoundResponseDto(round.getId(), round.getRoundNumber(), round.getStatus(), prizeDtos);
        }).collect(Collectors.toList());

        return new EventResponseDto(
                event.getId(),
                event.getTitle(),
                event.getScheduledAt(),
                event.getCreatedAt(),
                event.getStreamDelaySeconds(),
                event.getYoutubeUrl(),
                event.getStatus(),
                event.getHeaderImageUrl(),
                roundDtos
        );
    }

    @Transactional
    public EventResponseDto updateEvent(UUID id, CreateEventRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with ID: " + id));
        event.setTitle(request.getTitle());
        event.setScheduledAt(request.getScheduledAt());
        event.setYoutubeUrl(request.getYoutubeUrl());
        Event saved = eventRepository.save(event);
        return mapToResponseDto(saved);
    }

    @Transactional
    public EventResponseDto updateEventStatus(UUID id, String status) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with ID: " + id));
        event.setStatus(status.toUpperCase());
        Event saved = eventRepository.save(event);
        return mapToResponseDto(saved);
    }

    @Transactional
    public void deleteEvent(UUID id) {
        if (!eventRepository.existsById(id)) {
            throw new IllegalArgumentException("Event not found with ID: " + id);
        }
        eventRepository.deleteById(id);
    }

    @Transactional
    public EventResponseDto saveHeaderImage(UUID eventId, MultipartFile file) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with ID: " + eventId));
        
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty");
        }
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new IllegalArgumentException("File size exceeds 2MB limit");
        }

        try {
            java.io.File uploadDir = new java.io.File("uploads/headers");
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            
            String filename = eventId.toString() + "_" + file.getOriginalFilename().replaceAll("[^a-zA-Z0-9.-]", "_");
            java.io.File destFile = new java.io.File(uploadDir.getAbsolutePath(), filename);
            file.transferTo(destFile);
            
            event.setHeaderImageUrl(destFile.getAbsolutePath());
            Event saved = eventRepository.save(event);
            return mapToResponseDto(saved);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save header image", e);
        }
    }
}
