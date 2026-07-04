package com.bingoai.backend.admin;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AdminSessionRepository extends JpaRepository<AdminSession, UUID> {
    @org.springframework.data.jpa.repository.Query("SELECT s FROM AdminSession s JOIN FETCH s.admin WHERE s.token = :token")
    Optional<AdminSession> findByToken(@org.springframework.data.repository.query.Param("token") String token);
    void deleteByToken(String token);
    void deleteByExpiresAtBefore(OffsetDateTime now);
}
