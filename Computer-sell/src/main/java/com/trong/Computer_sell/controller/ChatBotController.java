package com.trong.Computer_sell.controller;

import com.trong.Computer_sell.DTO.ChatMessageDTO;
import com.trong.Computer_sell.DTO.ChatResponseDTO;
import com.trong.Computer_sell.service.ChatBotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatBotController {

    private final ChatBotService chatBotService;

    @PostMapping("/chat")
    public ResponseEntity<ChatResponseDTO> chat(
            @RequestBody ChatMessageDTO message,
            @RequestParam String userId) {
        ChatResponseDTO response = chatBotService.chat(message, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/product-availability")
    public ResponseEntity<String> getProductAvailability(
            @RequestParam String productName) {
        String availability = chatBotService.getProductAvailability(productName);
        return ResponseEntity.ok(availability);
    }

    @GetMapping("/product-price")
    public ResponseEntity<String> getProductPrice(
            @RequestParam String productName) {
        String price = chatBotService.getProductPrice(productName);
        return ResponseEntity.ok(price);
    }
}
