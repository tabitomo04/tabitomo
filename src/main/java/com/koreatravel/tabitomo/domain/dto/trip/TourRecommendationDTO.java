package com.koreatravel.tabitomo.domain.dto;

import java.util.Collections;
import java.util.List;

import com.koreatravel.tabitomo.domain.JSON.ItineraryItem;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TourRecommendationDTO {
    private String title;
    private List<com.koreatravel.tabitomo.domain.JSON.ItineraryItem> itinerary;
    private List<ItineraryItem> accommodations;

    public TourRecommendationDTO(String title, List<ItineraryItem> itinerary) {
        this.title = title;
        this.itinerary = itinerary;
        this.accommodations = Collections.emptyList();
    }
}
