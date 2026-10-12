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
    private final RoundRepository roundRepository;
    private final com.bingoai.backend.ticket.TicketRepository ticketRepository;
    private final org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;
    private final com.bingoai.backend.audit.AuditLogService auditLogService;

    public EventService(EventRepository eventRepository,
                        RoundRepository roundRepository,
                        com.bingoai.backend.ticket.TicketRepository ticketRepository,
                        org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate,
                        com.bingoai.backend.audit.AuditLogService auditLogService) {
        this.eventRepository = eventRepository;
        this.roundRepository = roundRepository;
        this.ticketRepository = ticketRepository;
        this.messagingTemplate = messagingTemplate;
        this.auditLogService = auditLogService;
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
                if (roundReq.getTieBreakerRule() != null) {
                    round.setTieBreakerRule(roundReq.getTieBreakerRule());
                }

                if (roundReq.getPrizes() != null) {
                    for (CreatePrizeRequest prizeReq : roundReq.getPrizes()) {
                        Prize prize = new Prize();
                        prize.setName(prizeReq.getName());
                        prize.setType(PrizeType.valueOf(prizeReq.getType()));
                        prize.setDrawPattern(prizeReq.getDrawPattern());
                        if (prizeReq.getDivisible() != null) {
                            prize.setDivisible(prizeReq.getDivisible());
                        }
                        round.addPrize(prize);
                    }
                }
                event.addRound(round);
            }
        }

        Event savedEvent = eventRepository.save(event);
        auditLogService.logAction(admin, "CREATE_EVENT", "Created event: " + savedEvent.getId());
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
                    .map(prize -> new PrizeResponseDto(prize.getId(), prize.getName(), prize.getType(), prize.getDrawPattern(), prize.isDivisible()))
                    .collect(Collectors.toList());
            return new RoundResponseDto(round.getId(), round.getRoundNumber(), round.getStatus(), prizeDtos, round.getTieBreakerRule());
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
        
        if (EventStatus.FINALIZADO.equals(event.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Um evento finalizado não pode ser modificado.");
        }
        
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
        
        if (EventStatus.FINALIZADO.equals(event.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não é possível adicionar rodadas a um evento finalizado.");
        }
        
        Round round = new Round();
        round.setRoundNumber(request.getRoundNumber());
        if (request.getTieBreakerRule() != null) {
            round.setTieBreakerRule(request.getTieBreakerRule());
        }
        if (request.getPrizes() != null) {
            for (CreatePrizeRequest prizeReq : request.getPrizes()) {
                Prize prize = new Prize();
                prize.setName(prizeReq.getName());
                prize.setType(PrizeType.valueOf(prizeReq.getType()));
                prize.setDrawPattern(prizeReq.getDrawPattern());
                if (prizeReq.getDivisible() != null) {
                    prize.setDivisible(prizeReq.getDivisible());
                }
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
        
        if (EventStatus.FINALIZADO.equals(event.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não é possível modificar rodadas de um evento finalizado.");
        }
        
        Round targetRound = event.getRounds().stream()
                .filter(r -> r.getId().equals(roundId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Round not found with ID: " + roundId));

        targetRound.setRoundNumber(request.getRoundNumber());
        if (request.getTieBreakerRule() != null) {
            targetRound.setTieBreakerRule(request.getTieBreakerRule());
        }
        
        targetRound.getPrizes().clear();
        if (request.getPrizes() != null) {
            for (CreatePrizeRequest prizeReq : request.getPrizes()) {
                Prize prize = new Prize();
                prize.setName(prizeReq.getName());
                prize.setType(PrizeType.valueOf(prizeReq.getType()));
                prize.setDrawPattern(prizeReq.getDrawPattern());
                if (prizeReq.getDivisible() != null) {
                    prize.setDivisible(prizeReq.getDivisible());
                }
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
        
        if (EventStatus.FINALIZADO.equals(event.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O status de um evento finalizado não pode ser alterado.");
        }
        
        event.setStatus(EventStatus.valueOf(status));
        Event saved = eventRepository.save(event);
        auditLogService.logAction(admin, "UPDATE_EVENT_STATUS", "Updated event " + id + " status to " + status);
        
        if (EventStatus.FINALIZADO.equals(saved.getStatus())) {
            // Also ensure all rounds of this event are marked FINISHED
            if (saved.getRounds() != null) {
                for (Round r : saved.getRounds()) {
                    if (!RoundStatus.FINISHED.equals(r.getStatus())) {
                        r.setStatus(RoundStatus.FINISHED);
                        roundRepository.save(r);
                    }
                }
            }

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
        
        if (EventStatus.FINALIZADO.equals(event.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Um evento finalizado não pode ser excluído.");
        }
        
        // Excluir arquivo do cabeçalho físico se houver
        if (event.getHeaderImageUrl() != null) {
            try {
                java.io.File headerFile = new java.io.File(event.getHeaderImageUrl());
                if (headerFile.exists()) {
                    headerFile.delete();
                }
            } catch (Exception e) {
                System.err.println("[EventService] Failed to delete header image file on event delete: " + e.getMessage());
            }
        }
        
        eventRepository.delete(event);
        auditLogService.logAction(admin, "DELETE_EVENT", "Deleted event: " + id + " (" + event.getTitle() + ")");
    }

    @Transactional
    public EventResponseDto saveHeaderImage(@NonNull UUID eventId, MultipartFile file, com.bingoai.backend.admin.Admin admin) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with ID: " + eventId));
        validateOwnership(event, admin);
        
        if (EventStatus.FINALIZADO.equals(event.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Um evento finalizado não pode ser modificado.");
        }
        
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty");
        }
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new IllegalArgumentException("File size exceeds 2MB limit");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/jpg") && !contentType.equals("image/png") && !contentType.equals("image/webp"))) {
            throw new IllegalArgumentException("Mapeamento de arquivo inválido. Apenas imagens JPEG, PNG ou WebP são aceitas.");
        }

        try {
            org.apache.tika.Tika tika = new org.apache.tika.Tika();
            String detectedType = tika.detect(file.getInputStream());
            if (!detectedType.startsWith("image/")) {
                throw new IllegalArgumentException("O conteúdo do arquivo não é uma imagem válida.");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Falha ao validar conteúdo do arquivo.", e);
        }

        try {
            java.io.File baseDir = new java.io.File("bingo-backend").exists() 
                ? new java.io.File("bingo-backend/uploads/headers") 
                : new java.io.File("uploads/headers");
                
            if (!baseDir.exists()) {
                baseDir.mkdirs();
            }
            
            // Excluir cabeçalho antigo físico se existir
            if (event.getHeaderImageUrl() != null) {
                try {
                    java.io.File oldFile = new java.io.File(event.getHeaderImageUrl());
                    if (!oldFile.exists() && new java.io.File("bingo-backend").exists()) {
                        oldFile = new java.io.File("bingo-backend", event.getHeaderImageUrl());
                    }
                    if (oldFile.exists()) {
                        oldFile.delete();
                    }
                } catch (Exception e) {
                    System.err.println("[EventService] Failed to delete old header image: " + e.getMessage());
                }
            }
            
            String originalName = file.getOriginalFilename();
            if (originalName == null) {
                originalName = "header";
            }
            String filename = eventId.toString() + "_" + System.currentTimeMillis() + "_" + originalName.replaceAll("[^a-zA-Z0-9.-]", "_");
            java.io.File destFile = new java.io.File(baseDir, filename);
            
            java.nio.file.Files.copy(file.getInputStream(), destFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            
            String relativePath = "uploads/headers/" + filename;
            event.setHeaderImageUrl(relativePath);
            Event saved = eventRepository.save(event);
            return mapToResponseDto(saved);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to save header image", e);
        }
    }

    @Transactional
    public EventResponseDto deleteHeaderImage(@NonNull UUID eventId, com.bingoai.backend.admin.Admin admin) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with ID: " + eventId));
        validateOwnership(event, admin);
        
        if (EventStatus.FINALIZADO.equals(event.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Um evento finalizado não pode ser modificado.");
        }
        
        if (event.getHeaderImageUrl() != null) {
            try {
                java.io.File file = new java.io.File(event.getHeaderImageUrl());
                if (file.exists()) {
                    file.delete();
                }
            } catch (Exception e) {
                System.err.println("[EventService] Failed to delete header image file: " + e.getMessage());
            }
            event.setHeaderImageUrl(null);
            Event saved = eventRepository.save(event);
            return mapToResponseDto(saved);
        }
        return mapToResponseDto(event);
    }
}
