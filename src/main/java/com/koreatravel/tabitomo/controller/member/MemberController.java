package com.koreatravel.tabitomo.controller.member;

import com.koreatravel.tabitomo.config.security.UserDetailsImpl;
import com.koreatravel.tabitomo.domain.dto.member.MemberProfileDTO;
import com.koreatravel.tabitomo.domain.dto.storybook.StorybookListDTO;
import com.koreatravel.tabitomo.domain.dto.storybook.TempsaveDTO;
import com.koreatravel.tabitomo.domain.entity.trip.FavoritePlace;
import com.koreatravel.tabitomo.domain.entity.trip.Schedule;
import com.koreatravel.tabitomo.domain.entity.trip.Trip;
import com.koreatravel.tabitomo.service.member.MemberService;
import com.koreatravel.tabitomo.service.storybook.EditorService;
import com.koreatravel.tabitomo.service.trip.FavoritePlaceService;
import com.koreatravel.tabitomo.service.trip.TripPlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.koreatravel.tabitomo.service.member.AuthService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final TripPlanService tripPlanService;
    private final FavoritePlaceService favoritePlaceService;
    private final EditorService editorService;
    private final AuthService authService;

    @GetMapping("/member/mypage")
    public String myPage(@AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(value = "tripPage", defaultValue = "0") int tripPage,
            @RequestParam(value = "favPage", defaultValue = "0") int favPage,
            @RequestParam(defaultValue = "false") boolean all,
            Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        String email = userDetails.getEmail();

        UUID memberId = userDetails.getId();

        MemberProfileDTO profile = memberService.getMemberProfile(userDetails.getId());
        model.addAttribute("profile", profile);

        Pageable tripPageable = PageRequest.of(tripPage, 3);
        Page<Trip> tripsPage = tripPlanService.findTripsByEmail(email, tripPageable);
        model.addAttribute("tripsPage", tripsPage);

        Pageable favPageable = PageRequest.of(favPage, 3);
        Page<FavoritePlace> favoritePlacesPage = favoritePlaceService.getFavorites(userDetails.getId(), favPageable);
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

        // 내 스토리북 리스트
        List<StorybookListDTO> storybookList = editorService.getMyStorybookList(memberId);
        if (!all) {
            storybookList = storybookList.stream().limit(3).toList();
        }
        model.addAttribute("storylist", storybookList);
        model.addAttribute("all", all);

        // 임시저장 리스트
        List<TempsaveDTO> tempsaveList = editorService.getTempsaveList(email);
        model.addAttribute("templist", tempsaveList);

        return "member/mypage";
    }

    @GetMapping("/member/trips/{id}")
    public String tripDetail(@PathVariable Long id, @AuthenticationPrincipal UserDetailsImpl userDetails, Model model,
            RedirectAttributes redirectAttributes) {
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
    public String deleteTrip(@PathVariable Long id, @AuthenticationPrincipal UserDetailsImpl userDetails,
            RedirectAttributes redirectAttributes) {
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
    public String toggleVisibility(@PathVariable Long id, @AuthenticationPrincipal UserDetailsImpl userDetails,
            RedirectAttributes redirectAttributes) {
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
    public String publicTrips(Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "title") String searchType,
            @RequestParam(defaultValue = "") String keyword) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by("createdAt").descending());
        Page<Trip> trips = tripPlanService.findAllPublicTrips(pageable, searchType, keyword);
        model.addAttribute("trips", trips);
        model.addAttribute("searchType", searchType);
        model.addAttribute("keyword", keyword);
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
    public String userPublicPage(@PathVariable String nickname, @RequestParam(defaultValue = "0") int page, Model model) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by("createdAt").descending());
        Page<Trip> tripsPage = tripPlanService.findPublicTripsByNickname(nickname, pageable);
        model.addAttribute("tripsPage", tripsPage);
        model.addAttribute("nickname", nickname);

        int totalPages = tripsPage.getTotalPages();
        if (totalPages > 0) {
            int startPage = Math.max(0, tripsPage.getNumber() - 2);
            int endPage = Math.min(totalPages - 1, tripsPage.getNumber() + 2);
            model.addAttribute("startPage", startPage);
            model.addAttribute("endPage", endPage);
        }

        return "user-public-page";
    }

    /**
     * 닉네임 중복 체크 API
     * 
     * @param nickname 확인할 닉네임
     * @return 중복 여부 (true: 사용 가능, false: 중복됨)
     */
    @PostMapping("/member/api/check-nickname")
    @ResponseBody
    public Map<String, Object> checkNickname(@RequestBody Map<String, String> request) {
        String nickname = request.get("nickname");
        Map<String, Object> response = new HashMap<>();
        try {
            boolean isAvailable = !authService.existsByNickname(nickname);
            response.put("available", isAvailable);
            response.put("message", isAvailable ? "사용 가능한 닉네임입니다." : "이미 사용 중인 이메일입니다.");
            return response;
        } catch (Exception e) {
            log.error("닉네임 확인 중 오류 발생", e);
            response.put("available", false);
            response.put("message", "닉네임 확인 중 오류가 발생했습니다.");
            return response;
        }
    }

    @PostMapping("/member/api/check-email")
    @ResponseBody
    public Map<String, Object> checkEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        Map<String, Object> response = new HashMap<>();
        try {
            boolean isAvailable = !authService.existsByEmail(email);
            response.put("available", isAvailable);
            response.put("message", isAvailable ? "사용 가능한 이메일입니다." : "이미 사용 중인 이메일입니다.");
            return response;
        } catch (Exception e) {
            log.error("이메일 확인 중 오류 발생", e);
            response.put("available", false);
            response.put("message", "이메일 확인 중 오류가 발생했습니다.");
            return response;
        }
    }

}
