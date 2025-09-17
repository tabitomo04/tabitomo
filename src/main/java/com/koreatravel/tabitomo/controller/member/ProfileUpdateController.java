package com.koreatravel.tabitomo.controller.member;

import com.koreatravel.tabitomo.config.security.UserDetailsImpl;
import com.koreatravel.tabitomo.domain.dto.member.MemberProfileDTO;
import com.koreatravel.tabitomo.domain.entity.member.MemberEntity;
import com.koreatravel.tabitomo.service.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
public class ProfileUpdateController {
    private static final Logger log = LoggerFactory.getLogger(ProfileUpdateController.class);

    private final MemberService memberService;

    /**
     * 프로필 이미지 업로드
     */
    @PostMapping("/profile/image")
    public ResponseEntity<?> uploadProfileImage(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = memberService.saveProfileImage(file);
            return ResponseEntity.ok().body(Map.of("imageUrl", imageUrl));
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "이미지 업로드에 실패했습니다."));
        }
    }

    /**
     * 이메일 인증 코드 전송
     */
    @PostMapping("/email/verification/send")
    public ResponseEntity<?> sendVerificationCode(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam String email) {
        try {
            // TODO: 이메일 인증 코드 전송 로직 구현
            return ResponseEntity.ok().body(Map.of("message", "인증 코드가 전송되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "인증 코드 전송에 실패했습니다."));
        }
    }

    /**
     * 이메일 인증 코드 검증
     */
    @PostMapping("/email/verification/verify")
    public ResponseEntity<?> verifyEmailCode(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam String email,
            @RequestParam String code) {
        try {
            // TODO: 이메일 인증 코드 검증 로직 구현
            boolean isValid = true; // 임시로 항상 true 반환
            if (isValid) {
                return ResponseEntity.ok().body(Map.of("message", "이메일 인증이 완료되었습니다."));
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "인증 코드가 일치하지 않습니다."));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "이메일 인증에 실패했습니다."));
        }
    }

    /**
     * 닉네임 중복 확인
     */
    @GetMapping("/nickname/check")
    public ResponseEntity<?> checkNickname(@RequestParam String nickname) {
        boolean exists = memberService.isNicknameExists(nickname);
        return ResponseEntity.ok().body(Map.of("exists", exists));
    }

    /**
     * 프로필 업데이트
     */
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody MemberProfileDTO profileDTO) {
        try {
            MemberEntity updatedMember = memberService.updateProfile(
                    userDetails.getMemberId(),
                    profileDTO,
                    null
            );
            return ResponseEntity.ok().body(Map.of(
                    "message", "프로필이 업데이트되었습니다.",
                    "profile", profileDTO
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "프로필 업데이트에 실패했습니다."));
        }
    }

    /**
     * 개인정보 설정 업데이트 (성별/나이 공개 여부)
     */
    @PutMapping("/privacy-settings")
    public ResponseEntity<?> updatePrivacySettings(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(required = false) Boolean showGender,
            @RequestParam(required = false) Boolean showAge) {
        try {
            // TODO: 개인정보 설정 업데이트 로직 구현
            return ResponseEntity.ok().body(Map.of("message", "개인정보 설정이 업데이트되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "개인정보 설정 업데이트에 실패했습니다."));
        }
    }
}