package com.koreatravel.tabitomo.controller.member;

import com.koreatravel.tabitomo.service.member.MemberService;
import com.koreatravel.tabitomo.service.trip.TripPlanService;
import com.koreatravel.tabitomo.service.storybook.EditorService;
import com.koreatravel.tabitomo.config.security.UserDetailsImpl;
import com.koreatravel.tabitomo.domain.dto.member.MemberProfileDTO;
import com.koreatravel.tabitomo.domain.dto.storybook.StorybookListDTO;
import com.koreatravel.tabitomo.domain.dto.storybook.TempsaveDTO;
import com.koreatravel.tabitomo.domain.entity.member.MemberEntity;
import com.koreatravel.tabitomo.domain.entity.trip.Trip;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
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

    @GetMapping("/info/{memberId}")
    public String viewMemberProfile(@PathVariable String memberId, Model model) {
        // TODO: memberId로 회원 정보 조회
        MemberProfileDTO profile = memberService.getMemberProfile(memberId);
        model.addAttribute("profile", profile);
        return "member/profile";
    }

    // 멤버 프로필 수정 페이지
    @GetMapping("/profile/update")
    public String updateProfilePage(@AuthenticationPrincipal UserDetailsImpl userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        
        MemberProfileDTO profile = memberService.getMemberProfile(userDetails.getMemberId().toString());
        model.addAttribute("profile", profile);
        return "member/update-profile";
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

    // 멤버 프로필 수정 처리
    @PostMapping("/profile/update")
    public ResponseEntity<?> updateProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
            @RequestPart(value = "profileData") MemberProfileDTO profileDTO) {
        
        try {
            MemberEntity updatedMember = memberService.updateProfile(
                userDetails.getMemberId(), 
                profileDTO, 
                profileImage
            );
            
            // 프로필 이미지가 업데이트된 경우 세션 업데이트
            if (profileImage != null && !profileImage.isEmpty()) {
                userDetails.getMember().setProfileImageUrl(updatedMember.getProfileImageUrl());
            }
            
            return ResponseEntity.ok().body(Map.of("message", "프로필이 성공적으로 업데이트되었습니다."));
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "프로필 업데이트 중 오류가 발생했습니다."));
        }
    }

    // 멤버의 스토리북 목록
    @GetMapping("/storybook/list")
    public String viewStorybookList() {
        return "member/storybook-list";
    }
    
    // 멤버의 좋아하는 관광지 목록
    @GetMapping("/attraction/list")
    public String viewAttractionList() {
        return "member/attraction-list";
    }
    
    // 멤버의 likedbook 목록
    @GetMapping("/likedbook/list")
    public String viewLikedbookList() {
        return "member/likedbook-list";
    }
    
    // 멤버의 여행 목록
    @GetMapping("/trip/list")
    public String viewTripList() {
        return "member/trip-list";
    }
    /**
     * 이메일 중복 확인 API
     * @param email 확인할 이메일
     * @return 중복 여부를 포함한 JSON 응답
     */
    @GetMapping("/api/check-email")
    @ResponseBody
    public Map<String, Object> checkEmail(@RequestParam String email) {
        boolean isExists = memberService.isEmailExists(email);
        Map<String, Object> response = new HashMap<>();
        response.put("success", !isExists);
        response.put("message", isExists ? "이미 사용 중인 이메일입니다." : "사용 가능한 이메일입니다.");
        return response;
    }
    
    /**
     * 닉네임 중복 확인 API
     * @param nickname 확인할 닉네임
     * @return 중복 여부를 포함한 JSON 응답
     */
    @GetMapping("/api/check-nickname")
    @ResponseBody
    public Map<String, Object> checkNickname(@RequestParam String nickname) {
        boolean isExists = memberService.isNicknameExists(nickname);
        Map<String, Object> response = new HashMap<>();
        response.put("exists", isExists);
        response.put("message", isExists ? "이미 사용 중인 닉네임입니다." : "사용 가능한 닉네임입니다.");
        return response;
    }
}
