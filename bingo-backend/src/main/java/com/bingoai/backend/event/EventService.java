package com.bingoai.backend.event;

import com.bingoai.backend.event.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.lang.NonNull;

@Service
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;
    private final com.bingoai.backend.ticket.TicketRepository ticketRepository;
    private final org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    public EventService(EventRepository eventRepository,
                        com.bingoai.backend.ticket.TicketRepository ticketRepository,
                        org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate) {
        this.eventRepository = eventRepository;
        this.ticketRepository = ticketRepository;
        this.messagingTemplate = messagingTemplate;
    }

    private void validateOwnership(Event event, com.bingoai.backend.admin.Admin admin) {
        if (admin == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Acesso não autorizado.");
        }
        if ("ROLE_SUPER_ADMIN".equals(admin.getRole())) {
            return;
        }
        if (event.getAdmin() == null || !event.getAdmin().getId().equals(admin.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem permissão para acessar este evento.");
        }
    }

    @Transactional
    public EventResponseDto createEvent(CreateEventRequest request, com.bingoai.backend.admin.Admin admin) {
        Event event = new Event();
        event.setTitle(request.getTitle());
        event.setScheduledAt(request.getScheduledAt());
        event.setYoutubeUrl(request.getYoutubeUrl());
        event.setAdmin(admin);

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

    public List<EventResponseDto> getAllEvents(com.bingoai.backend.admin.Admin admin) {
        if (admin == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Acesso não autorizado.");
        }
        
        List<Event> events;
        if ("ROLE_SUPER_ADMIN".equals(admin.getRole())) {
            events = eventRepository.findAll();
        } else {
            events = eventRepository.findByAdminId(admin.getId());
        }

        return events.stream()
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
                roundDtos,
                event.getAdmin() != null ? event.getAdmin().getUsername() : null
        );
    }

    @Transactional
    public EventResponseDto updateEvent(@NonNull UUID id, CreateEventRequest request, com.bingoai.backend.admin.Admin admin) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with ID: " + id));
        validateOwnership(event, admin);
        
        event.setTitle(request.getTitle());
        event.setScheduledAt(request.getScheduledAt());
        event.setYoutubeUrl(request.getYoutubeUrl());
        Event saved = eventRepository.save(event);
        return mapToResponseDto(saved);
    }

    @Transactional
    public EventResponseDto addRoundToEvent(@NonNull UUID eventId, CreateRoundRequest request, com.bingoai.backend.admin.Admin admin) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with ID: " + eventId));
        validateOwnership(event, admin);
        
        Round round = new Round();
        round.setRoundNumber(request.getRoundNumber());
        if (request.getPrizes() != null) {
            for (CreatePrizeRequest prizeReq : request.getPrizes()) {
                Prize prize = new Prize();
                prize.setName(prizeReq.getName());
                prize.setType(prizeReq.getType());
                prize.setDrawPattern(prizeReq.getDrawPattern());
                round.addPrize(prize);
            }
        }
        event.addRound(round);
        Event saved = eventRepository.save(event);
        return mapToResponseDto(saved);
    }

    @Transactional
    public EventResponseDto updateRound(@NonNull UUID eventId, @NonNull UUID roundId, CreateRoundRequest request, com.bingoai.backend.admin.Admin admin) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with ID: " + eventId));
        validateOwnership(event, admin);
        
        Round targetRound = event.getRounds().stream()
                .filter(r -> r.getId().equals(roundId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Round not found with ID: " + roundId));

        targetRound.setRoundNumber(request.getRoundNumber());
        
        targetRound.getPrizes().clear();
        if (request.getPrizes() != null) {
            for (CreatePrizeRequest prizeReq : request.getPrizes()) {
                Prize prize = new Prize();
                prize.setName(prizeReq.getName());
                prize.setType(prizeReq.getType());
                prize.setDrawPattern(prizeReq.getDrawPattern());
                targetRound.addPrize(prize);
            }
        }
        
        Event saved = eventRepository.save(event);
        return mapToResponseDto(saved);
    }

    @Transactional
    public EventResponseDto updateEventStatus(@NonNull UUID id, String status, com.bingoai.backend.admin.Admin admin) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with ID: " + id));
        validateOwnership(event, admin);
        
        event.setStatus(status.toUpperCase());
        Event saved = eventRepository.save(event);
        
        if ("FINALIZADO".equals(saved.getStatus())) {
            // Send STOMP message before invalidating tokens
            messagingTemplate.convertAndSend(
                    "/topic/events/" + id + "/status",
                    new com.bingoai.backend.game.dto.StatusMessage("FINALIZADO")
            );
            
            ticketRepository.invalidateTokensForEvent(id);
            System.out.println("[EventService] Invalidated all ticket session tokens for finalized event " + id);
        }
        
        return mapToResponseDto(saved);
    }

    @Transactional
    public void deleteEvent(@NonNull UUID id, com.bingoai.backend.admin.Admin admin) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with ID: " + id));
        validateOwnership(event, admin);
        
        eventRepository.delete(event);
    }

    @Transactional
    public EventResponseDto saveHeaderImage(@NonNull UUID eventId, MultipartFile file, com.bingoai.backend.admin.Admin admin) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with ID: " + eventId));
        validateOwnership(event, admin);
        
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
            
            String originalName = file.getOriginalFilename();
            if (originalName == null) {
                originalName = "header";
            }
            String filename = eventId.toString() + "_" + originalName.replaceAll("[^a-zA-Z0-9.-]", "_");
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
