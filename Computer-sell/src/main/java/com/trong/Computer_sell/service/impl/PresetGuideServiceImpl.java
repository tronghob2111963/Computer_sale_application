package com.trong.Computer_sell.service.impl;

import com.trong.Computer_sell.DTO.response.build.PresetSectionDTO;
import com.trong.Computer_sell.service.PresetGuideService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PresetGuideServiceImpl implements PresetGuideService {

    private static final String GUIDE_PATH = "PC_BUILD_PRESET_GUIDE.md";

    @Override
    public String getGuideContent() {
        try {
            return Files.readString(Path.of(GUIDE_PATH));
        } catch (IOException e) {
            log.error("Không thể đọc file hướng dẫn preset: {}", GUIDE_PATH, e);
            return "";
        }
    }

    @Override
    public List<PresetSectionDTO> getSections() {
        String content = getGuideContent();
        if (content.isBlank()) {
            return List.of();
        }

        List<PresetSectionDTO> sections = new ArrayList<>();
        String[] lines = content.split("\\r?\\n");
        String currentTitle = null;
        List<String> currentBody = new ArrayList<>();

        for (String line : lines) {
            if (line.startsWith("###")) {
                // flush previous
                if (currentTitle != null) {
                    sections.add(PresetSectionDTO.builder()
                            .title(currentTitle.trim())
                            .summary(buildSummary(currentBody))
                            .build());
                }
                currentTitle = line.replace("###", "").trim();
                currentBody.clear();
            } else if (!line.startsWith("##")) {
                if (!line.trim().isEmpty()) {
                    currentBody.add(line.trim());
                }
            }
        }

        if (currentTitle != null) {
            sections.add(PresetSectionDTO.builder()
                    .title(currentTitle.trim())
                    .summary(buildSummary(currentBody))
                    .build());
        }

        return sections;
    }

    private String buildSummary(List<String> body) {
        String joined = body.stream().limit(4).collect(Collectors.joining(" "));
        return joined.length() > 300 ? joined.substring(0, 300) + "..." : joined;
    }
}
