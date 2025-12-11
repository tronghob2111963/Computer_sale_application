package com.trong.Computer_sell.service;

import com.trong.Computer_sell.DTO.ChatMessageDTO;
import com.trong.Computer_sell.DTO.ChatResponseDTO;

public interface ChatBotService {
    ChatResponseDTO chat(ChatMessageDTO message, String userId);
    String getProductAvailability(String productName);
    String getProductPrice(String productName);
}
