package com.trong.Computer_sell.DTO.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RAGChatRequest {
    private String message;
    private String sessionId; // Optional: for conversation continuity
}
