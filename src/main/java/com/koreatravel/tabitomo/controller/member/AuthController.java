package com.koreatravel.tabitomo.controller.member;

import com.koreatravel.tabitomo.domain.dto.member.CountryDTO;
import com.koreatravel.tabitomo.domain.dto.member.LanguageDTO;
import com.koreatravel.tabitomo.domain.dto.member.MemberProfileDTO;
import com.koreatravel.tabitomo.domain.dto.auth.SignUpDTO;
import com.koreatravel.tabitomo.service.member.AuthService;
import com.koreatravel.tabitomo.service.member.MemberService;
import lombok.extern.slf4j.Slf4j;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.validation.BindingResult;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    private final MemberService memberService;

    // 로그인 페이지 이동
    @GetMapping("/login")
    public String loginPage() {
        return "loginform"; // templates/login.html
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
            @RequestParam(value = "remember-me", required = false) Boolean rememberMe,
            @RequestParam(value = "auto-login", required = false) Boolean autoLogin,
            HttpSession session,
            HttpServletResponse response,
            RedirectAttributes redirectAttributes) {

        try {
            // 서비스를 통해 로그인 처리 및 사용자 프로필 가져오기
            MemberProfileDTO memberProfile = authService.login(email, password);
            
            // 세션에 사용자 정보 저장 (MemberProfileDTO에 이미 questionnaireCompleted 필드가 있음)
            session.setAttribute("user", memberProfile);
            session.setAttribute("authenticatedEmail", email);
            session.setAttribute("questionnaireCompleted", memberProfile.isQuestionnaireCompleted());
            
            // 세션에 저장된 값 확인 로그
            log.info("Login - User: {}, Questionnaire completed: {}", email, memberProfile.isQuestionnaireCompleted());
            
            // 로그인 상태 유지 설정 (30일)
            if (Boolean.TRUE.equals(rememberMe) || Boolean.TRUE.equals(autoLogin)) {
                // 세션 만료 시간 설정 (30일)
                session.setMaxInactiveInterval(60 * 60 * 24 * 30); // 30일
                
                // 자동 로그인 쿠키 설정 (30일 유지)
                if (Boolean.TRUE.equals(autoLogin)) {
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
                    // remember-me만 체크된 경우 쿠키 삭제
                    Cookie autoLoginCookie = new Cookie("autoLogin", null);
                    autoLoginCookie.setMaxAge(0);
                    autoLoginCookie.setPath("/");
                    response.addCookie(autoLoginCookie);
                }
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
}
