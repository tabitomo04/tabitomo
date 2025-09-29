package com.koreatravel.tabitomo.controller.member;

import com.koreatravel.tabitomo.domain.dto.member.LanguageDTO;
import com.koreatravel.tabitomo.domain.dto.member.CountryDTO;
import com.koreatravel.tabitomo.domain.dto.member.MemberProfileDTO;
import com.koreatravel.tabitomo.domain.dto.auth.SignUpDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.koreatravel.tabitomo.config.security.UserDetailsImpl;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.koreatravel.tabitomo.service.member.AuthService;
import com.koreatravel.tabitomo.service.member.MemberService;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private final MemberService memberService;
    private final UserDetailsService userDetailsService;
    @SuppressWarnings("unused")
    private final RestTemplate restTemplate;

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
        try{
            List<LanguageDTO> languages = memberService.getAllLanguages();
            List<CountryDTO> countries = memberService.getAllCountries();
            model.addAttribute("languages", languages);
            model.addAttribute("countries", countries);
            model.addAttribute("member", new SignUpDTO());
            return "signupform";
        }catch(Exception e){
            log.error("회원가입 페이지 접근 중 오류 발생: {}", e.getMessage(), e);
            return "error";
        }
    }

    // 회원가입 처리
    @PostMapping("/signup")
    public String signup(@Valid @ModelAttribute("member") SignUpDTO member,
            @RequestParam("countryId") int countryId,
            @RequestParam("languageId") int languageId,
            BindingResult result,
            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("languages", memberService.getAllLanguages());
            model.addAttribute("countries", memberService.getAllCountries());
            return "signupform";
        }

        try {
            // Process the signup with country and language
            authService.signup(member, countryId, languageId);
            // Redirect to success page with nickname as a parameter
            String encodedNickname = URLEncoder.encode(member.getNickname() != null ? member.getNickname() : "", StandardCharsets.UTF_8);
            return "redirect:/auth/signup_success?nickname=" + encodedNickname;
        } catch (Exception e) {
            log.error("회원가입 처리 중 오류 발생: {}", e.getMessage(), e);
            model.addAttribute("languages", memberService.getAllLanguages());
            model.addAttribute("error", "회원가입 처리 중 오류가 발생했습니다.");
            return "signupform";
        }
    }

    @GetMapping("/signup_success")
    public String signupSuccess(@RequestParam(value = "nickname", required = false) String nickname, Model model) {
        // If nickname is not provided in the URL, show a generic message
        if (nickname == null || nickname.isEmpty()) {
            model.addAttribute("nickname", "회원");
        } else {
            model.addAttribute("nickname", nickname);
        }
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
            
            // Ensure both session attributes are in sync
            if (memberProfile.isQuestionnaireCompleted()) {
                session.removeAttribute("showQuestionnairePrompt");
            } else {
                session.setAttribute("showQuestionnairePrompt", true);
            }
            
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

    public ResponseEntity<?> clearQuestionnairePrompt(HttpSession session) {
        session.removeAttribute("showQuestionnairePrompt");
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/session")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getSessionData(HttpSession session) {
        Map<String, Object> sessionData = new HashMap<>();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated() && 
            !(authentication.getPrincipal() instanceof String && authentication.getPrincipal().equals("anonymousUser"))) {
            
            // Initialize variables to track questionnaire status
            boolean questionnaireCompleted = false;
            boolean showQuestionnairePrompt = true;
            String email = null;
            
            // Get user details from authentication
            if (authentication.getPrincipal() instanceof UserDetailsImpl) {
                UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                questionnaireCompleted = userDetails.isQuestionnaireCompleted();
                email = userDetails.getEmail();
                sessionData.put("email", email);
            }
            
            // Check session for overrides
            Boolean sessionQuestionnaireCompleted = (Boolean) session.getAttribute("questionnaireCompleted");
            Boolean sessionShowPrompt = (Boolean) session.getAttribute("showQuestionnairePrompt");
            
            // Use session values if they exist, otherwise use authentication values
            if (sessionQuestionnaireCompleted != null) {
                questionnaireCompleted = sessionQuestionnaireCompleted;
            }
            
            if (sessionShowPrompt != null) {
                showQuestionnairePrompt = sessionShowPrompt;
            } else {
                // Default to showing prompt if questionnaire is not completed
                showQuestionnairePrompt = !questionnaireCompleted;
            }
            
            // Ensure consistency between session and authentication
            if (authentication.getPrincipal() instanceof UserDetailsImpl) {
                UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                if (userDetails.isQuestionnaireCompleted() != questionnaireCompleted) {
                    // Update authentication if out of sync
                    UserDetailsImpl updatedUserDetails = userDetails.withQuestionnaireCompleted(questionnaireCompleted);
                    Authentication newAuth = new UsernamePasswordAuthenticationToken(
                        updatedUserDetails,
                        authentication.getCredentials(),
                        authentication.getAuthorities()
                    );
                    SecurityContextHolder.getContext().setAuthentication(newAuth);
                    log.debug("Synchronized authentication with session for questionnaire status");
                }
            }
            
            // Set response data
            sessionData.put("isAuthenticated", true);
            sessionData.put("questionnaireCompleted", questionnaireCompleted);
            sessionData.put("showQuestionnairePrompt", showQuestionnairePrompt);
            sessionData.put("sessionId", session.getId());
            
            log.debug("Session data for {} - completed: {}, showPrompt: {}", 
                email, questionnaireCompleted, showQuestionnairePrompt);
                
        } else {
            sessionData.put("isAuthenticated", false);
        }
        
        return ResponseEntity.ok(sessionData);
    }
    
    @PostMapping("/logout")
    public String logout(HttpServletRequest request) {
        // Spring Security가 처리하므로 로그아웃 로직은 불필요
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