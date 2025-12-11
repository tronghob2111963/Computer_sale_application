package com.trong.Computer_sell.repository;

import com.trong.Computer_sell.model.ChatMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {

    @Query("SELECT cm FROM ChatMessageEntity cm WHERE cm.session.id = :sessionId ORDER BY cm.createdAt ASC")
    List<ChatMessageEntity> findBySessionIdOrderByCreatedAtAsc(@Param("sessionId") UUID sessionId);

    @Query(value = """
        SELECT cm.* FROM chat_messages cm 
        WHERE cm.session_id = :sessionId 
        ORDER BY cm.created_at DESC 
        LIMIT :limit
        """, nativeQuery = true)
    List<ChatMessageEntity> findRecentMessages(
            @Param("sessionId") UUID sessionId,
            @Param("limit") int limit
    );
}
