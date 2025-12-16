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
            "CPU", "MAINBOARD", "RAM", "GPU", "STORAGE", "PSU", "COOLER", "CASE"
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
            return parseAiResponse(aiResponse, budget, request);
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
            Bạn là chuyên gia tư vấn build PC. Nhiệm vụ của bạn là chọn linh kiện phù hợp nhất từ danh sách sản phẩm có sẵn dựa trên nhu cầu và ngân sách của khách hàng.
            
            NGUYÊN TẮC CHỌN LINH KIỆN:
            1. Gaming: Ưu tiên GPU mạnh (40-50% ngân sách), CPU cân bằng
            2. Đồ họa/Render: Ưu tiên CPU nhiều nhân, GPU VRAM cao, RAM 32GB+
            3. Văn phòng: CPU tích hợp đồ họa, không cần GPU rời, ưu tiên SSD
            
            PHÂN BỔ NGÂN SÁCH THAM KHẢO:
            - Gaming 1080p: CPU 18%, GPU 40%, RAM 12%, Mainboard 9%, Storage 8%, PSU 6%, Case 4%, Cooler 3%
            - Gaming 1440p/4K: CPU 18%, GPU 45-50%, RAM 12%, còn lại chia đều
            - Creator: CPU 25%, GPU 30%, RAM 15%, Storage 15%, còn lại chia đều
            - Office: CPU 25%, Mainboard 18%, RAM 18%, Storage 20%, PSU 10%, Case 5%, Cooler 4%
            
            TRẢ LỜI THEO ĐỊNH DẠNG JSON CHÍNH XÁC:
            {
                "profile": "Tên profile (VD: Gaming 1080p)",
                "note": "Ghi chú ngắn về cấu hình",
                "parts": [
                    {
                        "productType": "CPU",
                        "productId": "uuid-của-sản-phẩm",
                        "productName": "Tên sản phẩm",
                        "reason": "Lý do chọn"
                    }
                ]
            }
            
            CHỈ TRẢ VỀ JSON, KHÔNG CÓ TEXT KHÁC.
            """;
    }

    private String buildUserPrompt(BuildSuggestRequest request, BigDecimal budget, Map<String, List<ProductInfo>> products) {
        StringBuilder sb = new StringBuilder();
        sb.append("NHU CẦU KHÁCH HÀNG:\n");
        sb.append("- Mục đích sử dụng: ").append(normalizeUseCase(request.getUseCase())).append("\n");
        sb.append("- Độ phân giải: ").append(request.getResolution() != null ? request.getResolution() : "1080p").append("\n");
        sb.append("- Ngân sách: ").append(formatCurrency(budget)).append("\n");
        if (StringUtils.hasText(request.getFormFactor())) {
            sb.append("- Form factor: ").append(request.getFormFactor()).append("\n");
        }
        if (Boolean.TRUE.equals(request.getPreferQuiet())) {
            sb.append("- Ưu tiên: Máy mát/êm\n");
        }
        
        sb.append("\nDANH SÁCH SẢN PHẨM CÓ SẴN:\n");
        for (Map.Entry<String, List<ProductInfo>> entry : products.entrySet()) {
            sb.append("\n[").append(entry.getKey()).append("]\n");
            for (ProductInfo p : entry.getValue()) {
                sb.append("- ID: ").append(p.id)
                  .append(" | ").append(p.name)
                  .append(" | ").append(p.brand)
                  .append(" | Giá: ").append(formatCurrency(p.price))
                  .append(" | Còn: ").append(p.stock).append(" sp\n");
            }
        }
        
        sb.append("\nHãy chọn linh kiện phù hợp nhất cho khách hàng. Tổng giá không vượt quá ngân sách.");
        return sb.toString();
    }

    private BuildSuggestResponse parseAiResponse(String aiResponse, BigDecimal budget, BuildSuggestRequest request) {
        try {
            // Trích xuất JSON từ response (có thể có text thừa)
            String jsonStr = extractJson(aiResponse);
            var responseMap = objectMapper.readValue(jsonStr, new TypeReference<Map<String, Object>>() {});
            
            String profile = (String) responseMap.getOrDefault("profile", "Custom Build");
            String note = (String) responseMap.getOrDefault("note", "");
            
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
                            SuggestedPartDTO part = SuggestedPartDTO.builder()
                                    .productType((String) partMap.get("productType"))
                                    .productId(productId)
                                    .productName(product.getName())
                                    .brand(product.getBrandId() != null ? product.getBrandId().getName() : null)
                                    .price(product.getPrice())
                                    .stock(product.getStock())
                                    .reason((String) partMap.getOrDefault("reason", "AI gợi ý"))
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

        for (Map.Entry<String, BigDecimal> entry : profile.partShares.entrySet()) {
            String typeName = entry.getKey();
            BigDecimal share = entry.getValue();
            if (share == null || share.compareTo(BigDecimal.ZERO) <= 0) continue;
            
            BigDecimal targetPrice = budget.multiply(share).setScale(0, RoundingMode.HALF_UP);
            List<ProductInfo> candidates = availableProducts.get(typeName);
            
            if (candidates != null && !candidates.isEmpty()) {
                // Chọn sản phẩm có giá gần target nhất
                ProductInfo chosen = candidates.stream()
                        .min(Comparator.comparing(p -> p.price.subtract(targetPrice).abs()))
                        .orElse(candidates.get(0));
                
                SuggestedPartDTO part = SuggestedPartDTO.builder()
                        .productType(typeName)
                        .productId(UUID.fromString(chosen.id))
                        .productName(chosen.name)
                        .brand(chosen.brand)
                        .price(chosen.price)
                        .stock(chosen.stock)
                        .reason("Gần mức mục tiêu ~" + formatCurrency(targetPrice))
                        .build();
                parts.add(part);
                estimatedTotal = estimatedTotal.add(chosen.price);
            }
        }

        return BuildSuggestResponse.builder()
                .profile(profile.name + " (Fallback)")
                .budgetInput(budget)
                .estimatedTotal(estimatedTotal)
                .note(profile.note)
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
                        shares(Map.of("CPU", bd(0.18), "MAINBOARD", bd(0.08), "GPU", bd(0.44),
                                "RAM", bd(0.12), "STORAGE", bd(0.07), "PSU", bd(0.06), "CASE", bd(0.03), "COOLER", bd(0.02))));
            }
            if (resolution.contains("4")) {
                return new Profile("Gaming 4K", "Ưu tiên GPU mạnh VRAM cao",
                        shares(Map.of("CPU", bd(0.18), "MAINBOARD", bd(0.07), "GPU", bd(0.50),
                                "RAM", bd(0.12), "STORAGE", bd(0.05), "PSU", bd(0.05), "CASE", bd(0.02), "COOLER", bd(0.01))));
            }
            return new Profile("Gaming 1080p", "Cân bằng CPU/GPU cho Full HD",
                    shares(Map.of("CPU", bd(0.18), "MAINBOARD", bd(0.09), "GPU", bd(0.40),
                            "RAM", bd(0.12), "STORAGE", bd(0.08), "PSU", bd(0.06), "CASE", bd(0.04), "COOLER", bd(0.03))));
        }

        if ("creator".equals(useCase)) {
            return new Profile("Đồ họa / Render", "Ưu tiên CPU nhiều nhân, GPU VRAM cao",
                    shares(Map.of("CPU", bd(0.25), "MAINBOARD", bd(0.07), "GPU", bd(0.30),
                            "RAM", bd(0.15), "STORAGE", bd(0.15), "PSU", bd(0.04), "CASE", bd(0.02), "COOLER", bd(0.02))));
        }

        return new Profile("Văn phòng / Học tập", "Dùng iGPU, ưu tiên SSD và RAM",
                shares(Map.of("CPU", bd(0.25), "MAINBOARD", bd(0.18), "GPU", bd(0.00),
                        "RAM", bd(0.18), "STORAGE", bd(0.20), "PSU", bd(0.10), "CASE", bd(0.05), "COOLER", bd(0.04))));
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
