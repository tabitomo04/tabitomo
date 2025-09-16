package com.koreatravel.tabitomo.domain.dto.member;

import lombok.Data;

@Data
public class LanguageDTO {
    private int languageId;
    private String nameNative;
    private String nameEn;

    public LanguageDTO(int languageId, String nameNative, String nameEn) {
        this.languageId = languageId;
        this.nameNative = nameNative;
        this.nameEn = nameEn;
    }
}