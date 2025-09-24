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
import java.util.UUID;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.validation.BindingResult;
import org.springframework.web.client.RestTemplate;

import org.springframework.web.bind.annotation.CookieValue;


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
    public String loginPage(@CookieValue(value = "savedEmail", required = false) String savedEmail,
                          @RequestParam(value = "error", required = false) String error,
                          Model model) {
        if (savedEmail != null && !savedEmail.isEmpty()) {
            model.addAttribute("savedEmail", savedEmail);
            model.addAttribute("rememberEmail", true);
        }
        
        // 에러 파라미터가 있는 경우 모델에 추가
        if (error != null && !error.isEmpty()) {
            model.addAttribute("error", error);
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
            HttpServletRequest request,
            HttpSession session,
            HttpServletResponse response,
            RedirectAttributes redirectAttributes) {

        try {
            // 서비스를 통해 로그인 처리 및 사용자 프로필 가져오기
            MemberProfileDTO memberProfile = authService.login(email, password);
            
            // 세션 무효화 후 새 세션 생성 (기존 세션 정리)
            session.invalidate();
            session = request.getSession(true);
            
            // 세션에 MemberProfileDTO 저장
            session.setAttribute("memberProfile", memberProfile);
            
            // 기존에 개별로 저장하던 속성 제거
            session.removeAttribute("userId");
            session.removeAttribute("authenticatedEmail");
            session.removeAttribute("questionnaireCompleted");
            
            // 세션에 저장된 값 확인 로그
            log.info("Login - User: {}, Questionnaire completed: {}", 
                memberProfile.getEmail(), 
                memberProfile.isQuestionnaireCompleted()
            );
            
            // 설문조사 프롬프트 표시 여부 설정
            if (!memberProfile.isQuestionnaireCompleted()) {
                session.setAttribute("showQuestionnairePrompt", true);
                log.info("Setting showQuestionnairePrompt flag for user {}", memberProfile.getEmail());
            } else {
                session.removeAttribute("showQuestionnairePrompt");
                log.info("Questionnaire already completed for user {}", memberProfile.getEmail());
            }
            
            // 세션 속성 강제 저장
            session.setAttribute("sessionUpdated", System.currentTimeMillis());
            log.info("Session created - ID: {}", session.getId());
            
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
    
    // 비밀번호 재설정
    @PostMapping("/reset-password")
    @ResponseBody
    public ResponseEntity<?> resetPassword(
            @RequestParam("email") String email,
            @RequestParam("newPassword") String newPassword) {
        try {
            boolean success = authService.resetPassword(email, newPassword);
            
            if (success) {
                return ResponseEntity.ok().body(Map.of(
                    "success", true,
                    "message", "비밀번호가 성공적으로 재설정되었습니다."
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "비밀번호 재설정에 실패했습니다."
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