package com.koreatravel.tabitomo.controller;

import com.koreatravel.tabitomo.domain.dto.member.*;
import com.koreatravel.tabitomo.exception.DuplicateResourceException;
import com.koreatravel.tabitomo.exception.ResourceNotFoundException;
import com.koreatravel.tabitomo.exception.ValidationException;
import com.koreatravel.tabitomo.service.member.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.ui.Model;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

import com.koreatravel.tabitomo.PathConstants;
import com.koreatravel.tabitomo.domain.dto.member.MemberFormDTO;
import com.koreatravel.tabitomo.domain.dto.member.RegisterDTO;
import static com.koreatravel.tabitomo.PathConstants.*;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping(AUTH)
public class AuthController {
    
    private final MemberService memberService;

    /**
     * 로그인 페이지
     */
    @GetMapping(LOGIN)
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                          @RequestParam(value = "logout", required = false) String logout,
                          @RequestParam(value = "exception", required = false) String exception,
                          Model model) {
        log.info("Accessing login page");
        if (error != null) {
            model.addAttribute("errorMessage", "이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully.");
        }
        return "auth/login";
    }
    
    /**
     * 회원가입 페이지
     */
    @GetMapping(SIGNUP)
    public String signupPage(Model model) {
        log.info("Accessing signup page");
        model.addAttribute("memberForm", new MemberFormDTO());
        model.addAttribute("currentYear", java.time.Year.now().getValue());
        return "auth/signup";
    }
    
    /**
     * 회원가입 처리 (HTML Form 제출용)
     */
    @PostMapping(SIGNUP)
    public String signup(
            @Validated @ModelAttribute("memberForm") MemberFormDTO formDTO,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            log.warn("Validation errors in signup form: {}", bindingResult.getAllErrors());
            return "auth/signup";
        }

        try {
            RegisterDTO registerDTO = RegisterDTO.builder()
                    .email(formDTO.getEmail())
                    .password(formDTO.getPassword())
                    .confirmPassword(formDTO.getPasswordConfirm())
                    .nickname(formDTO.getNickname())
                    .gender(formDTO.getGender())
                    .countryId(formDTO.getCountryId())
                    .preferredLanguageId(formDTO.getPreferredLanguageId())
                    .build();
            
            memberService.register(registerDTO);
            redirectAttributes.addFlashAttribute("successMessage", "회원가입이 완료되었습니다. 로그인해주세요.");
            return "redirect:" + LOGIN;
            
        } catch (ResourceNotFoundException e) {
            log.error("Resource not found during registration: {}", e.getMessage());
            bindingResult.reject("registrationError", "요청하신 리소스를 찾을 수 없습니다. 다시 시도해주세요.");
        } catch (DuplicateResourceException e) {
            log.error("Duplicate resource during registration: {}", e.getMessage());
            bindingResult.reject("email.duplicate", e.getMessage());
        } catch (Exception e) {
            log.error("Error during registration: {}", e.getMessage(), e);
            bindingResult.reject("registrationError", "회원가입 중 오류가 발생했습니다. 다시 시도해주세요.");
        }
        
        return "auth/signup";
    }
    
    /**
     * 비밀번호 재설정을 위한 인증코드 요청
     */
    /**
     * 비밀번호 재설정 요청 (더 이상 토큰을 사용하지 않음)
     * @deprecated 토큰 기반 비밀번호 재설정은 더 이상 지원되지 않습니다.
     */
    @Deprecated
    @PostMapping(REQUEST_PASSWORD_RESET)
    @ResponseBody
    public ResponseEntity<?> requestPasswordReset(@Valid @RequestBody PasswordResetRequestDTO request, BindingResult bindingResult) {
        log.warn("Password reset via email verification is deprecated");
        return ResponseEntity.status(HttpStatus.GONE)
            .body(Map.of("success", false, "message", "이메일 인증을 통한 비밀번호 재설정은 더 이상 지원되지 않습니다."));
    }
    
    /**
     * 인증 코드 검증 (더 이상 사용되지 않음)
     * @deprecated 토큰 기반 인증은 더 이상 지원되지 않습니다.
     */
    @Deprecated
    @PostMapping(RESET_PASSWORD + "/verify")
    @ResponseBody
    public ResponseEntity<?> verifyResetCode(@Valid @RequestBody PasswordResetRequestDTO request, BindingResult bindingResult) {
        log.warn("Token-based password reset verification is deprecated");
        return ResponseEntity.status(HttpStatus.GONE)
            .body(Map.of("success", false, "message", "토큰 기반 비밀번호 재설정은 더 이상 지원되지 않습니다."));
    }
    
    /**
     * 비밀번호 재설정
     */
    /**
     * 비밀번호 재설정 (토큰 없이 이메일 기반으로만 처리)
     */
    @PostMapping(RESET_PASSWORD + "/confirm")
    @ResponseBody
    public ResponseEntity<?> resetPassword(@Valid @RequestBody PasswordResetRequestDTO request, BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "유효하지 않은 요청입니다."));
            }
            
            // 비밀번호와 확인 비밀번호 일치 확인
            if (!request.isPasswordMatching()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "비밀번호가 일치하지 않습니다."));
            }
            
            // 비밀번호 재설정 (이메일만으로 처리)
            memberService.resetPassword(request.getEmail(), request.getNewPassword());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "비밀번호가 성공적으로 변경되었습니다. 로그인 페이지로 이동합니다.",
                "redirectUrl", "/auth/login"
            ));
            
        } catch (ResourceNotFoundException e) {
            // 보안을 위해 존재하지 않는 이메일인 경우에도 성공한 것처럼 응답
            log.warn("Password reset attempt for non-existent email: {}", request.getEmail());
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "요청이 처리되었습니다. 이메일이 등록되어 있다면 비밀번호가 변경되었을 수 있습니다.",
                "redirectUrl", "/auth/login"
            ));
        } catch (Exception e) {
            log.error("Error resetting password", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "비밀번호 재설정 중 오류가 발생했습니다."));
        }
    }
    
    @PostMapping(value = "/check-email", consumes = "application/json")
    @ResponseBody
    @CrossOrigin
    public ResponseEntity<?> checkEmailAvailability(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            if (email == null || email.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "이메일을 입력해주세요.",
                    "available", false
                ));
            }
            
            boolean isAvailable = !memberService.existsByEmail(email);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "available", isAvailable,
                "message", isAvailable ? "사용 가능한 이메일입니다." : "이미 사용 중인 이메일입니다."
            ));
        } catch (Exception e) {
            log.error("Error checking email availability", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "이메일 확인 중 오류가 발생했습니다.",
                "available", false
            ));
        }
    }
    
    

    /**
     * 비밀번호 찾기 페이지
     */
    @GetMapping(REQUEST_PASSWORD_RESET)
    public String forgotPasswordPage() {
        return "auth/forgot-password";
    }

    /**
     * 비밀번호 재설정 페이지
     */
    /**
     * 비밀번호 재설정 페이지 (토큰 파라미터 제거)
     */
    @GetMapping(RESET_PASSWORD)
    public String resetPasswordPage() {
        return "auth/reset-password";
    }


    /**
     * 로그인 성공 핸들러 (Spring Security가 처리)
     */
    @GetMapping(PathConstants.LOGIN_SUCCESS)
    public String loginSuccess(HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        log.info("로그인 성공: {}", email);
        
        try {
            // Get user profile which includes nickname
            MemberProfileDTO profile = memberService.getMemberProfile(email);
            
            // Set default profile image if not set
            String profileImage = (profile.getProfileImageUrl() == null || profile.getProfileImageUrl().trim().isEmpty())
                ? "/image/default image.svg"
                : profile.getProfileImageUrl();
                
            // Store user info in session
            session.setAttribute("userEmail", email);
            session.setAttribute("userNickname", profile.getNickname());
            session.setAttribute("userProfileImage", profileImage);
            log.debug("Stored user info in session - email: {}, nickname: {}, profileImage: {}", 
                email, profile.getNickname(), profileImage);
            
        } catch (Exception e) {
            log.error("사용자 정보를 세션에 저장하는 중 오류 발생: {}", e.getMessage(), e);
            // Continue with redirect even if session storage fails
        }
        
        return "redirect:/";
    }
    
    /**
     * 로그인 실패 핸들러 (Spring Security가 처리)
     */
    @GetMapping(PathConstants.LOGIN_ERROR)
    public String loginError(HttpServletRequest request, org.springframework.ui.Model model) {
        HttpSession session = request.getSession(false);
        String errorMessage = null;
        if (session != null) {
            Exception exception = (Exception) session.getAttribute("SPRING_SECURITY_LAST_EXCEPTION");
            if (exception != null) {
                if (exception instanceof org.springframework.security.authentication.BadCredentialsException) {
                    errorMessage = "이메일 또는 비밀번호가 올바르지 않습니다.";
                } else if (exception instanceof org.springframework.security.authentication.DisabledException) {
                    errorMessage = "이메일 인증이 완료되지 않았습니다. 이메일을 확인해주세요.";
                } else {
                    errorMessage = "로그인 중 오류가 발생했습니다: " + exception.getMessage();
                }
            }
        }
        model.addAttribute("error", errorMessage != null ? errorMessage : "로그인에 실패했습니다.");
        return "auth/login.html";
    }
    
    /**
     * 로그아웃 성공 핸들러 (Spring Security가 처리)
     */
    @GetMapping(PathConstants.LOGOUT_SUCCESS)
    public String logoutSuccess(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("message", "로그아웃 되었습니다.");
        return "redirect:/";
    }

    /**
     * 비밀번호 재설정 처리
     */
    @PostMapping(RESET_PASSWORD)
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordDTO resetPasswordDTO, 
                                         BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> 
                errors.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.badRequest().body(errors);
        }

        try {
            // 비밀번호 확인 검증
            if (!resetPasswordDTO.isPasswordMatching()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "새 비밀번호가 일치하지 않습니다."));
            }
            
            // 비밀번호 재설정 (이메일과 새 비밀번호만 사용)
            memberService.resetPassword(resetPasswordDTO.getEmail(), resetPasswordDTO.getNewPassword());
            
            return ResponseEntity.ok(Map.of("message", "비밀번호가 성공적으로 변경되었습니다."));
                    
        } catch (ValidationException e) {
            log.warn("비밀번호 재설정 실패 - 유효성 검증 오류: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
                    
        } catch (Exception e) {
            log.error("비밀번호 재설정 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "비밀번호 재설정 중 오류가 발생했습니다."));
        }
    }

    /**
     * 닉네임 중복 확인 (API)
     */
    @PostMapping(PathConstants.CHECK_NICKNAME)
    @ResponseBody
    public ResponseEntity<?> checkNickname(@RequestBody Map<String, String> request) {
        try {
            String nickname = request.get("nickname");
            if (nickname == null || nickname.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "닉네임을 입력해주세요.",
                    "available", false
                ));
            }
            boolean isAvailable = memberService.isNicknameAvailable(nickname);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "available", isAvailable,
                "message", isAvailable ? "사용 가능한 닉네임입니다." : "이미 사용 중인 닉네임입니다."
            ));
        } catch (Exception e) {
            log.error("닉네임 확인 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "닉네임 확인 중 오류가 발생했습니다.",
                "available", false
            ));
        }
    }
    
    /**
     * 현재 로그인한 사용자 정보 조회 (API)
     */
    @GetMapping(PathConstants.CURRENT_USER)
    @ResponseBody
    public ResponseEntity<?> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated() || 
                authentication instanceof org.springframework.security.authentication.AnonymousAuthenticationToken) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "로그인이 필요합니다."
                ));
            }
            
            String email = authentication.getName();
            MemberProfileDTO profile = memberService.getMemberProfile(email);
            
            if (profile == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "사용자 정보를 찾을 수 없습니다."
                ));
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", profile
            ));
            
        } catch (Exception e) {
            log.error("사용자 정보 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "사용자 정보를 가져오는 중 오류가 발생했습니다."
            ));
        }
    }
    
    /**
     * 이메일 중복 확인 (API)
     */
    @PostMapping(PathConstants.CHECK_EMAIL)
    @ResponseBody
    public ResponseEntity<?> checkEmailApi(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "이메일을 입력해주세요.",
                    "available", false
                ));
            }
            
            boolean isAvailable = !memberService.existsByEmail(email);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "available", isAvailable,
                "message", isAvailable ? "사용 가능한 이메일입니다." : "이미 사용 중인 이메일입니다."
            ));
            
        } catch (Exception e) {
            log.error("이메일 확인 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "이메일 확인 중 오류가 발생했습니다.",
                "available", false
            ));
        }
    }
}
