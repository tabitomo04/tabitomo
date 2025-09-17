package com.koreatravel.tabitomo.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
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
    @ResponseBody
    public ResponseEntity<?> sendVerificationEmail(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("Received email verification request: {}", request);
            
            String email = request.get("email");
            if (email == null || email.isEmpty()) {
                String errorMsg = "이메일 주소가 필요합니다.";
                log.warn(errorMsg);
                response.put("success", false);
                response.put("message", errorMsg);
                return ResponseEntity.badRequest().body(response);
            }
            
            // 이메일 형식 검증
            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                String errorMsg = "유효하지 않은 이메일 형식입니다: " + email;
                log.warn(errorMsg);
                response.put("success", false);
                response.put("message", errorMsg);
                return ResponseEntity.badRequest().body(response);
            }
            
            String verificationCode = String.format("%06d", (int) (Math.random() * 1000000));
            log.info("Generated verification code for {}: {}", email, verificationCode);
            
            try {
                // 이메일 전송
                gmailService.sendVerificationEmail(email, verificationCode);
                log.info("Verification email sent successfully to: {}", email);
                
                // 인증 코드 저장 (실제 운영 환경에서는 Redis 등을 사용)
                verificationCodes.put(email, verificationCode);
                verificationTimes.put(email, System.currentTimeMillis());
                log.info("Verification code stored for: {}", email);
            
            // 테스트를 위해 콘솔에 인증번호 출력 (개발 환경에서만)
            log.info("이메일 인증번호 - 이메일: {}, 인증번호: {}", email, verificationCode);
            
            // 응답 반환
            response.put("success", true);
            response.put("message", "인증 이메일이 전송되었습니다.");
            // 운영 환경에서는 아래 라인을 제거하거나 보안을 위해 다른 방식으로 처리
            response.put("verificationCode", verificationCode); // 테스트용
            
            log.info("Success response sent for email: {}", email);
            return ResponseEntity.ok().body(response);
            
            } catch (Exception e) {
                String errorMsg = "이메일 전송 중 오류가 발생했습니다: " + e.getMessage();
                log.error(errorMsg, e);
                response.put("success", false);
                response.put("message", errorMsg);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
            
        } catch (Exception e) {
            String errorMsg = "요청 처리 중 예상치 못한 오류가 발생했습니다: " + e.getMessage();
            log.error(errorMsg, e);
            response.put("success", false);
            response.put("message", errorMsg);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @PostMapping("/verify")
    public ResponseEntity<?> verifyEmailCode(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String email = request.get("email");
            String inputCode = request.get("code");
            
            if (email == null || email.isEmpty() || inputCode == null || inputCode.isEmpty()) {
                response.put("success", false);
                response.put("message", "이메일 주소와 인증 코드가 필요합니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
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
            
            // 인증 코드 확인
            if (!savedCode.equals(inputCode)) {
                response.put("success", false);
                response.put("message", "인증 코드가 일치하지 않습니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 인증 성공 (실제 구현에서는 세션이나 토큰에 인증 완료 상태 저장)
            verificationCodes.remove(email);
            verificationTimes.remove(email);
            
            response.put("success", true);
            response.put("message", "이메일 인증이 완료되었습니다.");
            
            return ResponseEntity.ok().body(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "인증 처리 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
