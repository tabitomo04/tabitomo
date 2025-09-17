package com.koreatravel.tabitomo.domain.dto.member;

import lombok.Data;

@Data
public class CountryDTO {
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