package com.trong.Computer_sell.repository;

import com.trong.Computer_sell.model.ChatSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSessionEntity, UUID> {

    Optional<ChatSessionEntity> findBySessionToken(String sessionToken);

    List<ChatSessionEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);

    @Query("SELECT cs FROM ChatSessionEntity cs LEFT JOIN FETCH cs.messages WHERE cs.id = :id")
    Optional<ChatSessionEntity> findByIdWithMessages(@Param("id") UUID id);

    @Query("SELECT cs FROM ChatSessionEntity cs LEFT JOIN FETCH cs.messages WHERE cs.sessionToken = :token")
    Optional<ChatSessionEntity> findBySessionTokenWithMessages(@Param("token") String token);
}
