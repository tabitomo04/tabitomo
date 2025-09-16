package com.koreatravel.tabitomo.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.koreatravel.tabitomo.domain.dto.trip.TripPlan;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class TripPlanParser {

    private final ObjectMapper objectMapper;
    
    private static final Pattern DAY_PATTERN = Pattern.compile("### \\[Day (\\d+)\\] (.*?)(?=### \\[Day \\d+\\]|### 숙소 추천|### 총 예상 경비|\\Z)", Pattern.DOTALL);
    private static final Pattern PLACE_PATTERN = Pattern.compile("- (\\d{1,2}:\\d{2}): (.*?) \\((.*?)\\s*(?:- (.*?))?\\s*(?=\\n- \\d|\\Z)", Pattern.DOTALL);
    private static final Pattern COORDINATE_PATTERN = Pattern.compile("위도: (\\d+\\.\\d+), 경도: (\\d+\\.\\d+)");
    
    public TripPlan parseAiResponse(String aiResponse, TripPlan tripPlan) {
        try {
            List<TripPlan.DailySchedule> dailySchedules = new ArrayList<>();
            Matcher dayMatcher = DAY_PATTERN.matcher(aiResponse);
            
            while (dayMatcher.find()) {
                int dayNumber = Integer.parseInt(dayMatcher.group(1));
                String dayContent = dayMatcher.group(2).trim();
                
                List<TripPlan.ScheduleItem> scheduleItems = new ArrayList<>();
                List<String> places = new ArrayList<>();
                
                Matcher placeMatcher = PLACE_PATTERN.matcher(dayContent);
                while (placeMatcher.find()) {
                    String time = placeMatcher.group(1);
                    String placeName = placeMatcher.group(2).trim();
                    String address = placeMatcher.group(3).trim();
                    String description = placeMatcher.group(4) != null ? placeMatcher.group(4).trim() : "";
                    
                    // Extract coordinates if available
                    Matcher coordMatcher = COORDINATE_PATTERN.matcher(description);
                    double latitude = 0.0;
                    double longitude = 0.0;
                    
                    if (coordMatcher.find()) {
                        try {
                            latitude = Double.parseDouble(coordMatcher.group(1));
                            longitude = Double.parseDouble(coordMatcher.group(2));
                        } catch (NumberFormatException e) {
                            log.warn("Failed to parse coordinates: {}", e.getMessage());
                        }
                    }
                    
                    TripPlan.ScheduleItem item = new TripPlan.ScheduleItem();
                    item.setStartTime(time);
                    item.setPlace(placeName);
                    item.setDescription(description);
                    item.setLatitude(latitude);
                    item.setLongitude(longitude);
                    
                    scheduleItems.add(item);
                    places.add(placeName);
                }
                
                TripPlan.DailySchedule dailySchedule = new TripPlan.DailySchedule();
                dailySchedule.setDay(dayNumber);
                dailySchedule.setSchedules(scheduleItems);
                dailySchedule.setPlaces(places);
                
                dailySchedules.add(dailySchedule);
            }
            
            tripPlan.setDailySchedules(dailySchedules);
            return tripPlan;
            
        } catch (Exception e) {
            log.error("Error parsing AI response", e);
            return tripPlan;
        }
    }
    
    public String formatDate(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 (E)"));
    }
}
