package vio.tabitomo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vio.tabitomo.domain.entity.Schedule;
import vio.tabitomo.domain.entity.Trip;
import vio.tabitomo.service.TripPlanService;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MypageController {

    private final TripPlanService tripPlanService;

    @GetMapping("/mypage")
    public String mypage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        List<Trip> trips = tripPlanService.findTripsByUsername(userDetails.getUsername());
        model.addAttribute("trips", trips);
        return "mypage";
    }

    @GetMapping("/trips/{id}")
    public String tripDetail(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails, Model model, RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        // Find the trip and verify ownership
        Trip trip = tripPlanService.findTripByIdAndUsername(id, userDetails.getUsername())
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
}
