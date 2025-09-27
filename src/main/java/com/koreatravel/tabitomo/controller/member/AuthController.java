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

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.validation.BindingResult;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private final MemberService memberService;
    @SuppressWarnings("unused")
    private final RestTemplate restTemplate;
    private final UserDetailsService userDetailsService;

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
            
            // Invalidate the current session and create a new one to prevent session fixation
            session.invalidate();
            session = request.getSession(true);
            
            // Set the new member profile in session
            session.setAttribute("memberProfile", memberProfile);
            session.setAttribute("userId", memberProfile.getId());
            session.setAttribute("authenticatedEmail", memberProfile.getEmail());
            session.setAttribute("questionnaireCompleted", memberProfile.isQuestionnaireCompleted());
            
            // Set authentication in SecurityContext
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetails(request));
            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authentication);
            SecurityContextHolder.setContext(securityContext);
            
            // Log user info
            log.info("Login - User: {}, Questionnaire completed: {}", 
                memberProfile.getEmail(), 
                memberProfile.isQuestionnaireCompleted()
            );
            
            // Set questionnaire prompt if not completed
            if (!memberProfile.isQuestionnaireCompleted()) {
                session.setAttribute("showQuestionnairePrompt", true);
                log.info("Setting showQuestionnairePrompt flag for user {}", memberProfile.getEmail());
            } else {
                session.removeAttribute("showQuestionnairePrompt");
                log.info("Questionnaire already completed for user {}", memberProfile.getEmail());
            }
            
            // Force session to be created
            session.setAttribute("sessionUpdated", System.currentTimeMillis());
            log.info("New session created - ID: {}, memberProfile: {}", session.getId(), memberProfile);
            
            // Set remember-me cookie if needed
            if (Boolean.TRUE.equals(rememberEmail)) {
                Cookie emailCookie = new Cookie("savedEmail", email);
                emailCookie.setMaxAge(60 * 60 * 24 * 365); // 1년
                emailCookie.setPath("/");
                emailCookie.setHttpOnly(true);
                emailCookie.setSecure(request.isSecure());
                response.addCookie(emailCookie);
            } else {
                // Remove email cookie if exists
                Cookie emailCookie = new Cookie("savedEmail", null);
                emailCookie.setMaxAge(0);
                emailCookie.setPath("/");
                response.addCookie(emailCookie);
            }
            
            return "redirect:/";
            
        } catch (BadCredentialsException e) {
            log.warn("Login failed for user {}: {}", email, e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/auth/login?error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("로그인 처리 중 오류 발생: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "로그인 처리 중 오류가 발생했습니다.");
            return "redirect:/auth/login?error=" + URLEncoder.encode("로그인 처리 중 오류가 발생했습니다.", StandardCharsets.UTF_8);
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