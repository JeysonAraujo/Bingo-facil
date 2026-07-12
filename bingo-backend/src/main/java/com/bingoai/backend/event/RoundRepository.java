package com.bingoai.backend.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoundRepository extends JpaRepository<Round, UUID> {

    List<Round> findByEventIdOrderByRoundNumberAsc(UUID eventId);

    Optional<Round> findFirstByEventIdAndStatus(UUID eventId, RoundStatus status);

    Optional<Round> findFirstByEventIdOrderByRoundNumberAsc(UUID eventId);
}
