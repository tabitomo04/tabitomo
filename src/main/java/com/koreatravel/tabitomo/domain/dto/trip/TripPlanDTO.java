package com.koreatravel.tabitomo.domain.dto;


import lombok.Data;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
public class TripPlanDTO {
    private String destination;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<String> styles;
    private List<String> facilities; // 숙소 부대시설
    private TourRecommendationDTO tourRecommendation; // 추천 결과
    private Map<String, Object> additionalInfo;
    private List<DailySchedule> dailySchedules;
    private List<Accommodation> accommodations; // 숙소 정보
    private int duration;
    private Integer accommodationBudget;
    private String budget;


    @Data
    public static class DailySchedule {
        private int day;
        private List<ScheduleItem> schedules;
        private List<String> places;
    }

    @Data
    public static class ScheduleItem {
        private String time;
        private String place;
        private String description;
        private double latitude;
        private double longitude;
    }

    @Data
    public static class Accommodation {
        private int day;
        private String placeName;
        private String description;
        private String priceRange;
        private String imageUrl;
        private Double latitude;
        private Double longitude;
    }
}
