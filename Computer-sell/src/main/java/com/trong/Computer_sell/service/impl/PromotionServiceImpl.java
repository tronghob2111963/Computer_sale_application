package com.trong.Computer_sell.service.impl;

import com.trong.Computer_sell.DTO.request.promotion.PromotionRequest;
import com.trong.Computer_sell.DTO.response.promotion.PromotionResponse;
import com.trong.Computer_sell.model.PromotionEntity;
import com.trong.Computer_sell.repository.PromotionRepository;
import com.trong.Computer_sell.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PromotionServiceImpl implements PromotionService {

    private final PromotionRepository promotionRepository;

    @Override
    public PromotionResponse create(PromotionRequest request) {
        if (promotionRepository.findByPromoCode(request.getPromoCode()).isPresent()) {
            throw new RuntimeException("Promo code already exists!");
        }

        PromotionEntity entity = new PromotionEntity();
        entity.setPromoCode(request.getPromoCode());
        entity.setDescription(request.getDescription());
        entity.setDiscountPercent(request.getDiscountPercent());
        entity.setStartDate(request.getStartDate());
        entity.setEndDate(request.getEndDate());
        entity.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        promotionRepository.save(entity);
        return PromotionResponse.fromEntity(entity);
    }

    @Override
    public PromotionResponse update(UUID id, PromotionRequest request) {
        PromotionEntity entity = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found"));

        entity.setDescription(request.getDescription());
        entity.setDiscountPercent(request.getDiscountPercent());
        entity.setStartDate(request.getStartDate());
        entity.setEndDate(request.getEndDate());
        entity.setIsActive(request.getIsActive());

        promotionRepository.save(entity);
        return PromotionResponse.fromEntity(entity);
    }

    @Override
    public void delete(UUID id) {
        if (!promotionRepository.existsById(id))
            throw new RuntimeException("Promotion not found");
        promotionRepository.deleteById(id);
    }

    @Override
    public List<PromotionResponse> getAll() {
        return promotionRepository.findAll().stream()
                .map(PromotionResponse::fromEntity)
                .toList();
    }

    @Override
    public PromotionResponse getByCode(String code) {
        PromotionEntity entity = promotionRepository.findByPromoCode(code)
                .orElseThrow(() -> new RuntimeException("Promotion code not found"));
        return PromotionResponse.fromEntity(entity);
    }
}
