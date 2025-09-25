package com.koreatravel.tabitomo.domain.dto.trip;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TripRegion {
    private Long tripId;
    private String title;
    private String region;
}
