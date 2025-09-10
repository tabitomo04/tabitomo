package com.koreatravel.tabitomo.controller;

import com.koreatravel.tabitomo.domain.entity.trip.TripEntity;
import com.koreatravel.tabitomo.service.trip.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MypageController {

    private final TripService tripService;

    @GetMapping("/mypage")
    public String mypage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/member/login";
        }
        
        // Get trips for the current user
        List<TripEntity> trips = tripService.getTripsByMemberEmail(userDetails.getUsername());
        model.addAttribute("trips", trips);
        return "mypage";
    }

    @GetMapping("/trips/{id}")
    public String tripDetail(@PathVariable Long id, 
                           @AuthenticationPrincipal UserDetails userDetails, 
                           Model model, 
                           RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/member/login";
        }

        // Find the trip and verify ownership
        TripEntity trip = tripService.getTrip(id);
        if (trip == null || !trip.getMember().getEmail().equals(userDetails.getUsername())) {
            redirectAttributes.addFlashAttribute("errorMessage", "해당 여행 계획을 찾을 수 없거나 접근 권한이 없습니다.");
            return "redirect:/mypage";
        }

        model.addAttribute("trip", trip);
        return "trip/detail";
    }
}
