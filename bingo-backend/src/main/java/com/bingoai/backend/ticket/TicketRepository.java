package com.bingoai.backend.ticket;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID> {
    List<Ticket> findByEventId(UUID eventId);
    List<Ticket> findByEventIdAndActivatedTrue(UUID eventId);
    boolean existsByEventId(UUID eventId);

    Optional<Ticket> findBySerial(String serial);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Ticket> findWithLockBySerial(String serial);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE Ticket t SET t.sessionToken = null WHERE t.event.id = :eventId")
    void invalidateTokensForEvent(@org.springframework.data.repository.query.Param("eventId") UUID eventId);
}
