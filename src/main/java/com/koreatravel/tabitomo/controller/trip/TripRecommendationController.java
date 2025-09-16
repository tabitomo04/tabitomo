package com.koreatravel.tabitomo.controller.trip;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.koreatravel.tabitomo.domain.dto.trip.ScheduleInfo;
import com.koreatravel.tabitomo.domain.dto.trip.TripPlan;
import com.koreatravel.tabitomo.domain.dto.trip.TourRecommendation;
import com.koreatravel.tabitomo.domain.entity.trip.Place;
import com.koreatravel.tabitomo.service.trip.GeminiAIService;
import com.koreatravel.tabitomo.service.trip.LocationService;
import com.koreatravel.tabitomo.service.trip.TripPlanService;

import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/trip")
@RequiredArgsConstructor
@SessionAttributes("tripPlan")
public class TripRecommendationController {

    private final GeminiAIService geminiAIService;
    private final LocationService locationService;
    private final TripPlanService tripPlanService;

    @Value("${kakao.js.key}")
    private String kakaoJsKey;

    @ModelAttribute("tripPlan")
    public TripPlan setUpTripForm() {
        return new TripPlan();
    }

    @GetMapping("/step1")
    public String showStep1() {
        return "tripselect/step1";
    }

    @PostMapping("/step2")
    public String processStep1(
            @ModelAttribute("tripPlan") TripPlan tripPlan,
            @RequestParam(name = "destination") String destination,
            @RequestParam(name = "startDate") String startDate,
            @RequestParam(name = "nights") int nights,
            @RequestParam(name = "accommodationBudget", required = false) Integer accommodationBudget,
            @RequestParam(name = "facilities", required = false) List<String> facilities) {

        log.info("step2 요청: destination={}, startDate={}, nights={}", destination, startDate, nights);
        if (nights <= 0) {
            log.warn("nights 값이 0 이하입니다. 1박으로 보정합니다.");
            nights = 1;
        }
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = start.plusDays(nights);
        int duration = nights + 1;
        log.info("계산된 duration={}, endDate={}", duration, end);

        tripPlan.setDestination(destination);
        tripPlan.setStartDate(start);
        tripPlan.setEndDate(end);
        tripPlan.setDuration(duration);
        tripPlan.setAccommodationBudget(accommodationBudget);
        tripPlan.setFacilities(facilities);

        return "tripselect/step2";
    }

    @PostMapping("/step3")
    public String processStep2(
            @ModelAttribute("tripPlan") TripPlan tripPlan,
            @RequestParam("styles") List<String> styles,
            @RequestParam(name = "budget") String budget) {

        List<String> uniqueStyles = styles == null ? new ArrayList<>() : styles.stream().distinct().collect(Collectors.toList());
        tripPlan.setStyles(uniqueStyles);
        tripPlan.setBudget(budget);

        log.info("GeminiAIService.getRecommendation 호출: destination={}, duration={}, styles={}, budget={}",
            tripPlan.getDestination(), tripPlan.getDuration(), tripPlan.getStyles(), tripPlan.getBudget());
        TourRecommendation tourRecommendation = geminiAIService.getRecommendation(
            tripPlan.getDestination(),
            String.valueOf(tripPlan.getDuration()),
            String.join(", ", tripPlan.getStyles()),
            tripPlan.getBudget(),
            tripPlan.getAccommodationBudget(),
            tripPlan.getFacilities()
        );

        if (tourRecommendation != null && tourRecommendation.getAccommodation() != null) {
            Place accPlace = tourRecommendation.getAccommodation();
            TripPlan.Accommodation accommodation = new TripPlan.Accommodation();
            accommodation.setPlaceName(accPlace.getName());
            accommodation.setDescription(accPlace.getDescription());
            accommodation.setPriceRange(accPlace.getPriceRange());
            accommodation.setImageUrl(accPlace.getImageUrl());
            accommodation.setLatitude(accPlace.getLatitude());
            accommodation.setLongitude(accPlace.getLongitude());
            accommodation.setAddress(accPlace.getAddress()); // 주소 정보 추가
            tripPlan.setAccommodation(accommodation);
        }

        List<TripPlan.DailySchedule> dailySchedules = new ArrayList<>();
        if (tourRecommendation != null && tourRecommendation.getItinerary() != null && !tourRecommendation.getItinerary().isEmpty()) {
            Map<Integer, List<ScheduleInfo>> dayGrouped = tourRecommendation.getItinerary().stream()
                .collect(Collectors.groupingBy(ScheduleInfo::getDay, LinkedHashMap::new, Collectors.toList()));

            for (Map.Entry<Integer, List<ScheduleInfo>> entry : dayGrouped.entrySet()) {
                TripPlan.DailySchedule dailySchedule = new TripPlan.DailySchedule();
                dailySchedule.setDay(entry.getKey());
                List<TripPlan.ScheduleItem> schedulesForDay = new ArrayList<>();
                List<String> placesForDay = new ArrayList<>();
                for (ScheduleInfo scheduleInfo : entry.getValue()) {
                    Place place = scheduleInfo.getPlace();
                    if (place != null) {
                        TripPlan.ScheduleItem scheduleItem = new TripPlan.ScheduleItem();
                        
                        String startTimeStr = scheduleInfo.getStartTime();
                        if (startTimeStr != null && startTimeStr.contains("~")) {
                            String[] times = startTimeStr.split("~", 2);
                            scheduleItem.setStartTime(times[0].trim());
                            if (times.length > 1) {
                                scheduleItem.setEndTime(times[1].trim());
                            }
                        } else {
                            scheduleItem.setStartTime(startTimeStr);
                            scheduleItem.setEndTime(scheduleInfo.getEndTime());
                        }

                        scheduleItem.setPlace(place.getName());
                        scheduleItem.setDescription(scheduleInfo.getMemo());
                        scheduleItem.setLatitude(place.getLatitude());
                        scheduleItem.setLongitude(place.getLongitude());
                        scheduleItem.setAddress(place.getAddress()); // 주소 정보 추가
                        schedulesForDay.add(scheduleItem);
                        placesForDay.add(place.getName());
                    }
                }
                dailySchedule.setSchedules(schedulesForDay);
                dailySchedule.setPlaces(placesForDay);
                dailySchedules.add(dailySchedule);
            }
        }
        tripPlan.setDailySchedules(dailySchedules);

        log.info("======================================================");
        log.info("FINAL CONTROLLER LOG - If you see this, the new code is running.");
        log.info("Passing tripPlan to step3 view: {}", tripPlan);
        log.info("======================================================");

        return "tripselect/step3";
    }

    @PostMapping("/step4")
    public String processStep3(@ModelAttribute("tripPlan") TripPlan tripPlan, Model model) {
        model.addAttribute("tripPlan", tripPlan);
        return "tripselect/step4";
    }

    @GetMapping("/step5")
    public String showStep5(@ModelAttribute("tripPlan") TripPlan tripPlan, Model model) {
        log.info("======================================================");
        log.info("Entering showStep5 method for /trip/step5");
        if (tripPlan == null || tripPlan.getDestination() == null) {
            log.warn("TripPlan object from session is NULL or empty.");
        } else {
            log.info("TripPlan object from session is NOT NULL. Data: {}", tripPlan.toString());
        }
        log.info("======================================================");

        model.addAttribute("tripPlan", tripPlan);
        model.addAttribute("kakaoJsKey", kakaoJsKey);
        return "tripselect/step5";
    }

    @GetMapping("/place-search-popup")
    public String showPlaceSearchPopup(@RequestParam(name = "destination", required = false, defaultValue = "") String destination, Model model) {
        model.addAttribute("destination", destination);
        return "tripselect/place-search-popup";
    }

    @GetMapping("/api/search-google-places")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> searchGooglePlaces(@RequestParam("query") String query) {
        try {
            List<Map<String, Object>> places = locationService.searchGooglePlaces(query);
            return ResponseEntity.ok(places);
        } catch (Exception e) {
            log.error("Error searching Google Places: ", e);
            return ResponseEntity.internalServerError().body(Collections.emptyList());
        }
    }

    @PostMapping("/update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateTripPlan(@RequestBody TripPlan tripPlan, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("status", "error", "message", "로그인이 필요합니다."));
        }
        try {
            tripPlanService.updateTripPlan(tripPlan, principal.getName());
            return ResponseEntity.ok(Map.of("status", "success", "message", "여행 계획이 성공적으로 업데이트되었습니다."));
        } catch (Exception e) {
            log.error("여행 계획 업데이트 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(Map.of("status", "error", "message", "업데이트 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    @PostMapping("/save")
    @ResponseBody
    public Map<String, Object> saveTripPlan(@RequestBody TripPlan tripPlan, Principal principal) {
        if (principal == null) {
            return Map.of("status", "error", "message", "로그인이 필요합니다.");
        }
        try {
            Long tripId = tripPlanService.saveTripPlan(tripPlan, principal.getName());
            return Map.of("status", "success", "message", "여행 계획이 저장되었습니다.", "tripId", tripId);
        } catch (Exception e) {
            log.error("여행 계획 저장 중 오류 발생", e);
            return Map.of("status", "error", "message", "저장 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @GetMapping("/search/places")
    @ResponseBody
    public List<Map<String, Object>> searchPlaces(
            @RequestParam String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(defaultValue = "5000") int radius) {

        if (lat != null && lng != null) {
            return locationService.searchPlaces(
                    query,
                    category,
                    lat,
                    lng,
                    radius
            );
        }
        return List.of();
    }
}
