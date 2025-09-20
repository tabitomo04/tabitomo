package com.koreatravel.tabitomo.domain.dto.trip;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
public class TripPlanDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id; // 여행 계획 ID
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
    public static class DailySchedule implements Serializable {
        private static final long serialVersionUID = 1L;
        private int day;
        private List<ScheduleItem> schedules;
        private List<String> places;
    }

    @Data
    public static class ScheduleItem implements Serializable {
        private static final long serialVersionUID = 1L;
        private String startTime;
        private String endTime;
        private String place;
        private String description;
        private Double latitude;
        private Double longitude;
        private String address; // 주소 필드 추가
    }

    @Data
    public static class Accommodation implements Serializable {
        private static final long serialVersionUID = 1L;
        private String placeName;
        private String description;
        private String priceRange;
        private String imageUrl;
        private Double latitude;
        private Double longitude;
        private String address; // 주소 필드 추가
    }
}
