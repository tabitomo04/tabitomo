package com.koreatravel.tabitomo.domain.dto.member;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LanguageDTO {
    private Integer languageId;
    private String nameNative;
    private String nameEn;
}