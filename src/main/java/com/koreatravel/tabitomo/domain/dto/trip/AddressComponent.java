package com.koreatravel.tabitomo.domain.dto.trip;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AddressComponent {
    private String region; // 광역시/도
    private String city;   // 시/군/구
}
