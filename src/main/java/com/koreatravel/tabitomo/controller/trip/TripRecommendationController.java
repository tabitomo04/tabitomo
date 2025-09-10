package com.koreatravel.tabitomo.controller.trip;

import com.koreatravel.tabitomo.domain.dto.trip.*;
import com.koreatravel.tabitomo.domain.entity.trip.PlaceEntity;
import com.koreatravel.tabitomo.service.trip.GeminiAIService;
import com.koreatravel.tabitomo.service.trip.TripPlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/trip")
@RequiredArgsConstructor
@SessionAttributes("tripPlan")
public class TripRecommendationController {

    private final GeminiAIService geminiAIService;
    private final TripPlanService tripPlanService;

    @ModelAttribute("tripPlan")
    public TripPlanDTO setUpTripForm() {
        return new TripPlanDTO();
    }

    @GetMapping("/step1")
    public String showStep1() {
        return "trip/step1";
    }

    @PostMapping("/step2")
    public String processStep1(
            @ModelAttribute("tripPlan") TripPlanDTO tripPlan,
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
        tripPlan.setFacilities(facilities != null ? facilities : new ArrayList<>());

        return "trip/step2";
    }

    @PostMapping("/step3")
    public String processStep2(
            @ModelAttribute("tripPlan") TripPlanDTO tripPlan,
            @RequestParam("styles") List<String> styles,
            @RequestParam(name = "budget") String budget,
            Model model) {

        List<String> uniqueStyles = styles == null ? new ArrayList<>() : styles.stream().distinct().collect(Collectors.toList());
        tripPlan.setStyles(uniqueStyles);
        tripPlan.setBudget(budget);

        log.info("GeminiAIService.getRecommendation 호출: destination={}, duration={}, styles={}, budget={}",
                tripPlan.getDestination(), tripPlan.getDuration(), tripPlan.getStyles(), tripPlan.getBudget());

        // Call the Gemini AI service to get recommendations
        TourRecommendationDTO tourRecommendation = geminiAIService.getRecommendation(
                tripPlan.getDestination(),
                String.valueOf(tripPlan.getDuration()),
                String.join(", ", tripPlan.getStyles()),
                tripPlan.getBudget(),
                tripPlan.getAccommodationBudget(),
                tripPlan.getFacilities()
        );

        // Process the recommendation and update the trip plan
        if (tourRecommendation != null && tourRecommendation.getAccommodation() != null) {
            TripPlanDTO.Accommodation accommodation = new TripPlanDTO.Accommodation();
            PlaceEntity place = tourRecommendation.getAccommodation();
            
            // Map fields from PlaceEntity to TripPlanDTO.Accommodation
            accommodation.setPlaceName(place.getName());
            accommodation.setDescription(place.getDescription());
            // Since PlaceEntity doesn't have priceRange, we'll set it to null or a default value
            accommodation.setPriceRange(null);
            accommodation.setImageUrl(place.getImageUrl());
            accommodation.setLatitude(place.getLatitude());
            accommodation.setLongitude(place.getLongitude());
            accommodation.setAddress(place.getAddress());
            
            tripPlan.setAccommodation(accommodation);
        }

        // Process itinerary items
        if (tourRecommendation != null && tourRecommendation.getItinerary() != null) {
            tripPlan.setDailySchedules(new ArrayList<>());
            for (int i = 1; i <= tripPlan.getDuration(); i++) {
                TripPlanDTO.DailySchedule dailySchedule = new TripPlanDTO.DailySchedule();
                dailySchedule.setDay(i);
                // Convert itinerary items to the correct type
                List<TourRecommendationDTO.ItineraryItem> itineraryItems = new ArrayList<>();
                if (tourRecommendation.getItinerary() != null) {
                    for (TourRecommendationDTO.ItineraryItem item : tourRecommendation.getItinerary()) {
                        TourRecommendationDTO.ItineraryItem newItem = new TourRecommendationDTO.ItineraryItem();
                        newItem.setDay(item.getDay());
                        newItem.setTime(item.getTime());
                        newItem.setPlace(item.getPlace());
                        newItem.setDescription(item.getDescription());
                        newItem.setLatitude(item.getLatitude());
                        newItem.setLongitude(item.getLongitude());
                        itineraryItems.add(newItem);
                    }
                }
                for (TourRecommendationDTO.ItineraryItem item : itineraryItems) {
                    if (item.getDay() == i) {
                        TripPlanDTO.ScheduleItem scheduleItem = new TripPlanDTO.ScheduleItem();
                        scheduleItem.setPlace(item.getPlace());
                        scheduleItem.setDescription(item.getDescription());
                        scheduleItem.setStartTime(item.getTime());
                        scheduleItem.setLatitude(item.getLatitude());
                        scheduleItem.setLongitude(item.getLongitude());
                        dailySchedule.getSchedules().add(scheduleItem);
                    }
                }
                tripPlan.getDailySchedules().add(dailySchedule);
            }
        }

        model.addAttribute("recommendation", tourRecommendation);
        return "trip/step3";
    }

    @PostMapping("/save")
    public String saveTripPlan(
            @ModelAttribute("tripPlan") TripPlanDTO tripPlan,
            Principal principal) {
        
        if (principal == null) {
            return "redirect:/login";
        }
        
        log.info("Saving trip plan for user: {}", principal.getName());
        com.koreatravel.tabitomo.domain.dto.trip.TripPlanDTO tripPlanDTO = convertToTripPlanDTO(tripPlan);
        Long tripId = tripPlanService.saveTripPlan(tripPlanDTO, principal.getName());
        return "redirect:/trip/result/" + tripId;
    }

    private com.koreatravel.tabitomo.domain.dto.trip.TripPlanDTO convertToTripPlanDTO(TripPlanDTO tripPlan) {
        com.koreatravel.tabitomo.domain.dto.trip.TripPlanDTO dto = new com.koreatravel.tabitomo.domain.dto.trip.TripPlanDTO();
        // Map the fields from TripPlan to TripPlanDTO
        dto.setDestination(tripPlan.getDestination());
        dto.setStartDate(tripPlan.getStartDate());
        dto.setEndDate(tripPlan.getEndDate());
        dto.setBudget(tripPlan.getBudget());
        dto.setAccommodationBudget(tripPlan.getAccommodationBudget());
        dto.setFacilities(tripPlan.getFacilities());
        dto.setStyles(tripPlan.getStyles());
        dto.setDuration(tripPlan.getDuration());
        
        // Map daily schedules if available
        if (tripPlan.getDailySchedules() != null) {
            List<com.koreatravel.tabitomo.domain.dto.trip.TripPlanDTO.DailySchedule> dailySchedules = new ArrayList<>();
            for (TripPlanDTO.DailySchedule schedule : tripPlan.getDailySchedules()) {
                com.koreatravel.tabitomo.domain.dto.trip.TripPlanDTO.DailySchedule dailySchedule = 
                    new com.koreatravel.tabitomo.domain.dto.trip.TripPlanDTO.DailySchedule();
                dailySchedule.setDay(schedule.getDay());
                
                // Map schedule items
                List<com.koreatravel.tabitomo.domain.dto.trip.TripPlanDTO.ScheduleItem> scheduleItems = new ArrayList<>();
                List<String> places = new ArrayList<>();
                
                for (TripPlanDTO.ScheduleItem item : schedule.getSchedules()) {
                    com.koreatravel.tabitomo.domain.dto.trip.TripPlanDTO.ScheduleItem scheduleItem = 
                        new com.koreatravel.tabitomo.domain.dto.trip.TripPlanDTO.ScheduleItem();
                    scheduleItem.setPlace(item.getPlace());
                    scheduleItem.setDescription(item.getDescription());
                    scheduleItem.setLatitude(item.getLatitude());
                    scheduleItem.setLongitude(item.getLongitude());
                    // Use startTime as the time field since TripPlanDTO.ScheduleItem only has time, not start/end time
                    scheduleItem.setStartTime(item.getStartTime());
                    
                    scheduleItems.add(scheduleItem);
                    places.add(item.getPlace());
                }
                dailySchedule.setSchedules(scheduleItems);
                dailySchedule.setPlaces(places);
                dailySchedules.add(dailySchedule);
            }
            dto.setDailySchedules(dailySchedules);
        }
        
        // Map accommodation if available
        if (tripPlan.getAccommodation() != null) {
            TripPlanDTO.Accommodation accommodation = new TripPlanDTO.Accommodation();
            TripPlanDTO.Accommodation sourceAcc = tripPlan.getAccommodation();
            
            accommodation.setPlaceName(sourceAcc.getPlaceName());
            accommodation.setDescription(sourceAcc.getDescription());
            accommodation.setPriceRange(sourceAcc.getPriceRange());
            accommodation.setImageUrl(sourceAcc.getImageUrl());
            accommodation.setLatitude(sourceAcc.getLatitude());
            accommodation.setLongitude(sourceAcc.getLongitude());
            
            dto.setAccommodation(accommodation);
        }
        
        return dto;
    }

    @GetMapping("/result/{tripId}")
    public String showResult(@PathVariable Long tripId, Model model) {
        // You can add logic to fetch the saved trip by ID and add it to the model
        // For now, we'll just show a success page
        model.addAttribute("tripId", tripId);
        return "trip/result";
    }
}
