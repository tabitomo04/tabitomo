package com.koreatravel.tabitomo.domain.dto.trip;

import com.koreatravel.tabitomo.domain.entity.trip.PlaceEntity;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TourRecommendationDTO {
    private String title;
    private List<ItineraryItem> itinerary = new ArrayList<>();
    private PlaceEntity accommodation;
    private String summary;
    private List<String> tips = new ArrayList<>();
    
    // For backward compatibility
    private List<ItineraryItem> accommodations = new ArrayList<>();

    public TourRecommendationDTO() {}
    
    public TourRecommendationDTO(String title, List<ItineraryItem> itinerary) {
        this.title = title;
        this.itinerary = itinerary != null ? new ArrayList<>(itinerary) : new ArrayList<>();
        this.accommodations = new ArrayList<>();
    }
    
    public TourRecommendationDTO(String title, List<ItineraryItem> itinerary, PlaceEntity accommodation) {
        this.title = title;
        this.itinerary = itinerary != null ? new ArrayList<>(itinerary) : new ArrayList<>();
        this.accommodation = accommodation;
        this.accommodations = new ArrayList<>();
    }
    
    @Data
    public static class ItineraryItem {
        private int day;
        private String time;
        private String place;
        private String placeName;
        private String description;
        private double latitude;
        private double longitude;
        private String restDate;
        private String useTime;
        private String imageUrl;
        
        public ItineraryItem() {}
        
        public ItineraryItem(int day, String time, String place, String description, 
                           double latitude, double longitude) {
            this.day = day;
            this.time = time;
            this.place = place;
            this.placeName = place; // Default placeName to place
            this.description = description;
            this.latitude = latitude;
            this.longitude = longitude;
        }
        
        // Additional setters used in GeminiAIService
        public void setRestDate(String restDate) {
            this.restDate = restDate;
        }
        
        public void setUseTime(String useTime) {
            this.useTime = useTime;
        }
        
        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }
    }
}
