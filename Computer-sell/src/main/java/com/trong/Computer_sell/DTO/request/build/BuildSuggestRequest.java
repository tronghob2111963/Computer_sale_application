package com.trong.Computer_sell.DTO.request.build;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BuildSuggestRequest {
    /**
     * Nhu cầu chính: gaming | office | creator | mixed
     */
    private String useCase;

    /**
     * Độ phân giải mục tiêu khi gaming: 1080p | 1440p | 4k
     */
    private String resolution;

    /**
     * Ngân sách mong muốn (VND)
     */
    private Long budget;

    /**
     * Form factor ưu tiên: itx | matx | atx
     */
    private String formFactor;

    /**
     * Ưu tiên máy êm/ mát
     */
    private Boolean preferQuiet;
}
