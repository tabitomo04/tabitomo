package com.koreatravel.tabitomo.domain.dto.trip;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
public class TripPlan {
    private UUID id; // 여행 계획 ID
    private String planName; // 여행 계획 이름
    private String destination;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<String> styles;
    private List<String> facilities; // 숙소 부대시설
    private TourRecommendation tourRecommendation; // 추천 결과
    private Map<String, Object> additionalInfo;
    private List<DailySchedule> dailySchedules;
    private Accommodation accommodation; // 숙소 정보
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
        private String startTime;
        private String endTime;
        private String place;
        private String description;
        private Double latitude;
        private Double longitude;
        private String address; // 주소 필드 추가
    }

    @Data
    public static class Accommodation {
        private String placeName;
        private String description;
        private String priceRange;
        private String imageUrl;
        private Double latitude;
        private Double longitude;
        private String address; // 주소 필드 추가
    }
}
