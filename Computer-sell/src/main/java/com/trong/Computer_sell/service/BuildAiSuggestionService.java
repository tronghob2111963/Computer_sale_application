package com.trong.Computer_sell.service;

import com.trong.Computer_sell.DTO.request.build.BuildSuggestRequest;
import com.trong.Computer_sell.DTO.response.build.BuildSuggestResponse;

public interface BuildAiSuggestionService {
    BuildSuggestResponse suggest(BuildSuggestRequest request);
    Object getPresetGuide();
}
