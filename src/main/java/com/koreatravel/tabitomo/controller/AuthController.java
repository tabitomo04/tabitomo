package com.koreatravel.tabitomo.controller;

import com.koreatravel.tabitomo.domain.dto.member.*;
import com.koreatravel.tabitomo.exception.DuplicateResourceException;
import com.koreatravel.tabitomo.exception.UnauthorizedException;
import com.koreatravel.tabitomo.exception.ValidationException;
import com.koreatravel.tabitomo.service.member.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.koreatravel.tabitomo.PathConstants;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping(PathConstants.AUTH)
public class AuthController {
    
    private final MemberService memberService;

    /**
     * 로그인 페이지
     */
    @GetMapping(PathConstants.LOGIN)
    public String loginPage() {
        return "auth/login";
    }

    /**
     * 회원가입 페이지
     */
    @GetMapping(PathConstants.SIGNUP)
    public String signupPage() {
        return "auth/signup";
    }

    /**
     * 비밀번호 찾기 페이지
     */
    @GetMapping(PathConstants.REQUEST_PASSWORD_RESET)
    public String forgotPasswordPage() {
        return "auth/forgot-password";
    }

    /**
     * 비밀번호 재설정 페이지
     */
    @GetMapping(PathConstants.RESET_PASSWORD)
    public String resetPasswordPage(@RequestParam String token, org.springframework.ui.Model model) {
        model.addAttribute("token", token);
        return "auth/reset-password";
    }


    /**
     * 회원가입 처리
     */
    @PostMapping(PathConstants.SIGNUP)
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
    @GetMapping(PathConstants.VERIFY_EMAIL)
    public String verifyEmail(@RequestParam String token, RedirectAttributes redirectAttributes) {
        try {
            memberService.verifyEmail(token);
            redirectAttributes.addFlashAttribute("message", "이메일 인증이 완료되었습니다. 로그인해주세요.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/login";
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
     * 비밀번호 재설정 요청
     */
    @PostMapping(PathConstants.REQUEST_PASSWORD_RESET)
    public ResponseEntity<?> requestPasswordReset(@RequestBody Map<String, String> request) {
        try {
            memberService.requestPasswordReset(request.get("email"));
            return ResponseEntity.ok(Map.of("message", "비밀번호 재설정 링크가 이메일로 전송되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * 비밀번호 재설정 처리
     */
    @PostMapping(PathConstants.RESET_PASSWORD)
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
     * 이메일 중복 확인
     */
    @PostMapping(PathConstants.CHECK_EMAIL)
    public ResponseEntity<?> checkEmail(@RequestBody Map<String, String> request) {
        boolean isAvailable = memberService.isEmailAvailable(request.get("email"));
        return ResponseEntity.ok(Map.of("available", isAvailable));
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
