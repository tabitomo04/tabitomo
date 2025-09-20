package com.koreatravel.tabitomo.domain.dto.trip;

import com.koreatravel.tabitomo.domain.entity.trip.Place;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TourRecommendation implements Serializable {
    private static final long serialVersionUID = 1L;

    private String title;
    private List<ScheduleInfo> itinerary;
    private Place accommodation;
}
