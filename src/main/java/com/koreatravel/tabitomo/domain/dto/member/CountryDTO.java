package com.koreatravel.tabitomo.domain.dto.member;

import lombok.Data;

@Data
public class CountryDTO {
    private int countryId;
    private String countryNameKo;
    private String countryNameEn;

    public CountryDTO(int countryId, String countryNameKo, String countryNameEn) {
        this.countryId = countryId;
        this.countryNameKo = countryNameKo;
        this.countryNameEn = countryNameEn;
    }
}