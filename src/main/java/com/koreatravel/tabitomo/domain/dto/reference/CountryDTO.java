package com.koreatravel.tabitomo.domain.dto.reference;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CountryDTO {
    private Integer countryId;
    private String countryName;
    private String countryCode;
    private String isoCode;
    private String region;
}
