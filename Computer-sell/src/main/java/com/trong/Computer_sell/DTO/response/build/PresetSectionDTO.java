package com.trong.Computer_sell.DTO.response.build;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresetSectionDTO {
    private String title;
    private String summary;
}
