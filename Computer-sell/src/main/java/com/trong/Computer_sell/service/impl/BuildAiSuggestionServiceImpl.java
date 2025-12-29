package com.trong.Computer_sell.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trong.Computer_sell.DTO.request.build.BuildSuggestRequest;
import com.trong.Computer_sell.DTO.response.build.BuildSuggestResponse;
import com.trong.Computer_sell.DTO.response.build.SuggestedPartDTO;
import com.trong.Computer_sell.model.ProductEntity;
import com.trong.Computer_sell.model.ProductTypeEntity;
import com.trong.Computer_sell.repository.ProductRepository;
import com.trong.Computer_sell.repository.ProductTypeRepository;
import com.trong.Computer_sell.service.BuildAiSuggestionService;
import com.trong.Computer_sell.service.OpenAIService;
import com.trong.Computer_sell.service.PresetGuideService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BuildAiSuggestionServiceImpl implements BuildAiSuggestionService {

    private static final BigDecimal DEFAULT_BUDGET = BigDecimal.valueOf(15_000_000L);
    private static final int TOP_N = 10;

    private final ProductRepository productRepository;
    private final ProductTypeRepository productTypeRepository;
    private final PresetGuideService presetGuideService;
    private final OpenAIService openAIService;
    private final ObjectMapper objectMapper;

    // Tên các loại linh kiện - sẽ được map với ProductType trong database
    private static final List<String> PART_TYPES = List.of(
            "CPU", "MAINBOARD", "RAM", "GPU", "STORAGE", "PSU", "COOLER", "CASE", "MONITOR"
    );

    @Override
    public BuildSuggestResponse suggest(BuildSuggestRequest request) {
        BigDecimal budget = sanitizeBudget(request.getBudget());
        
        // Thu thập danh sách sản phẩm có sẵn theo từng loại
        Map<String, List<ProductInfo>> availableProducts = collectAvailableProducts(budget);
        
        // Tạo prompt cho AI
        String systemPrompt = buildSystemPrompt();
        String userPrompt = buildUserPrompt(request, budget, availableProducts);
        
        try {
            // Gọi AI để gợi ý
            String aiResponse = openAIService.chatCompletion(systemPrompt, userPrompt);
            log.info("AI Response: {}", aiResponse);
            
            // Parse response từ AI
            BuildSuggestResponse response = parseAiResponse(aiResponse, budget, request);
            
            // Nếu AI trả về null (profile sai), parts rỗng, hoặc không có linh kiện -> dùng fallback
            if (response == null || response.getParts() == null || response.getParts().isEmpty()) {
                log.warn("AI returned invalid/empty response, falling back to rule-based");
                return fallbackSuggestion(request, budget, availableProducts);
            }
            
            // FINAL VALIDATION: Đảm bảo profile luôn đúng trước khi trả về
            String useCase = normalizeUseCase(request.getUseCase());
            String resolution = request.getResolution() != null ? request.getResolution() : "1080p";
            String correctedProfile = validateAndFixProfile(response.getProfile(), useCase, resolution);
            if (!correctedProfile.equals(response.getProfile())) {
                log.error("⛔⛔⛔ FINAL CHECK: Correcting profile from '{}' to '{}'", response.getProfile(), correctedProfile);
                // Tạo response mới với profile đã sửa
                response = BuildSuggestResponse.builder()
                        .profile(correctedProfile)
                        .budgetInput(response.getBudgetInput())
                        .estimatedTotal(response.getEstimatedTotal())
                        .note(response.getNote())
                        .parts(response.getParts())
                        .build();
            }
            
            return response;
        } catch (Exception e) {
            log.error("AI suggestion failed, falling back to rule-based", e);
            return fallbackSuggestion(request, budget, availableProducts);
        }
    }

    @Override
    public Object getPresetGuide() {
        return presetGuideService.getSections();
    }

    @Override
    public Object getAllProductTypes() {
        return productTypeRepository.findAll().stream()
                .map(pt -> Map.of(
                        "id", pt.getId().toString(),
                        "name", pt.getName(),
                        "normalized", normalize(pt.getName())
                ))
                .toList();
    }

    private Map<String, List<ProductInfo>> collectAvailableProducts(BigDecimal budget) {
        Map<String, List<ProductInfo>> result = new LinkedHashMap<>();
        
        for (String typeName : PART_TYPES) {
            UUID typeId = resolveTypeId(typeName);
            if (typeId == null) {
                log.warn("Skipping type '{}' - no matching ProductType found", typeName);
                continue;
            }
            
            // Lấy tất cả sản phẩm theo loại (không giới hạn giá)
            var allProducts = productRepository.searchProductByProductTypeId(typeId, PageRequest.of(0, TOP_N));
            log.info("Type '{}' has {} total products in DB", typeName, allProducts.getTotalElements());
            
            // Ưu tiên sản phẩm còn hàng
            List<ProductInfo> productInfos = allProducts.getContent().stream()
                    .filter(p -> p.getStock() != null && p.getStock() > 0)
                    .map(p -> new ProductInfo(
                            p.getId().toString(),
                            p.getName(),
                            p.getBrandId() != null ? p.getBrandId().getName() : "N/A",
                            p.getPrice(),
                            p.getStock()
                    ))
                    .collect(Collectors.toList());
            
            // Nếu không có sản phẩm còn hàng, lấy tất cả (kể cả hết hàng)
            if (productInfos.isEmpty() && !allProducts.isEmpty()) {
                log.info("No in-stock products for type '{}', including out-of-stock", typeName);
                productInfos = allProducts.getContent().stream()
                        .map(p -> new ProductInfo(
                                p.getId().toString(),
                                p.getName(),
                                p.getBrandId() != null ? p.getBrandId().getName() : "N/A",
                                p.getPrice(),
                                p.getStock() != null ? p.getStock() : 0
                        ))
                        .collect(Collectors.toList());
            }
            
            if (!productInfos.isEmpty()) {
                result.put(typeName, productInfos);
                log.info("Found {} products for type '{}'", productInfos.size(), typeName);
            } else {
                log.warn("No products available for type '{}' (typeId: {})", typeName, typeId);
            }
        }
        
        log.info("Collected products for {} types: {}", result.size(), result.keySet());
        return result;
    }

    private String buildSystemPrompt() {
        return """
            Bạn là chuyên gia tư vấn build PC. CHỈ TRẢ VỀ JSON, KHÔNG TEXT KHÁC.
            
             QUY TẮC TUYỆT ĐỐI:
            
            1. PROFILE PHẢI KHỚP VỚI MỤC ĐÍCH:
               - useCase="gaming" → profile="Gaming 1080p" hoặc "Gaming 1440p" hoặc "Gaming 4K"
               - useCase="creator" → profile="Creator" hoặc "Đồ họa"
               - useCase="office" → profile="Office" hoặc "Văn phòng"
              
            
            2. NGÂN SÁCH:
               - TỔNG GIÁ PHẢI TỪ 70%-100% NGÂN SÁCH
               - KHÔNG ĐƯỢC VƯỢT QUÁ 100% NGÂN SÁCH
               - VD: Ngân sách 20 triệu → Tổng giá 14-20 triệu (KHÔNG ĐƯỢC QUÁ 20 triệu)
            
            3. PHÂN BỔ NGÂN SÁCH GAMING:
               - GPU: 30-40% (QUAN TRỌNG NHẤT)
               - CPU: 12-18%
               - Monitor: 12-18%
               - RAM: 8-12%
               - Mainboard: 6-10%
               - Storage: 5-8%
               - PSU: 4-7%
               - Case: 3-5%
               - Cooler: 2-4%
            
            4. NGÔN NGỮ: Tất cả "note" và "reason" PHẢI BẰNG TIẾNG VIỆT
            
            5. LINH KIỆN BẮT BUỘC: CPU, MAINBOARD, RAM, GPU, STORAGE, PSU, COOLER, CASE, MONITOR
            
            JSON FORMAT:
            {"profile":"Gaming 1080p","note":"Cấu hình gaming mạnh mẽ","parts":[{"productType":"CPU","productId":"uuid","productName":"Tên","reason":"Hiệu năng tốt cho gaming"}]}
            
            VÍ DỤ REASON ĐÚNG (TIẾNG VIỆT):
            - "CPU mạnh mẽ, phù hợp cho gaming"
            - "GPU hiệu năng cao, chơi game mượt mà"
            - "RAM đủ lớn cho đa nhiệm"
            
            VÍ DỤ SAI (TIẾNG ANH):
            - "Good for office work" 
            - "Suitable for general use" 
            """;
    }

    private String buildUserPrompt(BuildSuggestRequest request, BigDecimal budget, Map<String, List<ProductInfo>> products) {
        String useCase = normalizeUseCase(request.getUseCase());
        String resolution = request.getResolution() != null ? request.getResolution() : "1080p";
        
        // Tính target price cho GPU (gaming) để AI biết cần chọn GPU mạnh
        BigDecimal gpuTargetMin = budget.multiply(BigDecimal.valueOf(0.30));
        BigDecimal gpuTargetMax = budget.multiply(BigDecimal.valueOf(0.40));
        
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════\n");
        sb.append("⛔ THÔNG TIN BẮT BUỘC:\n");
        sb.append("═══════════════════════════════════════\n");
        sb.append("🎯 MỤC ĐÍCH: ").append(useCase.toUpperCase()).append("\n");
        sb.append("📺 ĐỘ PHÂN GIẢI: ").append(resolution.toUpperCase()).append("\n");
        sb.append("💰 NGÂN SÁCH: ").append(formatCurrency(budget)).append("\n");
        
        // Xác định profile bắt buộc
        String requiredProfile;
        if ("gaming".equals(useCase)) {
            requiredProfile = "Gaming " + resolution;
            sb.append("\n YÊU CẦU BẮT BUỘC:\n");
            sb.append("- profile PHẢI LÀ: \"").append(requiredProfile).append("\"\n");
            sb.append("- GPU giá từ ").append(formatCurrency(gpuTargetMin)).append(" đến ").append(formatCurrency(gpuTargetMax)).append("\n");
            sb.append("- TỔNG GIÁ từ ").append(formatCurrency(budget.multiply(BigDecimal.valueOf(0.70)))).append(" đến ").append(formatCurrency(budget)).append("\n");
            sb.append("- TỔNG GIÁ KHÔNG ĐƯỢC VƯỢT QUÁ ").append(formatCurrency(budget)).append("\n");
            sb.append("- Tất cả 'note' và 'reason' PHẢI BẰNG TIẾNG VIỆT\n");
            sb.append(" NGHIÊM CẤM: Không được trả về profile \"Office\" cho yêu cầu GAMING!\n");
            sb.append(" LƯU Ý: Đây là build GAMING, KHÔNG PHẢI văn phòng!\n");
        } else if ("creator".equals(useCase)) {
            requiredProfile = "Creator";
            sb.append("\nYÊU CẦU BẮT BUỘC:\n");
            sb.append("- profile PHẢI LÀ: \"").append(requiredProfile).append("\"\n");
            sb.append("- TỔNG GIÁ từ ").append(formatCurrency(budget.multiply(BigDecimal.valueOf(0.70)))).append(" đến ").append(formatCurrency(budget)).append("\n");
            sb.append("- TỔNG GIÁ KHÔNG ĐƯỢC VƯỢT QUÁ ").append(formatCurrency(budget)).append("\n");
            sb.append("- Tất cả 'note' và 'reason' PHẢI BẰNG TIẾNG VIỆT\n");
        } else {
            requiredProfile = "Office";
            sb.append("\n YÊU CẦU BẮT BUỘC:\n");
            sb.append("- profile PHẢI LÀ: \"").append(requiredProfile).append("\"\n");
            sb.append("- TỔNG GIÁ KHÔNG ĐƯỢC VƯỢT QUÁ ").append(formatCurrency(budget)).append("\n");
            sb.append("- Tất cả 'note' và 'reason' PHẢI BẰNG TIẾNG VIỆT\n");
        }
        
        if (StringUtils.hasText(request.getFormFactor())) {
            sb.append("- FORM FACTOR: ").append(request.getFormFactor().toUpperCase()).append("\n");
        }
        if (Boolean.TRUE.equals(request.getPreferQuiet())) {
            sb.append("- ƯU TIÊN: Máy mát/êm\n");
        }
        if (StringUtils.hasText(request.getDescription())) {
            sb.append("\n MÔ TẢ THÊM: \"").append(request.getDescription()).append("\"\n");
        }
        
        sb.append("\n═══════════════════════════════════════\n");
        sb.append(" DANH SÁCH SẢN PHẨM (CHỈ CHỌN TỪ ĐÂY):\n");
        sb.append("═══════════════════════════════════════\n");
        
        for (Map.Entry<String, List<ProductInfo>> entry : products.entrySet()) {
            sb.append("\n【").append(entry.getKey()).append("】\n");
            for (ProductInfo p : entry.getValue()) {
                sb.append("• ID: ").append(p.id)
                  .append(" | ").append(p.name)
                  .append(" | ").append(formatCurrency(p.price))
                  .append(" | Còn: ").append(p.stock).append("\n");
            }
        }
        
        sb.append("\n═══════════════════════════════════════\n");
        sb.append(" NHẮC LẠI: profile=\"").append(requiredProfile).append("\", TỔNG GIÁ <= ").append(formatCurrency(budget)).append("\n");
        
        return sb.toString();
    }

    private BuildSuggestResponse parseAiResponse(String aiResponse, BigDecimal budget, BuildSuggestRequest request) {
        try {
            // Trích xuất JSON từ response (có thể có text thừa)
            String jsonStr = extractJson(aiResponse);
            var responseMap = objectMapper.readValue(jsonStr, new TypeReference<Map<String, Object>>() {});
            
            String profile = (String) responseMap.getOrDefault("profile", "Custom Build");
            String note = (String) responseMap.getOrDefault("note", "");
            
            // VALIDATION: Đảm bảo profile khớp với useCase
            String useCase = normalizeUseCase(request.getUseCase());
            String resolution = request.getResolution() != null ? request.getResolution() : "1080p";
            String originalProfile = profile;
            profile = validateAndFixProfile(profile, useCase, resolution);
            
            // Log nếu profile bị sửa (AI trả về sai)
            if (!originalProfile.equalsIgnoreCase(profile)) {
                log.error("AI returned INCORRECT profile '{}' for useCase '{}'. Corrected to '{}'",
                    originalProfile, useCase, profile);
                // Vẫn dùng kết quả AI nhưng với profile đã sửa
            }
            
            // Validate và sửa note nếu không khớp với useCase
            note = validateAndFixNote(note, useCase);
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> partsRaw = (List<Map<String, Object>>) responseMap.get("parts");
            
            List<SuggestedPartDTO> parts = new ArrayList<>();
            BigDecimal estimatedTotal = BigDecimal.ZERO;
            
            if (partsRaw != null) {
                for (Map<String, Object> partMap : partsRaw) {
                    String productIdStr = (String) partMap.get("productId");
                    if (productIdStr == null) continue;
                    
                    try {
                        UUID productId = UUID.fromString(productIdStr);
                        ProductEntity product = productRepository.findById(productId).orElse(null);
                        
                        if (product != null) {
                            String productType = (String) partMap.get("productType");
                            String reason = (String) partMap.getOrDefault("reason", "AI gợi ý");
                            
                            // Validate và sửa reason nếu không khớp với useCase
                            reason = validateAndFixReason(reason, useCase, productType);
                            
                            SuggestedPartDTO part = SuggestedPartDTO.builder()
                                    .productType(productType)
                                    .productId(productId)
                                    .productName(product.getName())
                                    .brand(product.getBrandId() != null ? product.getBrandId().getName() : null)
                                    .price(product.getPrice())
                                    .stock(product.getStock())
                                    .reason(reason)
                                    .build();
                            parts.add(part);
                            if (product.getPrice() != null) {
                                estimatedTotal = estimatedTotal.add(product.getPrice());
                            }
                        }
                    } catch (IllegalArgumentException e) {
                        log.warn("Invalid product ID from AI: {}", productIdStr);
                    }
                }
            }
            
            // Kiểm tra tổng giá có hợp lý không (ít nhất 50% ngân sách cho gaming/creator)
            if (("gaming".equals(useCase) || "creator".equals(useCase)) && 
                estimatedTotal.compareTo(budget.multiply(BigDecimal.valueOf(0.50))) < 0) {
                log.warn("AI selected parts with total {} which is too low for budget {}. Using fallback.", 
                    estimatedTotal, budget);
                return null; // Trigger fallback
            }
            
            // Kiểm tra tổng giá không vượt quá ngân sách
            if (estimatedTotal.compareTo(budget) > 0) {
                log.warn("AI selected parts with total {} exceeds budget {}. Using fallback.", 
                    estimatedTotal, budget);
                return null; // Trigger fallback
            }
            
            return BuildSuggestResponse.builder()
                    .profile(profile)
                    .budgetInput(budget)
                    .estimatedTotal(estimatedTotal)
                    .note(note)
                    .parts(parts)
                    .build();
                    
        } catch (Exception e) {
            log.error("Failed to parse AI response: {}", e.getMessage());
            throw new RuntimeException("Cannot parse AI response", e);
        }
    }
    
    /**
     * Validate và sửa profile nếu AI trả về sai
     */
    private String validateAndFixProfile(String aiProfile, String useCase, String resolution) {
        String profileLower = aiProfile.toLowerCase();
        
        if ("gaming".equals(useCase)) {
            // Nếu AI trả về Office/Văn phòng cho gaming request -> sửa lại
            if (profileLower.contains("office") || profileLower.contains("văn phòng") || profileLower.contains("van phong")) {
                log.error("⛔ CRITICAL ERROR: AI returned WRONG profile '{}' for GAMING request! Fixing to Gaming {}", aiProfile, resolution);
                return "Gaming " + resolution;
            }
            // Nếu không chứa "gaming" -> thêm vào
            if (!profileLower.contains("gaming")) {
                log.warn("AI profile '{}' doesn't contain 'gaming', fixing to Gaming {}", aiProfile, resolution);
                return "Gaming " + resolution;
            }
        } else if ("creator".equals(useCase)) {
            if (!profileLower.contains("creator") && !profileLower.contains("đồ họa") && !profileLower.contains("do hoa")) {
                log.error("⛔ CRITICAL ERROR: AI returned WRONG profile '{}' for CREATOR request! Fixing to Creator", aiProfile);
                return "Creator";
            }
        } else if ("office".equals(useCase)) {
            if (profileLower.contains("gaming") || profileLower.contains("creator")) {
                log.error("⛔ CRITICAL ERROR: AI returned WRONG profile '{}' for OFFICE request! Fixing to Office", aiProfile);
                return "Office";
            }
        }
        
        return aiProfile;
    }
    
    /**
     * Validate và sửa reason của từng part nếu AI trả về sai useCase
     */
    private String validateAndFixReason(String reason, String useCase, String productType) {
        if (reason == null || reason.isEmpty()) {
            return getDefaultReason(useCase, productType);
        }
        
        String reasonLower = reason.toLowerCase();
        
        if ("gaming".equals(useCase)) {
            // Nếu reason chứa "văn phòng", "office", "học tập" cho gaming request -> sửa lại
            if (reasonLower.contains("văn phòng") || reasonLower.contains("van phong") || 
                reasonLower.contains("office") || reasonLower.contains("học tập") || 
                reasonLower.contains("hoc tap") || reasonLower.contains("general use")) {
                log.warn("⛔ Fixing WRONG reason '{}' for GAMING {} to gaming-appropriate reason", reason, productType);
                return getGamingReason(productType);
            }
        } else if ("creator".equals(useCase)) {
            // Nếu reason chứa "gaming" hoặc "văn phòng" cho creator request -> sửa lại
            if (reasonLower.contains("gaming") || reasonLower.contains("văn phòng") || reasonLower.contains("office")) {
                log.warn("⛔ Fixing WRONG reason '{}' for CREATOR {} to creator-appropriate reason", reason, productType);
                return getCreatorReason(productType);
            }
        } else if ("office".equals(useCase)) {
            // Nếu reason chứa "gaming" cho office request -> sửa lại
            if (reasonLower.contains("gaming") || reasonLower.contains("game")) {
                log.warn("⛔ Fixing WRONG reason '{}' for OFFICE {} to office-appropriate reason", reason, productType);
                return getOfficeReason(productType);
            }
        }
        
        return reason;
    }
    
    private String getDefaultReason(String useCase, String productType) {
        return switch (useCase) {
            case "gaming" -> getGamingReason(productType);
            case "creator" -> getCreatorReason(productType);
            default -> getOfficeReason(productType);
        };
    }
    
    private String getGamingReason(String productType) {
        if (productType == null) return "Phù hợp cho gaming";
        return switch (productType.toUpperCase()) {
            case "CPU" -> "CPU mạnh mẽ, phù hợp cho gaming và đa nhiệm";
            case "GPU" -> "GPU hiệu năng cao, chơi game mượt mà";
            case "RAM" -> "RAM đủ lớn cho gaming và chạy nhiều ứng dụng";
            case "MAINBOARD" -> "Mainboard chất lượng, hỗ trợ WiFi và nhiều cổng kết nối";
            case "STORAGE" -> "Tốc độ đọc/ghi nhanh, giảm thời gian load game";
            case "PSU" -> "Nguồn ổn định, đủ công suất cho cấu hình gaming";
            case "COOLER" -> "Tản nhiệt hiệu quả, giữ nhiệt độ ổn định khi gaming";
            case "CASE" -> "Case thoáng khí, thiết kế gaming đẹp mắt";
            case "MONITOR" -> "Màn hình tần số quét cao, phù hợp cho gaming";
            default -> "Phù hợp cho cấu hình gaming";
        };
    }
    
    private String getCreatorReason(String productType) {
        if (productType == null) return "Phù hợp cho đồ họa/render";
        return switch (productType.toUpperCase()) {
            case "CPU" -> "CPU nhiều nhân, mạnh mẽ cho render và xử lý video";
            case "GPU" -> "GPU VRAM cao, tăng tốc render và xử lý đồ họa";
            case "RAM" -> "RAM dung lượng lớn cho đa nhiệm và xử lý file nặng";
            case "MAINBOARD" -> "Mainboard ổn định, hỗ trợ nhiều khe RAM";
            case "STORAGE" -> "SSD tốc độ cao, giảm thời gian export/render";
            case "PSU" -> "Nguồn ổn định cho workstation";
            case "COOLER" -> "Tản nhiệt tốt cho CPU chạy render lâu dài";
            case "CASE" -> "Case thoáng khí, phù hợp workstation";
            case "MONITOR" -> "Màn hình màu chuẩn, phù hợp cho thiết kế đồ họa";
            default -> "Phù hợp cho công việc sáng tạo nội dung";
        };
    }
    
    private String getOfficeReason(String productType) {
        if (productType == null) return "Phù hợp cho văn phòng";
        return switch (productType.toUpperCase()) {
            case "CPU" -> "CPU tiết kiệm điện, đủ mạnh cho công việc văn phòng";
            case "GPU" -> "GPU tích hợp đủ dùng cho văn phòng";
            case "RAM" -> "RAM đủ cho đa nhiệm văn phòng";
            case "MAINBOARD" -> "Mainboard ổn định, tiết kiệm điện";
            case "STORAGE" -> "SSD tốc độ tốt, khởi động nhanh";
            case "PSU" -> "Nguồn tiết kiệm điện, ổn định";
            case "COOLER" -> "Tản nhiệt êm ái cho môi trường văn phòng";
            case "CASE" -> "Case nhỏ gọn, phù hợp văn phòng";
            case "MONITOR" -> "Màn hình rõ nét, bảo vệ mắt";
            default -> "Phù hợp cho công việc văn phòng";
        };
    }
    
    /**
     * Validate và sửa note nếu AI trả về sai useCase
     */
    private String validateAndFixNote(String note, String useCase) {
        if (note == null || note.isEmpty()) {
            return getDefaultNote(useCase);
        }
        
        String noteLower = note.toLowerCase();
        
        if ("gaming".equals(useCase)) {
            // Nếu note chứa "văn phòng", "office" cho gaming request -> sửa lại
            if (noteLower.contains("văn phòng") || noteLower.contains("van phong") || 
                noteLower.contains("office") || noteLower.contains("học tập")) {
                log.warn("⛔ Fixing WRONG note '{}' for GAMING to gaming-appropriate note", note);
                return "Cấu hình phù hợp cho gaming, chơi game mượt mà";
            }
        } else if ("creator".equals(useCase)) {
            if (noteLower.contains("gaming") || noteLower.contains("văn phòng") || noteLower.contains("office")) {
                log.warn("⛔ Fixing WRONG note '{}' for CREATOR to creator-appropriate note", note);
                return "Cấu hình phù hợp cho đồ họa, render và sáng tạo nội dung";
            }
        } else if ("office".equals(useCase)) {
            if (noteLower.contains("gaming") || noteLower.contains("game")) {
                log.warn("⛔ Fixing WRONG note '{}' for OFFICE to office-appropriate note", note);
                return "Cấu hình phù hợp cho công việc văn phòng và học tập";
            }
        }
        
        return note;
    }
    
    private String getDefaultNote(String useCase) {
        return switch (useCase) {
            case "gaming" -> "Cấu hình phù hợp cho gaming, chơi game mượt mà";
            case "creator" -> "Cấu hình phù hợp cho đồ họa, render và sáng tạo nội dung";
            default -> "Cấu hình phù hợp cho công việc văn phòng và học tập";
        };
    }

    private String extractJson(String text) {
        // Tìm JSON object trong response
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return text;
    }

    private BuildSuggestResponse fallbackSuggestion(BuildSuggestRequest request, BigDecimal budget, 
                                                     Map<String, List<ProductInfo>> availableProducts) {
        // Fallback về logic rule-based khi AI fail
        Profile profile = resolveProfile(request);
        List<SuggestedPartDTO> parts = new ArrayList<>();
        BigDecimal estimatedTotal = BigDecimal.ZERO;
        BigDecimal remainingBudget = budget;
        
        // Tính tổng giá sản phẩm rẻ nhất của mỗi loại
        BigDecimal minTotalPrice = BigDecimal.ZERO;
        for (List<ProductInfo> candidates : availableProducts.values()) {
            if (candidates != null && !candidates.isEmpty()) {
                BigDecimal minPrice = candidates.stream()
                        .map(p -> p.price)
                        .min(BigDecimal::compareTo)
                        .orElse(BigDecimal.ZERO);
                minTotalPrice = minTotalPrice.add(minPrice);
            }
        }
        
        boolean budgetTooLow = budget.compareTo(minTotalPrice) < 0;
        String notePrefix = budgetTooLow ? 
                "⚠️ Ngân sách " + formatCurrency(budget) + " không đủ để build PC hoàn chỉnh (tối thiểu cần " + formatCurrency(minTotalPrice) + "). Đây là cấu hình rẻ nhất có thể: " : 
                "";

        // Sắp xếp parts theo thứ tự ưu tiên (GPU trước cho gaming)
        List<Map.Entry<String, BigDecimal>> sortedParts = new ArrayList<>(profile.partShares.entrySet());
        sortedParts.sort((a, b) -> b.getValue().compareTo(a.getValue())); // Sắp xếp giảm dần theo share

        for (Map.Entry<String, BigDecimal> entry : sortedParts) {
            String typeName = entry.getKey();
            BigDecimal share = entry.getValue();
            if (share == null || share.compareTo(BigDecimal.ZERO) <= 0) continue;
            
            List<ProductInfo> candidates = availableProducts.get(typeName);
            
            if (candidates != null && !candidates.isEmpty()) {
                ProductInfo chosen;
                String reason;
                
                if (budgetTooLow) {
                    // Ngân sách quá thấp -> chọn sản phẩm rẻ nhất
                    chosen = candidates.stream()
                            .min(Comparator.comparing(p -> p.price))
                            .orElse(candidates.get(0));
                    reason = "Sản phẩm rẻ nhất trong danh mục";
                } else {
                    // Ngân sách đủ -> chọn sản phẩm tốt nhất trong khoảng target
                    BigDecimal targetPrice = budget.multiply(share).setScale(0, RoundingMode.HALF_UP);
                    BigDecimal maxAllowed = targetPrice.multiply(BigDecimal.valueOf(1.2)); // Cho phép vượt 20%
                    
                    // Ưu tiên sản phẩm đắt nhất trong khoảng cho phép (để có hiệu năng tốt nhất)
                    Optional<ProductInfo> bestInRange = candidates.stream()
                            .filter(p -> p.price.compareTo(maxAllowed) <= 0)
                            .max(Comparator.comparing(p -> p.price));
                    
                    if (bestInRange.isPresent()) {
                        chosen = bestInRange.get();
                        reason = "Hiệu năng tốt nhất trong ngân sách ~" + formatCurrency(targetPrice);
                    } else {
                        // Không có sản phẩm nào trong khoảng -> chọn rẻ nhất
                        chosen = candidates.stream()
                                .min(Comparator.comparing(p -> p.price))
                                .orElse(candidates.get(0));
                        reason = "Sản phẩm phù hợp ngân sách";
                    }
                }
                
                SuggestedPartDTO part = SuggestedPartDTO.builder()
                        .productType(typeName)
                        .productId(UUID.fromString(chosen.id))
                        .productName(chosen.name)
                        .brand(chosen.brand)
                        .price(chosen.price)
                        .stock(chosen.stock)
                        .reason(reason)
                        .build();
                parts.add(part);
                estimatedTotal = estimatedTotal.add(chosen.price);
                remainingBudget = remainingBudget.subtract(chosen.price);
            }
        }

        return BuildSuggestResponse.builder()
                .profile(profile.name)
                .budgetInput(budget)
                .estimatedTotal(estimatedTotal)
                .note(notePrefix + profile.note)
                .parts(parts)
                .build();
    }

    private BigDecimal sanitizeBudget(Long budget) {
        if (budget == null || budget <= 0) {
            return DEFAULT_BUDGET;
        }
        return BigDecimal.valueOf(budget);
    }

    private String normalizeUseCase(String useCase) {
        if (!StringUtils.hasText(useCase)) return "office";
        String lower = useCase.trim().toLowerCase(Locale.ROOT);
        if (lower.contains("game")) return "gaming";
        if (lower.contains("creat") || lower.contains("render") || lower.contains("edit")) return "creator";
        return "office";
    }

    private String formatCurrency(BigDecimal price) {
        if (price == null) return "N/A";
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        formatter.setMaximumFractionDigits(0);
        return formatter.format(price) + " VND";
    }

    private UUID resolveTypeId(String typeName) {
        // Lấy tất cả ProductType từ database
        List<ProductTypeEntity> allProductTypes = productTypeRepository.findAll();
        
        // Log để debug - dùng INFO để dễ thấy
        log.info("Looking for type: '{}' in available types: {}", 
            typeName, 
            allProductTypes.stream().map(ProductTypeEntity::getName).toList());
        
        // Tìm trực tiếp theo tên (exact match)
        UUID direct = productTypeRepository.findFirstByNameIgnoreCase(typeName)
                .map(ProductTypeEntity::getId)
                .orElse(null);
        if (direct != null) {
            log.info("Direct match found for '{}' -> ID: {}", typeName, direct);
            return direct;
        }

        Map<String, List<String>> aliases = getAliases();
        String normTarget = normalize(typeName);
        
        // Tạo map từ ID -> normalized name
        Map<UUID, String> typeIdToNormName = allProductTypes.stream()
                .collect(Collectors.toMap(ProductTypeEntity::getId, t -> normalize(t.getName())));
        
        // Tạo map từ ID -> original name để tìm kiếm contains
        Map<UUID, String> typeIdToOrigName = allProductTypes.stream()
                .collect(Collectors.toMap(ProductTypeEntity::getId, ProductTypeEntity::getName));

        // Tìm theo aliases
        List<String> targetAliases = aliases.getOrDefault(typeName.toUpperCase(), List.of(typeName));
        List<String> normalizedAliases = targetAliases.stream().map(this::normalize).toList();
        
        for (var entry : typeIdToNormName.entrySet()) {
            String normDbName = entry.getValue();
            // Kiểm tra exact match hoặc contains
            if (normalizedAliases.contains(normDbName) || 
                normalizedAliases.stream().anyMatch(a -> normDbName.contains(a) || a.contains(normDbName))) {
                log.info("Matched type '{}' to DB type '{}' (ID: {})", 
                    typeName, typeIdToOrigName.get(entry.getKey()), entry.getKey());
                return entry.getKey();
            }
        }
        
        // Fallback: tìm theo substring trong tên gốc
        for (var entry : typeIdToOrigName.entrySet()) {
            String dbName = entry.getValue().toLowerCase();
            String searchName = typeName.toLowerCase();
            if (dbName.contains(searchName) || searchName.contains(dbName)) {
                log.info("Fallback matched type '{}' to DB type '{}' (ID: {})", 
                    typeName, entry.getValue(), entry.getKey());
                return entry.getKey();
            }
        }
        
        log.warn("Could not find ProductType for '{}'. Available types: {}", 
            typeName, allProductTypes.stream().map(ProductTypeEntity::getName).toList());
        return null;
    }

    private Map<String, List<String>> getAliases() {
        // Aliases phải match với tên ProductType trong database
        // Dựa trên UI: CPU, MAINBOARD, RAM, Card Đồ Họa, Ổ Cứng, Nguồn (PSU), Tản Nhiệt, Vỏ Case, Màn Hình
        Map<String, List<String>> aliases = new HashMap<>();
        aliases.put("CPU", List.of("CPU"));
        aliases.put("MAINBOARD", List.of("MAINBOARD", "MB", "MOBO", "MAIN"));
        aliases.put("GPU", List.of("GPU", "VGA", "CARDDOHOA", "CARD DO HOA", "CARD ĐỒ HỌA", "Card Đồ Họa"));
        aliases.put("RAM", List.of("RAM"));
        aliases.put("STORAGE", List.of("STORAGE", "OCUNG", "O CUNG", "Ổ CỨNG", "Ổ Cứng", "SSD", "HDD"));
        aliases.put("PSU", List.of("PSU", "NGUON", "NGUỒN", "Nguồn", "NGUONPSU", "Nguồn (PSU)"));
        aliases.put("CASE", List.of("CASE", "VOCASE", "VO CASE", "VỎ CASE", "Vỏ Case"));
        aliases.put("COOLER", List.of("COOLER", "TANNHIET", "TAN NHIET", "TẢN NHIỆT", "Tản Nhiệt"));
        aliases.put("MONITOR", List.of("MONITOR", "MANHINH", "MAN HINH", "MÀN HÌNH", "Màn Hình"));
        return aliases;
    }

    private String normalize(String text) {
        if (text == null) return "";
        return Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replaceAll("[^A-Za-z0-9]", "")
                .toUpperCase(Locale.ROOT);
    }

    private Profile resolveProfile(BuildSuggestRequest request) {
        String useCase = normalizeUseCase(request.getUseCase());
        String resolution = request.getResolution() != null ? request.getResolution().toLowerCase() : "1080p";

        if ("gaming".equals(useCase)) {
            if (resolution.contains("1440")) {
                return new Profile("Gaming 1440p", "Ưu tiên GPU 12GB+ cho 2K",
                        shares(createPartShares(0.15, 0.07, 0.10, 0.35, 0.06, 0.05, 0.02, 0.03, 0.17)));
            }
            if (resolution.contains("4")) {
                return new Profile("Gaming 4K", "Ưu tiên GPU mạnh VRAM cao",
                        shares(createPartShares(0.14, 0.06, 0.10, 0.38, 0.05, 0.05, 0.01, 0.02, 0.19)));
            }
            return new Profile("Gaming 1080p", "Cân bằng CPU/GPU cho Full HD",
                    shares(createPartShares(0.15, 0.08, 0.10, 0.32, 0.07, 0.05, 0.03, 0.04, 0.16)));
        }

        if ("creator".equals(useCase)) {
            return new Profile("Đồ họa / Render", "Ưu tiên CPU nhiều nhân, GPU VRAM cao, màn hình chất lượng",
                    shares(createPartShares(0.20, 0.06, 0.12, 0.25, 0.12, 0.04, 0.02, 0.02, 0.17)));
        }

        return new Profile("Văn phòng / Học tập", "Dùng iGPU, ưu tiên SSD và RAM",
                shares(createPartShares(0.20, 0.15, 0.15, 0.00, 0.17, 0.08, 0.03, 0.04, 0.18)));
    }

    private Map<String, BigDecimal> createPartShares(double cpu, double mainboard, double ram, double gpu,
                                                      double storage, double psu, double cooler, double caseShare, double monitor) {
        Map<String, BigDecimal> shares = new LinkedHashMap<>();
        shares.put("CPU", bd(cpu));
        shares.put("MAINBOARD", bd(mainboard));
        shares.put("RAM", bd(ram));
        shares.put("GPU", bd(gpu));
        shares.put("STORAGE", bd(storage));
        shares.put("PSU", bd(psu));
        shares.put("COOLER", bd(cooler));
        shares.put("CASE", bd(caseShare));
        shares.put("MONITOR", bd(monitor));
        return shares;
    }

    private Map<String, BigDecimal> shares(Map<String, BigDecimal> input) {
        return new LinkedHashMap<>(input);
    }

    private BigDecimal bd(double value) {
        return BigDecimal.valueOf(value);
    }

    private record ProductInfo(String id, String name, String brand, BigDecimal price, Integer stock) {}

    private static class Profile {
        final String name;
        final String note;
        final Map<String, BigDecimal> partShares;

        Profile(String name, String note, Map<String, BigDecimal> partShares) {
            this.name = name;
            this.note = note;
            this.partShares = partShares;
        }
    }
}
