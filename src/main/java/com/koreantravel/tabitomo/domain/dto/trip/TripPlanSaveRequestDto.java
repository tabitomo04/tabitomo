package com.koreantravel.tabitomo.domain.dto.trip;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TripPlanSaveRequestDto {
    private String planName;
    private String planDetails;
}
