package com.koreatravel.tabitomo.domain.dto.trip;

import com.koreatravel.tabitomo.domain.entity.trip.PlaceEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TourRecommendationDTO {
    private String title;
    private List<ItineraryItem> itinerary = new ArrayList<>();
    private AccommodationDTO accommodation;
    private String summary;
    private List<String> tips = new ArrayList<>();
    
    // For backward compatibility
    private List<ItineraryItem> accommodations = new ArrayList<>();

    public TourRecommendationDTO(String title, List<ScheduleInfoDTO> scheduleInfos, PlaceEntity placeEntity) {
        this.title = title;
        this.itinerary = new ArrayList<>();
        // Convert ScheduleInfo to ItineraryItem
        if (scheduleInfos != null) {
            for (ScheduleInfoDTO info : scheduleInfos) {
                ItineraryItem item = new ItineraryItem();
                item.setDay(info.getDay());
                if (info.getPlace() != null) {
                    item.setPlace(info.getPlace().getName());
                    item.setPlaceName(info.getPlace().getName());
                    // If you need latitude and longitude, you'll need to get the place entity first
                    // item.setLatitude(placeEntity.getLatitude());
                    // item.setLongitude(placeEntity.getLongitude());
                }
                item.setTime(info.getStartTime() + " - " + info.getEndTime());
                item.setDescription(info.getMemo());
                this.itinerary.add(item);
            }
        }
        
        // Convert PlaceEntity to AccommodationDTO
        if (placeEntity != null) {
            this.accommodation = new AccommodationDTO();
            this.accommodation.setPlaceName(placeEntity.getName());
            this.accommodation.setDescription(placeEntity.getDescription());
            this.accommodation.setImageUrl(placeEntity.getImageUrl());
            this.accommodation.setLatitude(placeEntity.getLatitude());
            this.accommodation.setLongitude(placeEntity.getLongitude());
            this.accommodation.setAddress(placeEntity.getAddress());
        }
    }
    
    public TourRecommendationDTO(String title, List<ItineraryItem> itinerary) {
        this.title = title;
        this.itinerary = itinerary != null ? new ArrayList<ItineraryItem>(itinerary) : new ArrayList<ItineraryItem>();
    }
    
    public TourRecommendationDTO(String title, List<ItineraryItem> itinerary, Object accommodation) {
        this.title = title;
        this.itinerary = itinerary != null ? new ArrayList<>(itinerary) : new ArrayList<>();
        this.accommodations = new ArrayList<>();
        
        if (accommodation != null) {
            if (accommodation instanceof PlaceEntity) {
                PlaceEntity place = (PlaceEntity) accommodation;
                this.accommodation = new AccommodationDTO();
                this.accommodation.setPlaceName(place.getName());
                this.accommodation.setDescription(place.getDescription());
                this.accommodation.setImageUrl(place.getImageUrl());
                this.accommodation.setLatitude(place.getLatitude());
                this.accommodation.setLongitude(place.getLongitude());
                this.accommodation.setAddress(place.getAddress());
            } else if (accommodation instanceof AccommodationDTO) {
                this.accommodation = (AccommodationDTO) accommodation;
            }
        }
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
