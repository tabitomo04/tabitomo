package com.koreatravel.tabitomo.controller.member;

import com.koreatravel.tabitomo.config.security.UserDetailsImpl;
import com.koreatravel.tabitomo.domain.dto.member.MemberProfileDTO;
import com.koreatravel.tabitomo.domain.entity.trip.FavoritePlace;
import com.koreatravel.tabitomo.domain.entity.trip.Schedule;
import com.koreatravel.tabitomo.domain.entity.trip.Trip;
import com.koreatravel.tabitomo.service.member.MemberService;
import com.koreatravel.tabitomo.service.trip.FavoritePlaceService;
import com.koreatravel.tabitomo.service.trip.TripPlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final TripPlanService tripPlanService;
    private final FavoritePlaceService favoritePlaceService;

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("countries", memberService.getAllCountries());
        model.addAttribute("languages", memberService.getAllLanguages());
        return "signupform";
    }

    @GetMapping("/member/mypage")
    public String myPage(@AuthenticationPrincipal UserDetailsImpl userDetails,
                         @RequestParam(value = "tripPage", defaultValue = "0") int tripPage,
                         @RequestParam(value = "favPage", defaultValue = "0") int favPage,
                         Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        String email = userDetails.getEmail();

        MemberProfileDTO profile = memberService.getMemberProfile(userDetails.getId());
        model.addAttribute("profile", profile);

        Pageable tripPageable = PageRequest.of(tripPage, 5);
        Page<Trip> tripsPage = tripPlanService.findTripsByEmail(email, tripPageable);
        model.addAttribute("tripsPage", tripsPage);

        Pageable favPageable = PageRequest.of(favPage, 5);
        Page<FavoritePlace> favoritePlacesPage = favoritePlaceService.getFavorites(email, favPageable);
        model.addAttribute("favoritePlacesPage", favoritePlacesPage);

        int totalTripPages = tripsPage.getTotalPages();
        if (totalTripPages > 0) {
            int startTripPage = Math.max(0, tripsPage.getNumber() - 2);
            int endTripPage = Math.min(totalTripPages - 1, tripsPage.getNumber() + 2);
            model.addAttribute("startTripPage", startTripPage);
            model.addAttribute("endTripPage", endTripPage);
        }

        int totalFavPages = favoritePlacesPage.getTotalPages();
        if (totalFavPages > 0) {
            int startFavPage = Math.max(0, favoritePlacesPage.getNumber() - 2);
            int endFavPage = Math.min(totalFavPages - 1, favoritePlacesPage.getNumber() + 2);
            model.addAttribute("startFavPage", startFavPage);
            model.addAttribute("endFavPage", endFavPage);
        }

        return "member/mypage";
    }

    @GetMapping("/member/trips/{id}")
    public String tripDetail(@PathVariable Long id, @AuthenticationPrincipal UserDetailsImpl userDetails, Model model, RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        Trip trip = tripPlanService.findTripByIdAndMemberId(id, userDetails.getId())
                .orElse(null);

        if (trip == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "해당 여행 계획을 찾을 수 없거나 접근 권한이 없습니다.");
            return "redirect:/member/mypage";
        }

        List<Schedule> schedules = tripPlanService.findSchedulesByTripId(id);

        model.addAttribute("trip", trip);
        model.addAttribute("schedules", schedules);
        return "trip-detail";
    }

    @PostMapping("/member/trips/{id}/delete")
    public String deleteTrip(@PathVariable Long id, @AuthenticationPrincipal UserDetailsImpl userDetails, RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        try {
            tripPlanService.deleteTripPlan(id, userDetails.getId());
            redirectAttributes.addFlashAttribute("successMessage", "여행 계획이 성공적으로 삭제되었습니다.");
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "여행 계획 삭제 중 오류가 발생했습니다.");
        }

        return "redirect:/member/mypage";
    }

    @PostMapping("/member/trips/{id}/toggle-visibility")
    public String toggleVisibility(@PathVariable Long id, @AuthenticationPrincipal UserDetailsImpl userDetails, RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        try {
            tripPlanService.toggleTripVisibility(id, userDetails.getId());
            redirectAttributes.addFlashAttribute("successMessage", "공개 상태가 변경되었습니다.");
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            log.error("Error toggling trip visibility", e);
            redirectAttributes.addFlashAttribute("errorMessage", "상태 변경 중 오류가 발생했습니다.");
        }

        return "redirect:/member/mypage";
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
            return "redirect:/trips/public";
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
}
