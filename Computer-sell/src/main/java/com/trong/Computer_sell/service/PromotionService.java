package com.trong.Computer_sell.service;

import com.trong.Computer_sell.DTO.request.promotion.PromotionRequest;
import com.trong.Computer_sell.DTO.response.promotion.PromotionResponse;

import java.util.List;
import java.util.UUID;



public interface PromotionService {
    PromotionResponse create(PromotionRequest request);
    PromotionResponse update(UUID id, PromotionRequest request);
    void delete(UUID id);
    List<PromotionResponse> getAll();
    PromotionResponse getByCode(String code);
}
