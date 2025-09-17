package com.koreatravel.tabitomo.mail;

import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
public class EmailVerificationController {

    private final GmailService gmailService;
    
    // 메모리 내 인증 코드 저장 (실제 운영 환경에서는 Redis 등을 사용하는 것이 좋습니다.)
    private final Map<String, String> verificationCodes = new ConcurrentHashMap<>();
    private final Map<String, Long> verificationTimes = new ConcurrentHashMap<>();
    private static final long VERIFICATION_CODE_EXPIRATION_MS = 3 * 60 * 1000; // 3분

    @PostMapping("/send-verification")
    public ResponseEntity<?> sendVerificationEmail(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            if (email == null || email.isEmpty()) {
                return ResponseEntity.badRequest().body("이메일 주소가 필요합니다.");
            }
            
            String verificationCode = String.format("%06d", (int) (Math.random() * 1000000));
            
            // 이메일 전송
            gmailService.sendVerificationEmail(email, verificationCode);
            
            // 인증 코드 저장 (실제 운영 환경에서는 Redis 등을 사용)
            verificationCodes.put(email, verificationCode);
            verificationTimes.put(email, System.currentTimeMillis());
            
            // 테스트를 위해 콘솔에 인증번호 출력
            System.out.println("이메일: " + email + ", 인증번호: " + verificationCode);
            
            // 응답 반환 (테스트를 위해 인증 코드도 함께 반환)
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "인증 이메일이 전송되었습니다.");
            response.put("verificationCode", verificationCode); // 테스트용 (실제 운영에서는 제거)
            
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("인증 이메일 전송에 실패했습니다: " + e.getMessage());
        }
    }
    
    @PostMapping("/verify")
    public ResponseEntity<?> verifyEmailCode(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String inputCode = request.get("code");
            
            if (email == null || email.isEmpty() || inputCode == null || inputCode.isEmpty()) {
                return ResponseEntity.badRequest().body("이메일 주소와 인증 코드가 필요합니다.");
            }
            
            // 저장된 인증 코드와 시간 조회
            String savedCode = verificationCodes.get(email);
            Long verificationTime = verificationTimes.get(email);
            
            // 인증 코드가 존재하고, 만료되지 않았는지 확인
            if (savedCode == null || verificationTime == null) {
                return ResponseEntity.badRequest().body("인증 요청을 먼저 해주세요.");
            }
            
            long currentTime = System.currentTimeMillis();
            if (currentTime - verificationTime > VERIFICATION_CODE_EXPIRATION_MS) {
                // 인증 코드 만료
                verificationCodes.remove(email);
                verificationTimes.remove(email);
                return ResponseEntity.badRequest().body("인증 시간이 만료되었습니다. 다시 시도해주세요.");
            }
            
            // 인증 코드 확인
            if (!savedCode.equals(inputCode)) {
                return ResponseEntity.badRequest().body("인증 코드가 일치하지 않습니다.");
            }
            
            // 인증 성공 (실제 구현에서는 세션이나 토큰에 인증 완료 상태 저장)
            verificationCodes.remove(email);
            verificationTimes.remove(email);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "이메일 인증이 완료되었습니다.");
            
            return ResponseEntity.ok().body(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("인증 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}
