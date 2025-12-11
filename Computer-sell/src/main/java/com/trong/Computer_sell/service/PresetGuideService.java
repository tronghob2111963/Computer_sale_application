package com.trong.Computer_sell.service;

import com.trong.Computer_sell.DTO.response.build.PresetSectionDTO;

import java.util.List;

public interface PresetGuideService {
    String getGuideContent();
    List<PresetSectionDTO> getSections();
}
