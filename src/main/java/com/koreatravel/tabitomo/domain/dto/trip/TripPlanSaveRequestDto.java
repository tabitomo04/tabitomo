package com.koreatravel.tabitomo.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TripPlanSaveRequestDto {
    private String planName;
    private String planDetails;
}
