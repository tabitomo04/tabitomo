package com.koreatravel.tabitomo.controller;

import com.koreatravel.tabitomo.domain.dto.trip.TripPlanDTO;
import com.koreatravel.tabitomo.domain.entity.trip.TripEntity;
import com.koreatravel.tabitomo.service.trip.TripPlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/trip")
@RequiredArgsConstructor
public class TripController {

    private final TripPlanService tripPlanService;

    @GetMapping("/new")
    public String newTripForm(Model model) {
        TripPlanDTO tripPlanDTO = new TripPlanDTO();
        // Set default values if needed
        model.addAttribute("tripPlanDTO", tripPlanDTO);
        return "trip-create";
    }

    @PostMapping
    public String createTrip(@ModelAttribute TripPlanDTO tripPlanDTO, 
                           @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        Long tripId = tripPlanService.saveTripPlan(tripPlanDTO, email);
        return "redirect:/trip/" + tripId;
    }

    @GetMapping("/{tripId}")
    public String viewTrip(@PathVariable Long tripId, 
                          @AuthenticationPrincipal UserDetails userDetails,
                          Model model) {
        String email = userDetails.getUsername();
        TripEntity trip = tripPlanService.findTripByIdAndMemberEmail(tripId, email)
                .orElseThrow(() -> new RuntimeException("Trip not found or access denied"));
        
        model.addAttribute("trip", trip);
        model.addAttribute("schedules", tripPlanService.findSchedulesByTripId(tripId));
        return "trip-detail";
    }

    @GetMapping("/{tripId}/edit")
    public String editTripForm(@PathVariable Long tripId,
                             @AuthenticationPrincipal UserDetails userDetails,
                             Model model) {
        String email = userDetails.getUsername();
        TripEntity trip = tripPlanService.findTripByIdAndMemberEmail(tripId, email)
                .orElseThrow(() -> new RuntimeException("Trip not found or access denied"));
        
        // Convert TripEntity to TripPlanDTO using the service layer method
        TripPlanDTO tripPlanDTO = tripPlanService.convertToDTO(trip);
        model.addAttribute("tripPlanDTO", tripPlanDTO);
        model.addAttribute("trip", trip);
        return "trip-edit";
    }

    @PostMapping("/{tripId}/update")
    public String updateTrip(@PathVariable Long tripId,
                           @ModelAttribute TripPlanDTO tripPlanDTO,
                           @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        tripPlanService.updateTripPlan(tripId, tripPlanDTO, email);
        return "redirect:/trip/" + tripId;
    }

    @PostMapping("/{tripId}/delete")
    public String deleteTrip(@PathVariable Long tripId,
                           @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        tripPlanService.deleteTripPlan(tripId, email);
        return "redirect:/member/mypage";
    }

    @GetMapping("/list")
    public String listTrips(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String email = userDetails.getUsername();
        List<TripEntity> trips = tripPlanService.findTripsByMemberEmail(email);
        model.addAttribute("trips", trips);
        return "trip-list";
    }

}
