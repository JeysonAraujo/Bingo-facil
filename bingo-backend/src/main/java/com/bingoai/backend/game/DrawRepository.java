package com.bingoai.backend.game;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DrawRepository extends JpaRepository<Draw, UUID> {

    List<Draw> findByRoundIdOrderByDrawnSequenceAsc(UUID roundId);

    Optional<Draw> findTopByRoundIdOrderByDrawnSequenceDesc(UUID roundId);

    boolean existsByRoundIdAndBallNumber(UUID roundId, Integer ballNumber);

    void deleteByRoundIdAndBallNumber(UUID roundId, Integer ballNumber);

    void deleteByRoundId(UUID roundId);
}
