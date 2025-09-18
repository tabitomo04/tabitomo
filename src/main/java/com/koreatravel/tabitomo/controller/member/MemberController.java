package com.koreatravel.tabitomo.controller.member;

import com.koreatravel.tabitomo.config.security.UserDetailsImpl;
import com.koreatravel.tabitomo.domain.dto.member.*;
import com.koreatravel.tabitomo.domain.entity.member.MemberAddInfoEntity;
import com.koreatravel.tabitomo.exception.ResourceNotFoundException;
import com.koreatravel.tabitomo.repository.member.MemberAddInfoRepository;
import com.koreatravel.tabitomo.service.member.MemberService;
import com.koreatravel.tabitomo.service.trip.TripPlanService;
import com.koreatravel.tabitomo.service.storybook.EditorService;
import com.koreatravel.tabitomo.domain.entity.trip.Trip;
import com.koreatravel.tabitomo.domain.dto.storybook.StorybookListDTO;
import com.koreatravel.tabitomo.domain.dto.storybook.TempsaveDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Controller for handling all member-related web and API requests.
 */
@Slf4j
@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {
    

    private final MemberService memberService;
    private final TripPlanService tripService;
    private final EditorService storybookService;
    private final MemberAddInfoRepository memberAddInfoRepository;

    // API Endpoints
    
    /**
     * Get current user's profile
     */
    @GetMapping("/me")
    @ResponseBody
    public ResponseEntity<?> getMyProfile(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "인증이 필요합니다."));
        }
        
        try {
            MemberProfileDTO profile = memberService.getMemberProfile(userDetails.getId());
            List<MemberAddInfoEntity> additionalInfo = memberAddInfoRepository.findByMemberId(userDetails.getId());
            
            // Create response DTO
            MemberProfileResponseDTO response = MemberProfileResponseDTO.builder()
                .profile(profile)
                .additionalInfo(MemberAddInfoDTO.fromEntities(additionalInfo))
                .isCurrentUser(true)
                .build();
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("프로필 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "프로필 조회에 실패했습니다."));
        }
    }
    
    /**
     * Get member profile by ID
     */
    @GetMapping("/{memberId}")
    @ResponseBody
    public ResponseEntity<?> getMemberProfile(
            @PathVariable UUID memberId,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        try {
            // Check if the requested profile is the current user's profile
            boolean isCurrentUser = currentUser != null && currentUser.getId().equals(memberId);

            // Get member profile
            MemberProfileDTO profile = memberService.getMemberProfile(memberId);

            // Get additional info
            List<MemberAddInfoEntity> additionalInfo = memberAddInfoRepository.findByMemberId(memberId);

            // Create response DTO
            MemberProfileResponseDTO response = MemberProfileResponseDTO.of(
                profile,
                additionalInfo,
                isCurrentUser
            );

            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "회원을 찾을 수 없습니다."));
        } catch (Exception e) {
            log.error("프로필 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "프로필 조회에 실패했습니다."));
        }
    }
    
    /**
     * Add or update member additional info (hashtags)
     */
    @PostMapping("/me")
    @ResponseBody
    public ResponseEntity<?> updateMemberInfo(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody Map<String, Object> memberInfo) {
        try {
            // TODO: Implement adding/updating member additional info (hashtags)
            // This should handle both adding new hashtags and removing existing ones
            return ResponseEntity.ok(Map.of("success", true, "message", "Member info updated successfully"));
        } catch (Exception e) {
            log.error("Failed to update member info", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to update member info"));
        }
    }
    
    /**
     * Update current user's profile information
     */
    @PostMapping("/me/update")
    @ResponseBody
    public ResponseEntity<?> updateProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody MemberProfileDTO profileDTO) {
        try {
            MemberProfileDTO updatedProfile = memberService.updateProfile(userDetails.getId(), profileDTO);
            return ResponseEntity.ok(updatedProfile);
        } catch (Exception e) {
            log.error("프로필 업데이트 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "프로필 업데이트에 실패했습니다."));
        }
    }
    
    /**
     * Upload profile image
     */
    @PostMapping("/me/image")
    @ResponseBody
    public ResponseEntity<?> uploadProfileImage(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "업로드할 파일이 없습니다."));
            }
            
            String imageUrl = memberService.saveProfileImage(file);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "imageUrl", imageUrl
            ));
        } catch (IOException e) {
            log.error("프로필 이미지 업로드 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "이미지 업로드에 실패했습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Check if nickname is available
     */
    @GetMapping("/check-nickname")
    @ResponseBody
    public ResponseEntity<?> checkNickname(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam String nickname) {
        try {
            // Check if nickname is valid
            if (nickname == null || nickname.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "exists", false,
                    "valid", false,
                    "message", "닉네임을 입력해주세요."
                ));
            }
            
            // Check if nickname is the same as current user's nickname
            if (userDetails != null && nickname.equals(userDetails.getUsername())) {
                return ResponseEntity.ok().body(Map.of(
                    "exists", false,
                    "valid", true,
                    "message", "현재 사용 중인 닉네임입니다.",
                    "available", true
                ));
            }
            
            // Check if nickname exists
            boolean exists = memberService.isNicknameExists(nickname);
            
            return ResponseEntity.ok().body(Map.of(
                "exists", exists,
                "valid", true,
                "message", exists ? "이미 사용 중인 닉네임입니다." : "사용 가능한 닉네임입니다.",
                "available", !exists
            ));
        } catch (Exception e) {
            log.error("닉네임 확인 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "닉네임 확인 중 오류가 발생했습니다."));
        }
    }
    
    // Legacy Web Endpoints (to be removed in future)
    
    @Deprecated(since = "2.0", forRemoval = true)
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

    /**
     * @deprecated Use GET /api/members/{memberId}/profile instead
     */
    @Deprecated(since = "2.0", forRemoval = true)
    @GetMapping("/view/{memberId}")
    public String viewMemberProfile(@PathVariable UUID memberId, Model model) {
        log.warn("Deprecated endpoint called: /member/view/{}. Please use /api/members/{}/profile instead.", memberId, memberId);
        MemberProfileDTO profile = memberService.getMemberProfile(memberId);
        model.addAttribute("profile", profile);
        return "member/profile";
    }

    // 멤버 프로필 수정 페이지
    @Deprecated(since = "2.0", forRemoval = true)
    @GetMapping("/profile/update")
    public String updateProfilePage(@AuthenticationPrincipal UserDetailsImpl userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        
        MemberProfileDTO profile = memberService.getMemberProfile(userDetails.getId());
        model.addAttribute("profile", profile);
        return "member/update-profile";
    }

    // 여행 상세 조회
    @Deprecated(since = "2.0", forRemoval = true)
    @GetMapping("/trips/{id}")
    public String tripDetail(@PathVariable UUID id, 
                           @AuthenticationPrincipal UserDetailsImpl userDetails, 
                           Model model, 
                           RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        // 여행 조회 및 소유권 확인
        Trip trip = tripService.findTripByIdAndUserId(id, userDetails.getId())
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
    @Deprecated(since = "2.0", forRemoval = true)
    @PostMapping("/profile/update")
    public ResponseEntity<?> updateProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
            @RequestPart(value = "profileData") MemberProfileDTO profileDTO) {
        
        try {
            // 1. 프로필 정보 업데이트
            MemberProfileDTO updatedProfile = memberService.updateProfile(
                userDetails.getId(), 
                profileDTO
            );
            
            // 2. 프로필 이미지가 있는 경우 별도로 업데이트
            if (profileImage != null && !profileImage.isEmpty()) {
                try {
                    String imageUrl = memberService.updateProfileImage(userDetails.getId(), profileImage);
                    updatedProfile.setProfileImageUrl(imageUrl);
                    // 세션 업데이트
                    userDetails.getMember().setProfileImageUrl(imageUrl);
                } catch (IOException e) {
                    log.error("프로필 이미지 업데이트 실패: {}", e.getMessage(), e);
                    return ResponseEntity.badRequest().body(Map.of(
                        "message", "프로필 정보는 업데이트되었지만 이미지 업로드에 실패했습니다.",
                        "error", e.getMessage()
                    ));
                }
            }
            
            return ResponseEntity.ok().body(Map.of(
                "success", true,
                "message", "프로필이 성공적으로 업데이트되었습니다.",
                "profile", updatedProfile
            ));
        } catch (Exception e) {
            log.error("프로필 업데이트 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    // 멤버의 스토리북 목록
    @Deprecated(since = "2.0", forRemoval = true)
    @GetMapping("/storybook/list")
    public String viewStorybookList() {
        return "member/storybook-list";
    }
    
    // 멤버의 좋아하는 관광지 목록
    @Deprecated(since = "2.0", forRemoval = true)
    @GetMapping("/attraction/list")
    public String viewAttractionList() {
        return "member/attraction-list";
    }
    
    // 멤버의 likedbook 목록
    @Deprecated(since = "2.0", forRemoval = true)
    @GetMapping("/likedbook/list")
    public String viewLikedbookList() {
        return "member/likedbook-list";
    }
    
    // 멤버의 여행 목록
    @Deprecated(since = "2.0", forRemoval = true)
    @GetMapping("/trip/list")
    public String viewTripList() {
        return "member/trip-list";
    }
    /**
     * 이메일 중복 확인 API
     * @param email 확인할 이메일
     * @return 중복 여부를 포함한 JSON 응답
     */
    @Deprecated(since = "2.0", forRemoval = true)
    @GetMapping("/api/check-email")
    @ResponseBody
    public Map<String, Object> checkEmail(@RequestParam("email") String email) {
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
    @Deprecated(since = "2.0", forRemoval = true)
    @GetMapping(value = "/api/check-nickname", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkNickname(
            @RequestParam(value = "nickname") String nickname) {
        // Log the raw and decoded nickname for debugging
        log.debug("Raw nickname parameter: {}", nickname);
        try {
            // URL decode the nickname in case it's double-encoded
            String decodedNickname = java.net.URLDecoder.decode(nickname, "UTF-8");
            log.debug("Decoded nickname: {}", decodedNickname);
            nickname = decodedNickname;
        } catch (java.io.UnsupportedEncodingException e) {
            log.warn("Failed to decode nickname: {}", nickname, e);
        }
        Map<String, Object> response = new HashMap<>();
        try {
            log.info("Checking nickname: {}", nickname);
            
            if (nickname == null || nickname.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "닉네임을 입력해주세요.");
                log.warn("Empty nickname provided");
                return ResponseEntity.badRequest().body(response);
            }
            
            log.debug("Calling memberService.isNicknameExists({})", nickname);
            boolean isExists = memberService.isNicknameExists(nickname);
            
            response.put("success", true);
            response.put("exists", isExists);
            response.put("message", isExists ? "이미 사용 중인 닉네임입니다." : "사용 가능한 닉네임입니다.");
            
            log.info("Nickname check completed - exists: {} for nickname: {}", isExists, nickname);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error checking nickname: " + nickname, e);
            response.put("success", false);
            response.put("message", "닉네임 확인 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
