package com.trong.Computer_sell.repository;

import com.trong.Computer_sell.model.ChatLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChatLogRepository extends JpaRepository<ChatLog, UUID> {
    List<ChatLog> findByUserId(UUID userId);
}
