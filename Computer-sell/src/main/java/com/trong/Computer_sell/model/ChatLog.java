package com.trong.Computer_sell.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tbl_chatlogs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatLog {
    @Id
    @UuidGenerator
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(name = "user_id", columnDefinition = "UUID", nullable = false)
    private UUID userId;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "response", columnDefinition = "TEXT")
    private String response;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;
}
