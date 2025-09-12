package com.koreatravel.tabitomo.domain.dto.reference;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LanguageDTO {
    private Integer languageId;
    private String languageCode;
    private String nameNative;
    private String nameEn;
}
