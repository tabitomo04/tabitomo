package com.koreantravel.tabitomo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.koreantravel.tabitomo.domain.entity.FavoritePlace;
import com.koreantravel.tabitomo.domain.entity.Schedule;
import com.koreantravel.tabitomo.domain.entity.Trip;
import com.koreantravel.tabitomo.service.FavoritePlaceService;
import com.koreantravel.tabitomo.service.TripPlanService;

import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MypageController {

    private final TripPlanService tripPlanService;
    private final FavoritePlaceService favoritePlaceService;

    @GetMapping("/mypage")
    public String mypage(@AuthenticationPrincipal UserDetails userDetails,
                         @RequestParam(value = "tripPage", defaultValue = "0") int tripPage,
                         @RequestParam(value = "favPage", defaultValue = "0") int favPage,
                         Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        String username = userDetails.getUsername();

        // Pagination for Trips (5 per page)
        Pageable tripPageable = PageRequest.of(tripPage, 5);
        Page<Trip> tripsPage = tripPlanService.findTripsByEmail(username, tripPageable);
        model.addAttribute("tripsPage", tripsPage);

        // Pagination for Favorite Places (5 per page)
        Pageable favPageable = PageRequest.of(favPage, 5);
        Page<FavoritePlace> favoritePlacesPage = favoritePlaceService.getFavorites(username, favPageable);
        model.addAttribute("favoritePlacesPage", favoritePlacesPage);

        // Pagination helper for trips
        int totalTripPages = tripsPage.getTotalPages();
        if (totalTripPages > 0) {
            int startTripPage = Math.max(0, tripsPage.getNumber() - 2);
            int endTripPage = Math.min(totalTripPages - 1, tripsPage.getNumber() + 2);
            model.addAttribute("startTripPage", startTripPage);
            model.addAttribute("endTripPage", endTripPage);
        }

        // Pagination helper for favorites
        int totalFavPages = favoritePlacesPage.getTotalPages();
        if (totalFavPages > 0) {
            int startFavPage = Math.max(0, favoritePlacesPage.getNumber() - 2);
            int endFavPage = Math.min(totalFavPages - 1, favoritePlacesPage.getNumber() + 2);
            model.addAttribute("startFavPage", startFavPage);
            model.addAttribute("endFavPage", endFavPage);
        }

        return "mypage";
    }

    @GetMapping("/trips/public")
    public String publicTrips(Model model) {
        List<Trip> trips = tripPlanService.findAllPublicTrips();
        model.addAttribute("trips", trips);
        return "public-trips";
    }

    @GetMapping("/public-trips/{id}")
    public String publicTripDetail(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Trip> tripOpt = tripPlanService.findTripById(id);

        if (tripOpt.isEmpty() || !"PUBLIC".equals(tripOpt.get().getVisibility())) {
            redirectAttributes.addFlashAttribute("errorMessage", "해당 여행 계획을 찾을 수 없거나 공개되지 않았습니다.");
            return "redirect:/public-trips";
        }

        Trip trip = tripOpt.get();
        List<Schedule> schedules = tripPlanService.findSchedulesByTripId(id);

        model.addAttribute("trip", trip);
        model.addAttribute("schedules", schedules);
        return "public-trip-detail";
    }

    @GetMapping("/user/{nickname}")
    public String userPublicPage(@PathVariable String nickname, Model model) {
        List<Trip> trips = tripPlanService.findPublicTripsByNickname(nickname);
        model.addAttribute("trips", trips);
        model.addAttribute("nickname", nickname);
        return "user-public-page";
    }

    @GetMapping("/trips/{id}")
    public String tripDetail(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails, Model model, RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        // Find the trip and verify ownership
        Trip trip = tripPlanService.findTripByIdAndEmail(id, userDetails.getUsername())
                .orElse(null);

        if (trip == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "해당 여행 계획을 찾을 수 없거나 접근 권한이 없습니다.");
            return "redirect:/mypage";
        }

        // Find all schedules for this trip
        List<Schedule> schedules = tripPlanService.findSchedulesByTripId(id);

        model.addAttribute("trip", trip);
        model.addAttribute("schedules", schedules);
        return "trip-detail";
    }

    @PostMapping("/trips/{id}/delete")
    public String deleteTrip(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails, RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        try {
            tripPlanService.deleteTripPlan(id, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("successMessage", "여행 계획이 성공적으로 삭제되었습니다.");
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "여행 계획 삭제 중 오류가 발생했습니다.");
        }

        return "redirect:/mypage";
    }

    @PostMapping("/trips/{id}/toggle-visibility")
    public String toggleVisibility(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails, RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        try {
            tripPlanService.toggleTripVisibility(id, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("successMessage", "공개 상태가 변경되었습니다.");
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            log.error("Error toggling trip visibility", e);
            redirectAttributes.addFlashAttribute("errorMessage", "상태 변경 중 오류가 발생했습니다.");
        }

        return "redirect:/mypage";
    }
}
