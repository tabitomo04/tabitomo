package com.koreatravel.tabitomo.controller.api;

import com.koreatravel.tabitomo.domain.dto.member.MemberProfileDTO;
import com.koreatravel.tabitomo.domain.dto.member.MemberRegisterDTO;
import com.koreatravel.tabitomo.exception.DuplicateEmailException;
import com.koreatravel.tabitomo.exception.DuplicateNicknameException;
import com.koreatravel.tabitomo.exception.LoginException;
import com.koreatravel.tabitomo.service.auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Authentication controller for handling user registration, login, logout, and account management.
 * This controller works with Spring Security's session-based authentication.
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody MemberRegisterDTO registerDTO) {
        try {
            String email = authService.register(registerDTO);
            return ResponseEntity.ok()
                    .body("{\"message\":\"회원가입이 완료되었습니다. 이메일을 확인해주세요.\", \"email\":\"" + email + "\"}");
        } catch (DuplicateEmailException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("{\"error\":\"이미 사용 중인 이메일입니다.\"}");
        } catch (DuplicateNicknameException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("{\"error\":\"이미 사용 중인 닉네임입니다.\"}");
        } catch (Exception e) {
            log.error("회원가입 중 오류가 발생했습니다.", e);
            return ResponseEntity.internalServerError()
                    .body("{\"error\":\"회원가입 중 오류가 발생했습니다.\"}");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestParam String email,
            @RequestParam String password) {
        try {
            MemberProfileDTO userProfile = authService.login(email, password);
            return ResponseEntity.ok(userProfile);
        } catch (LoginException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"error\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            log.error("Login error: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("{\"error\":\"An error occurred during login\"}");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        try {
            // Logout using Spring Security's logout handler
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                new SecurityContextLogoutHandler().logout(request, null, auth);
            }
            
            // Clear the security context
            SecurityContextHolder.clearContext();
            
            // Invalidate the current session
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            
            return ResponseEntity.ok().body("{\"message\":\"Logged out successfully\"}");
        } catch (Exception e) {
            log.error("Logout error: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("{\"error\":\"An error occurred during logout\"}");
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // Check if user is not authenticated
        if (auth == null || auth instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"error\":\"Not authenticated\"}");
        }
        
        // Get the current user's profile
        Optional<MemberProfileDTO> userProfile = authService.getCurrentUserProfile();
        if (userProfile.isPresent()) {
            return ResponseEntity.ok(userProfile.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("{\"error\":\"User profile not found\"}");
        }
    }

    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        boolean isVerified = authService.verifyEmail(token);
        if (isVerified) {
            return ResponseEntity.ok()
                    .body("{\"message\":\"이메일 인증이 완료되었습니다.\"}");
        } else {
            return ResponseEntity.badRequest()
                    .body("{\"error\":\"유효하지 않거나 만료된 토큰입니다.\"}");
        }
    }

    @PostMapping("/password/reset-request")
    public ResponseEntity<?> requestPasswordReset(@RequestParam String email) {
        boolean isSent = authService.sendPasswordResetEmail(email);
        if (isSent) {
            return ResponseEntity.ok()
                    .body("{\"message\":\"비밀번호 재설정 이메일을 발송했습니다.\"}");
        } else {
            return ResponseEntity.badRequest()
                    .body("{\"error\":\"비밀번호 재설정 요청을 처리할 수 없습니다.\"}");
        }
    }

    @PostMapping("/password/reset")
    public ResponseEntity<?> resetPassword(
            @RequestParam String token,
            @RequestParam String newPassword) {
        boolean isReset = authService.resetPassword(token, newPassword);
        if (isReset) {
            return ResponseEntity.ok()
                    .body("{\"message\":\"비밀번호가 성공적으로 재설정되었습니다.\"}");
        } else {
            return ResponseEntity.badRequest()
                    .body("{\"error\":\"비밀번호 재설정에 실패했습니다.\"}");
        }
    }
}
