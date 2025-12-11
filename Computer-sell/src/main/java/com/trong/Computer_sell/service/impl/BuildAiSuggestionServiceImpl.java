package com.trong.Computer_sell.service.impl;

import com.trong.Computer_sell.DTO.request.build.BuildSuggestRequest;
import com.trong.Computer_sell.DTO.response.build.BuildSuggestResponse;
import com.trong.Computer_sell.DTO.response.build.SuggestedPartDTO;
import com.trong.Computer_sell.model.ProductEntity;
import com.trong.Computer_sell.model.ProductTypeEntity;
import com.trong.Computer_sell.repository.ProductRepository;
import com.trong.Computer_sell.repository.ProductTypeRepository;
import com.trong.Computer_sell.service.BuildAiSuggestionService;
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
    private static final int TOP_N = 8; // lấy rộng hơn để dễ có kết quả

    private final ProductRepository productRepository;
    private final ProductTypeRepository productTypeRepository;
    private final PresetGuideService presetGuideService;

    @Override
    public BuildSuggestResponse suggest(BuildSuggestRequest request) {
        BigDecimal budget = sanitizeBudget(request.getBudget());
        Profile profile = resolveProfile(request);

        List<SuggestedPartDTO> parts = new ArrayList<>();
        BigDecimal estimatedTotal = BigDecimal.ZERO;

        for (Map.Entry<String, BigDecimal> entry : profile.partShares.entrySet()) {
            String typeName = entry.getKey();
            BigDecimal share = entry.getValue();
            if (share == null || share.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            BigDecimal targetPrice = budget.multiply(share).setScale(0, RoundingMode.HALF_UP);
            SuggestedPartDTO part = pickPart(typeName, targetPrice, request);
            if (part != null) {
                parts.add(part);
                if (part.getPrice() != null) {
                    estimatedTotal = estimatedTotal.add(part.getPrice());
                }
            }
        }

        String note = profile.note;
        var matchedSection = presetGuideService.getSections().stream()
                .filter(sec -> sec.getTitle().toLowerCase(Locale.ROOT).contains(profile.name.toLowerCase(Locale.ROOT)))
                .findFirst()
                .orElse(null);
        if (matchedSection != null) {
            note = note + " | Theo preset: " + matchedSection.getTitle();
        }
        if (StringUtils.hasText(request.getFormFactor())) {
            note = note + " | Uu tien form factor: " + request.getFormFactor();
        }
        if (Boolean.TRUE.equals(request.getPreferQuiet())) {
            note = note + " | Uu tien dan may em/mat (case airflow, quat RPM thap).";
        }

        return BuildSuggestResponse.builder()
                .profile(profile.name)
                .budgetInput(budget)
                .estimatedTotal(estimatedTotal)
                .note(note)
                .parts(parts)
                .build();
    }

    @Override
    public Object getPresetGuide() {
        return presetGuideService.getSections();
    }

    private SuggestedPartDTO pickPart(String typeName, BigDecimal targetPrice, BuildSuggestRequest request) {
        UUID typeId = resolveTypeId(typeName);
        if (typeId == null) {
            log.warn("Khong tim thay ProductType '{}' (alias)", typeName);
            return pickByKeywordFallback(typeName, targetPrice, request);
        }

        var pageRequest = PageRequest.of(0, TOP_N);
        List<ProductEntity> candidates = productRepository
                .suggestByTypeAndMaxPrice(typeId, targetPrice, pageRequest)
                .getContent();

        if (candidates.isEmpty()) {
            candidates = productRepository.findCheapestByType(typeId, pageRequest).getContent();
        }

        ProductEntity chosen = chooseAvailable(candidates);
        if (chosen == null) {
            log.warn("Khong co san pham kha dung cho loai {}", typeName);
            return pickByKeywordFallback(typeName, targetPrice, request);
        }

        StringBuilder reason = new StringBuilder("Gan muc muc tieu ~")
                .append(formatCurrency(targetPrice));
        if ("GPU".equalsIgnoreCase(typeName)) {
            reason.append(" phu hop nhu cau ").append(normalizeUseCase(request.getUseCase()));
        }
        if (Boolean.TRUE.equals(request.getPreferQuiet())
                && ("CASE".equalsIgnoreCase(typeName) || "COOLER".equalsIgnoreCase(typeName))) {
            reason.append("; uu tien mat/em");
        }

        return SuggestedPartDTO.builder()
                .productType(typeName)
                .productId(chosen.getId())
                .productName(chosen.getName())
                .brand(chosen.getBrandId() != null ? chosen.getBrandId().getName() : null)
                .price(chosen.getPrice())
                .stock(chosen.getStock())
                .reason(reason.toString())
                .build();
    }

    private ProductEntity chooseAvailable(List<ProductEntity> candidates) {
        return candidates.stream()
                .filter(p -> p.getStock() != null && p.getStock() > 0)
                .findFirst()
                .orElse(candidates.isEmpty() ? null : candidates.get(0));
    }

    private SuggestedPartDTO pickByKeywordFallback(String typeName, BigDecimal targetPrice, BuildSuggestRequest request) {
        try {
            List<String> keywords = new ArrayList<>();
            keywords.add(typeName);
            keywords.addAll(getAliases().getOrDefault(typeName.toUpperCase(Locale.ROOT), List.of()));

            for (String kw : keywords) {
                var page = productRepository.searchUserByKeyword(kw, PageRequest.of(0, TOP_N));
                if (page == null || page.isEmpty()) {
                    continue;
                }
                ProductEntity chosen = chooseAvailable(page.getContent());
                if (chosen == null) {
                    continue;
                }

                StringBuilder reason = new StringBuilder("Fallback tu keyword '")
                        .append(kw)
                        .append("' ~ ")
                        .append(formatCurrency(targetPrice));
                if ("GPU".equalsIgnoreCase(typeName)) {
                    reason.append(" phu hop nhu cau ").append(normalizeUseCase(request.getUseCase()));
                }

                return SuggestedPartDTO.builder()
                        .productType(typeName)
                        .productId(chosen.getId())
                        .productName(chosen.getName())
                        .brand(chosen.getBrandId() != null ? chosen.getBrandId().getName() : null)
                        .price(chosen.getPrice())
                        .stock(chosen.getStock())
                        .reason(reason.toString())
                        .build();
            }
            return null;
        } catch (Exception e) {
            log.error("Fallback keyword failed for type {}", typeName, e);
            return null;
        }
    }

    private BigDecimal sanitizeBudget(Long budget) {
        if (budget == null || budget <= 0) {
            return DEFAULT_BUDGET;
        }
        return BigDecimal.valueOf(budget);
    }

    private Profile resolveProfile(BuildSuggestRequest request) {
        String useCase = normalizeUseCase(request.getUseCase());
        String resolution = normalizeResolution(request.getResolution());

        if ("gaming".equals(useCase)) {
            switch (resolution) {
                case "1440p":
                    return new Profile(
                            "Gaming 1440p",
                            "Uu tien GPU 12GB+ cho 2K, can bang CPU tam trung.",
                            shares(Map.of(
                                    "CPU", bd(0.18),
                                    "MAINBOARD", bd(0.08),
                                    "GPU", bd(0.44),
                                    "RAM", bd(0.12),
                                    "STORAGE", bd(0.07),
                                    "PSU", bd(0.06),
                                    "CASE", bd(0.03),
                                    "COOLER", bd(0.02)
                            ))
                    );
                case "4k":
                    return new Profile(
                            "Gaming 4K",
                            "Uu tien GPU manh (VRAM cao), CPU tam trung-kha.",
                            shares(Map.of(
                                    "CPU", bd(0.18),
                                    "MAINBOARD", bd(0.07),
                                    "GPU", bd(0.50),
                                    "RAM", bd(0.12),
                                    "STORAGE", bd(0.05),
                                    "PSU", bd(0.05),
                                    "CASE", bd(0.02),
                                    "COOLER", bd(0.01)
                            ))
                    );
                default:
                    return new Profile(
                            "Gaming 1080p",
                            "Can bang CPU/GPU cho Full HD, uu tien VRAM 8-12GB.",
                            shares(Map.of(
                                    "CPU", bd(0.18),
                                    "MAINBOARD", bd(0.09),
                                    "GPU", bd(0.40),
                                    "RAM", bd(0.12),
                                    "STORAGE", bd(0.08),
                                    "PSU", bd(0.06),
                                    "CASE", bd(0.04),
                                    "COOLER", bd(0.03)
                            ))
                    );
            }
        }

        if ("creator".equals(useCase)) {
            return new Profile(
                    "Do hoa / Dung phim",
                    "Uu tien CPU nhieu nhan va GPU VRAM cao, RAM 32GB+ neu ngan sach cho phep.",
                    shares(Map.of(
                            "CPU", bd(0.25),
                            "MAINBOARD", bd(0.07),
                            "GPU", bd(0.30),
                            "RAM", bd(0.15),
                            "STORAGE", bd(0.15),
                            "PSU", bd(0.04),
                            "CASE", bd(0.02),
                            "COOLER", bd(0.02)
                    ))
            );
        }

        // Office / mixed
        return new Profile(
                "Van phong / Hoc tap",
                "Dung iGPU hoac GPU co ban, uu tien SSD va RAM cho da nhiem.",
                shares(Map.of(
                        "CPU", bd(0.25),
                        "MAINBOARD", bd(0.18),
                        "GPU", bd(0.00),
                        "RAM", bd(0.18),
                        "STORAGE", bd(0.20),
                        "PSU", bd(0.10),
                        "CASE", bd(0.05),
                        "COOLER", bd(0.04)
                ))
        );
    }

    private Map<String, BigDecimal> shares(Map<String, BigDecimal> input) {
        return new LinkedHashMap<>(input);
    }

    private String normalizeUseCase(String useCase) {
        if (!StringUtils.hasText(useCase)) {
            return "office";
        }
        String lower = useCase.trim().toLowerCase(Locale.ROOT);
        if (lower.contains("game")) return "gaming";
        if (lower.contains("creat") || lower.contains("render") || lower.contains("edit")) return "creator";
        return "office";
    }

    private String normalizeResolution(String resolution) {
        if (!StringUtils.hasText(resolution)) {
            return "1080p";
        }
        String lower = resolution.trim().toLowerCase(Locale.ROOT);
        if (lower.contains("1440")) return "1440p";
        if (lower.contains("4")) return "4k";
        return "1080p";
    }

    private BigDecimal bd(double value) {
        return BigDecimal.valueOf(value);
    }

    private String formatCurrency(BigDecimal price) {
        if (price == null) {
            return "N/A";
        }
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        formatter.setMaximumFractionDigits(0);
        return formatter.format(price) + " VND";
    }

    private UUID resolveTypeId(String typeName) {
        UUID direct = productTypeRepository.findFirstByNameIgnoreCase(typeName)
                .map(ProductTypeEntity::getId)
                .orElse(null);
        if (direct != null) return direct;

        Map<String, List<String>> aliases = getAliases();
        String normTarget = normalize(typeName);
        Map<UUID, String> allTypes = productTypeRepository.findAll().stream()
                .collect(Collectors.toMap(ProductTypeEntity::getId, t -> normalize(t.getName())));

        for (var aliasEntry : aliases.entrySet()) {
            String base = normalize(aliasEntry.getKey());
            List<String> variants = aliasEntry.getValue().stream().map(this::normalize).toList();
            if (normTarget.equals(base) || variants.contains(normTarget)) {
                for (var kv : allTypes.entrySet()) {
                    if (kv.getValue().equals(base) || variants.contains(kv.getValue())) {
                        return kv.getKey();
                    }
                }
            }
        }

        for (var kv : allTypes.entrySet()) {
            if (kv.getValue().contains(normTarget) || normTarget.contains(kv.getValue())) {
                return kv.getKey();
            }
        }

        log.warn("Alias lookup failed for '{}', available types: {}", typeName, allTypes.values());
        return null;
    }

    private Map<String, List<String>> getAliases() {
        return Map.of(
                "CPU", List.of("CPU"),
                "MAINBOARD", List.of("MAINBOARD", "MB", "MOBO", "BO MACH CHU", "BO MẠCH CHỦ"),
                "GPU", List.of("GPU", "VGA", "CARD DO HOA", "CARD ĐỒ HỌA", "CARD MAN HINH", "CARD MÀN HÌNH"),
                "RAM", List.of("RAM"),
                "STORAGE", List.of("STORAGE", "O CUNG", "Ổ CỨNG", "SSD", "HDD", "Luu tru", "LƯU TRỮ"),
                "PSU", List.of("PSU", "NGUON", "NGUỒN", "POWER SUPPLY", "POWER"),
                "CASE", List.of("CASE", "VO CASE", "VỎ CASE", "VO MAY", "VỎ MÁY"),
                "COOLER", List.of("COOLER", "TAN NHIET", "TẢN NHIỆT", "TAN NHIET CPU", "HE THONG TAN", "HỆ THỐNG TẢN"),
                "MONITOR", List.of("MONITOR", "MAN HINH", "MÀN HÌNH")
        );
    }

    private String normalize(String text) {
        if (text == null) return "";
        return Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replaceAll("[^A-Za-z0-9]", "")
                .toUpperCase(Locale.ROOT);
    }

    private static class Profile {
        private final String name;
        private final String note;
        private final Map<String, BigDecimal> partShares;

        private Profile(String name, String note, Map<String, BigDecimal> partShares) {
            this.name = name;
            this.note = note;
            this.partShares = partShares;
        }
    }
}
