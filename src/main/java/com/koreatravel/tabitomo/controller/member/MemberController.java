package com.koreatravel.tabitomo.controller.member;

import com.koreatravel.tabitomo.service.member.MemberService;
import com.koreatravel.tabitomo.service.trip.TripPlanService;
import com.koreatravel.tabitomo.service.storybook.EditorService;
import com.koreatravel.tabitomo.config.security.UserDetailsImpl;
import com.koreatravel.tabitomo.domain.dto.storybook.StorybookListDTO;
import com.koreatravel.tabitomo.domain.dto.storybook.TempsaveDTO;
import com.koreatravel.tabitomo.domain.entity.trip.Trip;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/member")
public class MemberController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private TripPlanService tripService;

    @Autowired
    private EditorService storybookService;

    // 마이페이지
    @GetMapping("/mypage")
    public String myPage(@AuthenticationPrincipal UserDetailsImpl userDetails, 
                        Model model,
                        @RequestParam(defaultValue = "false") boolean all) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        
        // 1. 여행 목록 조회
        List<Trip> trips = tripService.findTripsByUserId(userDetails.getMemberId());
        model.addAttribute("trips", trips);
        
        // 2. 스토리북 목록 조회
        List<StorybookListDTO> storybookList = storybookService.getMyStorybookList();
        model.addAttribute("storybookList", storybookList);
        
        // 3. 임시저장 목록 조회 (all 파라미터가 true일 때만)
        if (all) {
            List<TempsaveDTO> tempsaveList = storybookService.getTempsaveList();
            model.addAttribute("templist", tempsaveList);
        }
        
        model.addAttribute("showAll", all);
        return "member/mypage";
    }

    // 여행 상세 조회
    @GetMapping("/trips/{id}")
    public String tripDetail(@PathVariable Long id, 
                           @AuthenticationPrincipal UserDetailsImpl userDetails, 
                           Model model, 
                           RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        // 여행 조회 및 소유권 확인
        Trip trip = tripService.findTripByIdAndUserId(id, userDetails.getMemberId())
                .orElse(null);

        if (trip == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "해당 여행 계획을 찾을 수 없거나 접근 권한이 없습니다.");
            return "redirect:/member/mypage";
        }

        // 여행 일정 조회
        model.addAttribute("trip", trip);
        return "trip/detail";
    }

    // 멤버 프로필(정보) -> 다른 사람도 표기되어야 함
    @GetMapping("/info/{memberId}")
    public String viewMemberProfile(@PathVariable String memberId) {
        return "member/profile";
    }

    // 멤버 프로필 수정
    @GetMapping("/profile/update")
    public String updateProfile() {
        return "member/update-profile";
    }

    // 멤버의 스토리북 목록
    @GetMapping("/member/storybook/list")
    public String viewStorybookList() {
        return "member/storybook-list";
    }

    // 멤버의 좋아하는 관광지 목록
    @GetMapping("/member/attraction/list")
    public String viewAttractionList() {
        return "member/attraction-list";
    }

    // 멤버의 likedbook 목록
    @GetMapping("/member/likedbook/list")
    public String viewLikedbookList() {
        return "member/likedbook-list";
    }

    // 멤버의 여행 목록
    @GetMapping("/member/trip/list")
    public String viewTripList() {
        return "member/trip-list";
    }
}
