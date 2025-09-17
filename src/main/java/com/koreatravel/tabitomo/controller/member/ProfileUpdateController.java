package com.koreatravel.tabitomo.controller.member;

import com.koreatravel.tabitomo.config.security.UserDetailsImpl;
import com.koreatravel.tabitomo.mail.GmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
public class ProfileUpdateController {
    private final GmailService gmailService;
    // 메모리 내 인증 코드 저장 (실제 운영 환경에서는 Redis 등을 사용하는 것이 좋습니다.)
    private final Map<String, String> verificationCodes = new ConcurrentHashMap<>();
    private final Map<String, Long> verificationTimes = new ConcurrentHashMap<>();
    private static final long VERIFICATION_CODE_EXPIRATION_MS = 3 * 60 * 1000; // 3분

    /**
     * 프로필 이미지 업로드
     */
    @PostMapping("/profile/image")
    public ResponseEntity<?> updateProfileImage(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam("file") MultipartFile file) {
        try {
            // TODO: Implement profile image update logic
            return ResponseEntity.ok().body(Map.of("message", "Profile image update not implemented yet"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "이미지 업로드에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 이메일 인증 코드 전송
     */
    @PostMapping("/email/verification/send")
    public ResponseEntity<?> sendVerificationCode(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam String email) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 이메일 형식 검증
            if (email == null || email.isEmpty()) {
                response.put("success", false);
                response.put("message", "이메일 주소가 필요합니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                response.put("success", false);
                response.put("message", "유효하지 않은 이메일 형식입니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            String verificationCode = String.format("%06d", (int) (Math.random() * 1000000));
            
            // 이메일 전송
            gmailService.sendVerificationEmail(email, verificationCode);
            
            // 인증 코드 저장 (실제 운영 환경에서는 Redis 등을 사용)
            verificationCodes.put(email, verificationCode);
            verificationTimes.put(email, System.currentTimeMillis());
            
            // 테스트를 위해 콘솔에 인증번호 출력
            System.out.println("이메일: " + email + ", 인증번호: " + verificationCode);
            
            response.put("success", true);
            response.put("message", "인증 코드가 전송되었습니다.");
            return ResponseEntity.ok().body(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "인증 코드 전송에 실패했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
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
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 저장된 인증 코드와 시간 조회
            String savedCode = verificationCodes.get(email);
            Long verificationTime = verificationTimes.get(email);
            
            // 인증 코드가 존재하고, 만료되지 않았는지 확인
            if (savedCode == null || verificationTime == null) {
                response.put("success", false);
                response.put("message", "인증 요청을 먼저 해주세요.");
                return ResponseEntity.badRequest().body(response);
            }
            
            long currentTime = System.currentTimeMillis();
            if (currentTime - verificationTime > VERIFICATION_CODE_EXPIRATION_MS) {
                // 인증 코드 만료
                verificationCodes.remove(email);
                verificationTimes.remove(email);
                response.put("success", false);
                response.put("message", "인증 시간이 만료되었습니다. 다시 시도해주세요.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 인증 코드 일치 여부 확인
            boolean isVerified = savedCode.equals(code);
            
            if (isVerified) {
                // 인증 성공 시 인증 정보 삭제 (1회용)
                verificationCodes.remove(email);
                verificationTimes.remove(email);
                response.put("success", true);
                response.put("message", "이메일 인증이 완료되었습니다.");
                return ResponseEntity.ok().body(response);
            } else {
                response.put("success", false);
                response.put("message", "인증 코드가 일치하지 않습니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "인증 처리 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }


    /**
     * 닉네임 중복 확인
     */
    @GetMapping("/nickname/check")
    public ResponseEntity<?> checkNickname(@RequestParam String nickname) {
        // TODO: Implement nickname check logic
        // boolean exists = memberService.isNicknameExists(nickname);
        return ResponseEntity.ok().body(Map.of("exists", false));
    }

    /**
     * 프로필 업데이트
     */
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody Map<String, Object> profileData) {
        try {
            // TODO: Implement profile update logic
            // MemberEntity updatedMember = memberService.updateProfile(
            //         userDetails.getMemberId(),
            //         profileData,
            //         null
            // );
            return ResponseEntity.ok().body(Map.of(
                    "message", "프로필이 업데이트되었습니다.",
                    "profile", profileData
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "프로필 업데이트에 실패했습니다: " + e.getMessage()));
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