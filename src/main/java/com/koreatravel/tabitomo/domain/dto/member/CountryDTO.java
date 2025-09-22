package com.koreatravel.tabitomo.domain.dto.member;

import lombok.Data;
import java.io.Serializable;

@Data
public class CountryDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private int countryId;
    private String countryCode;
    private String countryNameKo;
    private String countryNameEn;

    public CountryDTO(int countryId, String countryCode, String countryNameKo, String countryNameEn) {
        this.countryId = countryId;
        this.countryCode = countryCode;
        this.countryNameKo = countryNameKo;
        this.countryNameEn = countryNameEn;
    }
}