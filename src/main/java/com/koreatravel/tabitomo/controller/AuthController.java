package com.koreatravel.tabitomo.controller;

import com.koreatravel.tabitomo.domain.dto.member.*;
import com.koreatravel.tabitomo.exception.DuplicateResourceException;
import com.koreatravel.tabitomo.exception.ResourceNotFoundException;
import com.koreatravel.tabitomo.exception.UnauthorizedException;
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
    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("memberForm", new MemberFormDTO());
        return "auth/signup";
    }
    
    /**
     * 회원가입 처리
     */
    @PostMapping("/signup")
    public String signup(
            @Validated @ModelAttribute("memberForm") MemberFormDTO formDTO,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        
        // Validate form data
        if (bindingResult.hasErrors()) {
            log.warn("Validation errors in signup form: {}", bindingResult.getAllErrors());
            return "auth/signup";
        }

        try {
            // Convert form DTO to register DTO
            RegisterDTO registerDTO = RegisterDTO.builder()
                    .email(formDTO.getEmail())
                    .password(formDTO.getPassword())
                    .confirmPassword(formDTO.getPasswordConfirm())
                    .nickname(formDTO.getNickname())
                    .gender(formDTO.getGender())
                    .countryId(formDTO.getCountryId())
                    .preferredLanguageId(formDTO.getPreferredLanguageId())
                    .build();
            
            try {
                // Call the service to register the user
                memberService.register(registerDTO);
                
                redirectAttributes.addFlashAttribute("successMessage", "회원가입이 완료되었습니다. 이메일을 확인해주세요.");
                return "redirect:/auth/login";
            } catch (ResourceNotFoundException e) {
                log.error("Resource not found during registration: {}", e.getMessage());
                bindingResult.reject("registrationError", "요청하신 리소스를 찾을 수 없습니다. 다시 시도해주세요.");
                return "auth/signup";
            } catch (Exception e) {
                log.error("Error during registration: {}", e.getMessage(), e);
                bindingResult.reject("registrationError", "회원가입 중 오류가 발생했습니다. 다시 시도해주세요.");
                return "auth/signup";
            }
            
        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation during signup: {}", e.getMessage());
            bindingResult.reject("email.duplicate", "이미 사용 중인 이메일입니다.");
            return "auth/signup";
        } catch (Exception e) {
            log.error("Error during signup: {}", e.getMessage(), e);
            bindingResult.reject("signup.failed", "회원가입 중 오류가 발생했습니다. 다시 시도해주세요.");
            return "auth/signup";
        }
    }
    
    /**
     * 회원가입 페이지
     */
    
    /**
     * 비밀번호 재설정을 위한 인증코드 요청
     */
    @PostMapping(REQUEST_PASSWORD_RESET)
    @ResponseBody
    public ResponseEntity<?> requestPasswordReset(@Valid @RequestBody PasswordResetRequestDTO request, BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "유효하지 않은 이메일 형식입니다."));
            }
            
            // 인증코드 생성 및 저장 (이메일로 전송)
            String verificationCode = memberService.generateAndSaveVerificationCode(request.getEmail());
            
            // TODO: 실제 프로덕션에서는 이메일로 인증코드 전송
            log.info("Password reset verification code for {}: {}", request.getEmail(), verificationCode);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "인증번호가 이메일로 전송되었습니다.",
                "verificationCode", verificationCode // 테스트용으로 코드 반환 (실제로는 제거)
            ));
            
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "가입되지 않은 이메일입니다."));
        } catch (Exception e) {
            log.error("Error requesting password reset", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "인증번호 전송 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 인증 코드 검증
     */
    @PostMapping(RESET_PASSWORD + "/verify")
    @ResponseBody
    public ResponseEntity<?> verifyResetCode(@Valid @RequestBody PasswordResetRequestDTO request, BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "이메일과 인증번호를 확인해주세요."));
            }
            
            boolean isValid = memberService.verifyCode(request.getEmail(), request.getToken());
            
            if (!isValid) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "유효하지 않은 인증번호이거나 만료되었습니다."));
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "인증이 완료되었습니다."
            ));
            
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "가입되지 않은 이메일입니다."));
        } catch (Exception e) {
            log.error("Error verifying reset code", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "인증번호 확인 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 비밀번호 재설정
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
            
            // 인증 코드 검증
            if (!memberService.verifyCode(request.getEmail(), request.getToken())) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "유효하지 않은 인증번호이거나 만료되었습니다."));
            }
            
            // 비밀번호 재설정
            memberService.resetPassword(request.getEmail(), request.getNewPassword());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "비밀번호가 성공적으로 변경되었습니다. 로그인 페이지로 이동합니다.",
                "redirectUrl", "/auth/login"
            ));
            
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "가입되지 않은 이메일입니다."));
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
    
    @GetMapping(SIGNUP)
    public String signupPage(Model model) {
        log.info("Accessing signup page");
        
        // Add any model attributes needed for the signup form
        model.addAttribute("currentYear", java.time.Year.now().getValue());
        
        // Log template resolution
        log.debug("Resolving template: auth/signup");
        String templatePath = "auth/signup";
        log.debug("Full template path: classpath:/templates/" + templatePath + ".html");
        
        return templatePath;
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
    @GetMapping(RESET_PASSWORD)
    public String resetPasswordPage(@RequestParam String token, org.springframework.ui.Model model) {
        model.addAttribute("token", token);
        return "auth/reset-password";
    }


    /**
     * 회원가입 처리
     */
    @PostMapping(SIGNUP)
    public ResponseEntity<?> register(
            @Valid @RequestBody RegisterDTO registerDTO,
            BindingResult bindingResult) {
        
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.badRequest().body(errors);
        }
        
        try {
            // 비밀번호 확인 검증
            if (!registerDTO.isPasswordMatching()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "비밀번호가 일치하지 않습니다."));
            }
            
            memberService.register(registerDTO);
            return ResponseEntity.ok(Map.of("message", "회원가입이 완료되었습니다. 이메일을 확인해주세요."));
        } catch (DuplicateResourceException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            log.error("회원가입 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "회원가입 처리 중 오류가 발생했습니다."));
        }
    }

    /**
     * 이메일 인증 처리
     */
    @GetMapping(VERIFY_EMAIL)
    public String verifyEmail(
            @RequestParam String email,
            @RequestParam String code,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        try {
            if (memberService.verifyEmail(email, code)) {
                // Set the authenticated user's email in session
                session.setAttribute("authenticatedEmail", email);
                redirectAttributes.addFlashAttribute("message", "이메일 인증이 완료되었습니다. 여행 성향 테스트를 진행해주세요.");
                return "redirect:" + PathConstants.QUESTION_START;
            } else {
                redirectAttributes.addFlashAttribute("error", "유효하지 않은 인증 코드입니다.");
                return "redirect:/login";
            }
        } catch (Exception e) {
            log.error("이메일 인증 중 오류 발생: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/login";
        }
    }

    /**
     * 로그인 성공 핸들러 (Spring Security가 처리)
     */
    @GetMapping(PathConstants.LOGIN_SUCCESS)
    public String loginSuccess() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("로그인 성공: {}", auth.getName());
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
        return "redirect:/login";
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
            
            memberService.resetPassword(resetPasswordDTO);
            return ResponseEntity.ok(Map.of("message", "비밀번호가 성공적으로 변경되었습니다."));
            
        } catch (UnauthorizedException e) {
            log.warn("비밀번호 재설정 실패 - 인증 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", e.getMessage()));
                    
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
     * 닉네임 중복 확인
     */
    @PostMapping(PathConstants.CHECK_NICKNAME)
    public ResponseEntity<?> checkNickname(@RequestBody Map<String, String> request) {
        boolean isAvailable = memberService.isNicknameAvailable(request.get("nickname"));
        return ResponseEntity.ok(Map.of("available", isAvailable));
    }
}
