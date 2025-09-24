package com.koreatravel.tabitomo.controller.member;

import com.koreatravel.tabitomo.domain.dto.member.CountryDTO;
import com.koreatravel.tabitomo.domain.dto.member.LanguageDTO;
import com.koreatravel.tabitomo.domain.dto.member.MemberProfileDTO;
import com.koreatravel.tabitomo.domain.dto.auth.SignUpDTO;
import com.koreatravel.tabitomo.service.member.AuthService;
import com.koreatravel.tabitomo.service.member.MemberService;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.validation.BindingResult;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    private final MemberService memberService;
    private final RestTemplate restTemplate;

    // 로그인 페이지 이동
    @GetMapping("/login")
    public String loginPage(@CookieValue(value = "savedEmail", required = false) String savedEmail, Model model) {
        if (savedEmail != null && !savedEmail.isEmpty()) {
            model.addAttribute("savedEmail", savedEmail);
            model.addAttribute("rememberEmail", true);
        }
        return "loginform";
    }

    @GetMapping("/signup")
    public String signupPage(Model model) {
        List<CountryDTO> countries = memberService.getAllCountries();
        List<LanguageDTO> languages = memberService.getAllLanguages();

        model.addAttribute("countries", countries);
        model.addAttribute("languages", languages);
        model.addAttribute("member", new SignUpDTO()); // SignUpDTO 추가
        return "signupform";
    }

    // 회원가입 처리
    @PostMapping("/signup")
    public String signup(@Valid @ModelAttribute("member") SignUpDTO member,
            @RequestParam("countryId") int countryId,
            @RequestParam("languageId") int languageId,
            BindingResult result,
            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("countries", memberService.getAllCountries());
            model.addAttribute("languages", memberService.getAllLanguages());
            return "signupform";
        }

        // Process the signup with country and language
        authService.signup(member, countryId, languageId);
        return "redirect:/auth/signup_success";
    }

    @GetMapping("/signup_success")
    public String signupSuccess() {
        return "signup_success";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam(value = "remember-email", required = false) Boolean rememberEmail,
            @RequestParam(value = "remember-me", required = false) Boolean autoLogin,
            HttpSession session,
            HttpServletResponse response,
            RedirectAttributes redirectAttributes) {

        try {
            // 서비스를 통해 로그인 처리 및 사용자 프로필 가져오기
            MemberProfileDTO memberProfile = authService.login(email, password);
            
            // 세션에 최소한의 사용자 정보만 저장
            session.setAttribute("userId", memberProfile.getId());
            session.setAttribute("authenticatedEmail", email);
            // 설문조사 완료 여부는 세션에만 저장 (MemberProfileDTO 대신)
            session.setAttribute("questionnaireCompleted", memberProfile.isQuestionnaireCompleted());
            
            // 세션에 저장된 값 확인 로그
            log.info("Login - User: {}, Questionnaire completed: {}", email, memberProfile.isQuestionnaireCompleted());
            
            // 이메일 저장 쿠키 설정 (1년 유지)
            if (Boolean.TRUE.equals(rememberEmail)) {
                Cookie emailCookie = new Cookie("savedEmail", email);
                emailCookie.setMaxAge(60 * 60 * 24 * 365); // 1년
                emailCookie.setPath("/");
                response.addCookie(emailCookie);
            } else {
                // 이메일 저장 체크 해제 시 쿠키 삭제
                Cookie emailCookie = new Cookie("savedEmail", null);
                emailCookie.setMaxAge(0);
                emailCookie.setPath("/");
                response.addCookie(emailCookie);
            }
            
            // 자동 로그인 설정 (30일)
            if (Boolean.TRUE.equals(autoLogin)) {
                // 세션 만료 시간 설정 (30일)
                session.setMaxInactiveInterval(60 * 60 * 24 * 30); // 30일
                
                // 자동 로그인 토큰 생성 및 쿠키 설정 (30일 유지)
                String token = UUID.randomUUID().toString();
                // 토큰을 DB에 저장하는 로직 추가 (예: memberService.saveAutoLoginToken(email, token))
                
                // 쿠키 설정
                Cookie autoLoginCookie = new Cookie("autoLogin", token);
                autoLoginCookie.setMaxAge(60 * 60 * 24 * 30); // 30일
                autoLoginCookie.setPath("/");
                autoLoginCookie.setHttpOnly(true);
                // HTTPS 사용 시에만 secure 플래그 설정
                // autoLoginCookie.setSecure(true);
                response.addCookie(autoLoginCookie);
            } else {
                // 기본 세션 시간 (30분)
                session.setMaxInactiveInterval(60 * 30);
                
                // 쿠키 삭제
                Cookie autoLoginCookie = new Cookie("autoLogin", null);
                autoLoginCookie.setMaxAge(0);
                autoLoginCookie.setPath("/");
                response.addCookie(autoLoginCookie);
            }
            
            return "redirect:/";
            
        } catch (BadCredentialsException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/auth/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "로그인 처리 중 오류가 발생했습니다.");
            return "redirect:/auth/login";
        }
    }

    @PostMapping("/clear-questionnaire-prompt")
    public ResponseEntity<?> clearQuestionnairePrompt(HttpSession session) {
        session.removeAttribute("showQuestionnairePrompt");
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    // 인증번호 발송
    @PostMapping("/send-verification-code")
    public ResponseEntity<?> sendVerificationCode(@RequestParam String email) {
        try {
            // 이메일 유효성 검사
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", "이메일을 입력해주세요.")
                );
            }

            // 이메일 형식 검증
            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", "유효하지 않은 이메일 형식입니다.")
                );
            }

            // 기존 이메일 인증 컨트롤러의 엔드포인트 호출
            Map<String, String> request = new HashMap<>();
            request.put("email", email);
            
            // EmailVerificationController의 sendVerificationEmail 호출
            ResponseEntity<?> verificationResponse = restTemplate.postForEntity(
                "http://localhost:8080/api/email/send-verification",
                request,
                Map.class
            );

            if (verificationResponse.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> response = (Map<String, Object>) verificationResponse.getBody();
                if (response != null && Boolean.TRUE.equals(response.get("success"))) {
                    return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "인증번호가 발송되었습니다.",
                        "code", response.get("verificationCode") // 테스트용으로만 반환
                    ));
                }
            }
            
            return ResponseEntity.badRequest().body(
                Map.of("success", false, "message", "인증번호 발송에 실패했습니다.")
            );
            
        } catch (Exception e) {
            log.error("인증번호 발송 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(
                Map.of("success", false, "message", "인증번호 발송 중 오류가 발생했습니다.")
            );
        }
    }

    // 인증번호 확인
    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(
            @RequestParam String email,
            @RequestParam String code) {
        try {
            // 유효성 검사
            if (email == null || email.trim().isEmpty() || code == null || code.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", "이메일과 인증번호를 입력해주세요.")
                );
            }

            // EmailVerificationController의 verifyEmailCode 호출
            Map<String, String> request = new HashMap<>();
            request.put("email", email);
            request.put("code", code);
            
            ResponseEntity<?> verificationResponse = restTemplate.postForEntity(
                "http://localhost:8080/api/email/verify",
                request,
                Map.class
            );

            if (verificationResponse.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> response = (Map<String, Object>) verificationResponse.getBody();
                if (response != null && Boolean.TRUE.equals(response.get("success"))) {
                    // 비밀번호 재설정을 위한 임시 토큰 생성 (실제 구현에서는 JWT 등을 사용할 수 있음)
                    String resetToken = UUID.randomUUID().toString();
                    // 토큰 저장 (실제 구현에서는 Redis 등을 사용)
                    authService.storeResetToken(email, resetToken);
                    
                    return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "인증이 완료되었습니다.",
                        "token", resetToken
                    ));
                }
            }
            
            return ResponseEntity.badRequest().body(
                Map.of("success", false, "message", "잘못된 인증번호입니다.")
            );
            
        } catch (Exception e) {
            log.error("인증번호 확인 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(
                Map.of("success", false, "message", "인증번호 확인 중 오류가 발생했습니다.")
            );
        }
    }

    // 비밀번호 재설정
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @RequestParam String email,
            @RequestParam String token,
            @RequestParam String newPassword) {
        try {
            boolean success = authService.verifyAndResetPassword(email, token, newPassword);
            
            if (success) {
                return ResponseEntity.ok().body(Map.of(
                    "success", true,
                    "message", "비밀번호가 성공적으로 재설정되었습니다."
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "비밀번호 재설정에 실패했습니다. 토큰이 유효하지 않거나 만료되었습니다."
                ));
            }
            
        } catch (Exception e) {
            log.error("비밀번호 재설정 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "서버 오류가 발생했습니다."
            ));
        }
    }
}
