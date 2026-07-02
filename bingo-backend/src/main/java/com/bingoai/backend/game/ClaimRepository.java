package com.bingoai.backend.game;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, UUID> {
    List<Claim> findByRoundId(UUID roundId);
    boolean existsByRoundIdAndTicketIdAndCardIndex(UUID roundId, UUID ticketId, Integer cardIndex);
}
