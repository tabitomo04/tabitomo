package com.koreatravel.tabitomo.domain.dto.member;

import lombok.Data;
import java.io.Serializable;

@Data
public class LanguageDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private int languageId;
    private String nameNative;
    private String nameEn;

    public LanguageDTO(int languageId, String nameNative, String nameEn) {
        this.languageId = languageId;
        this.nameNative = nameNative;
        this.nameEn = nameEn;
    }
}