package com.koreatravel.tabitomo.service.auth;

import com.koreatravel.tabitomo.config.security.MemberDetails;
import com.koreatravel.tabitomo.domain.dto.member.MemberProfileDTO;
import com.koreatravel.tabitomo.domain.dto.member.MemberRegisterDTO;
import com.koreatravel.tabitomo.domain.entity.member.MemberEntity;
import com.koreatravel.tabitomo.exception.DuplicateEmailException;
import com.koreatravel.tabitomo.exception.DuplicateNicknameException;
import com.koreatravel.tabitomo.exception.InvalidTokenException;
import com.koreatravel.tabitomo.exception.LoginException;
import com.koreatravel.tabitomo.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service implementation for authentication and authorization operations.
 * Handles user registration, login, email verification, and password reset functionality.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    
    private static final String DEFAULT_ROLE = "ROLE_USER";
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final Duration LOGIN_ATTEMPT_WINDOW = Duration.ofMinutes(15);
    private static final Duration PASSWORD_RESET_TOKEN_EXPIRY = Duration.ofHours(24);
    private static final Duration EMAIL_VERIFICATION_TOKEN_EXPIRY = Duration.ofDays(7);
    
    // In-memory store for login attempts (in production, consider using Redis)
    private final ConcurrentHashMap<String, LoginAttempt> loginAttempts = new ConcurrentHashMap<>();
    
    @Value("${app.security.password.min-length:8}")
    private int minPasswordLength;
    
    @Value("${app.security.password.max-length:20}")
    private int maxPasswordLength;

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public String register(MemberRegisterDTO registerDTO) throws DuplicateEmailException, DuplicateNicknameException {
        log.debug("Registering new user with email: {}", registerDTO.getEmail());
        
        // Validate email format
        if (!isValidEmail(registerDTO.getEmail())) {
            log.warn("Invalid email format: {}", registerDTO.getEmail());
            throw new IllegalArgumentException("error.auth.invalid.email");
        }
        
        // Check email availability
        if (memberRepository.existsByEmail(registerDTO.getEmail())) {
            log.warn("Email already in use: {}", registerDTO.getEmail());
            throw new DuplicateEmailException("error.auth.email.exists");
        }

        // Check nickname availability
        if (memberRepository.existsByNickname(registerDTO.getNickname())) {
            log.warn("Nickname already in use: {}", registerDTO.getNickname());
            throw new DuplicateNicknameException("error.auth.nickname.exists");
        }

        // Validate password
        validatePassword(registerDTO.getPassword(), registerDTO.getPasswordConfirm());

        // Convert gender from String to Integer
        Integer genderValue = null;
        if (registerDTO.getGender() != null) {
            switch (registerDTO.getGender().toUpperCase()) {
                case "MALE":
                    genderValue = 1;
                    break;
                case "FEMALE":
                    genderValue = 2;
                    break;
                case "OTHER":
                    genderValue = 3;
                    break;
                case "PREFER_NOT_TO_SAY":
                    genderValue = 4;
                    break;
                default:
                    genderValue = null; // or set a default value if needed
            }
        }

        // 회원 엔티티 생성 및 저장
        MemberEntity member = MemberEntity.builder()
                .email(registerDTO.getEmail())
                .password(passwordEncoder.encode(registerDTO.getPassword()))
                .nickname(registerDTO.getNickname())
                .gender(genderValue)
                .isActive(true) // 기본적으로 활성 상태로 설정
                .emailVerified(false) // 이메일 인증 전
                .emailVerificationToken(generateEmailVerificationToken())
                .build();

        // TODO: 국가 및 선호 언어 설정 추가

        memberRepository.save(member);
        
        // TODO: 이메일 인증 메일 발송 로직 추가
        
        return member.getEmail();
    }

    @Override
    @Transactional(readOnly = true)
    public MemberProfileDTO login(String email, String password) throws LoginException {
        checkLoginAttempts(email);
        
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            // Clear login attempts on successful authentication
            loginAttempts.remove(email.toLowerCase());
            
            // Set authentication in security context
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Get user profile
            MemberEntity member = memberRepository.findByEmail(email)
                    .orElseThrow(() -> new LoginException("error.auth.user.not.found"));

            // Check if email is verified
            if (!member.isEmailVerified()) {
                log.warn("Login attempt with unverified email: {}", email);
                throw new LoginException("error.auth.email.not.verified");
            }

            // Check if account is active
            if (!member.isActive()) {
                log.warn("Login attempt to inactive account: {}", email);
                throw new LockedException("error.auth.account.inactive");
            }

            // Update last login time
            member.setLastLoginTime(LocalDateTime.now());
            memberRepository.save(member);
            
            log.info("User logged in successfully: {}", email);
            
            // Return user profile
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            if (userDetails instanceof MemberDetails) {
                return ((MemberDetails) userDetails).getProfile();
            }
            
            return MemberProfileDTO.builder()
                    .id(member.getId())
                    .email(member.getEmail())
                    .nickname(member.getNickname())
                    .profileImageUrl(member.getProfileImageUrl())
                    .gender(member.getGenderAsString())
                    .build();

        } catch (AuthenticationException e) {
            handleFailedLoginAttempt(email);
            log.warn("Authentication failed for user: {}", email, e);
            throw new BadCredentialsException("error.auth.invalid.credentials");
        } catch (Exception e) {
            log.error("Unexpected error during login for user: {}", email, e);
            throw new LoginException("error.auth.login.failed");
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<MemberProfileDTO> getCurrentUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() || 
            !(authentication.getPrincipal() instanceof UserDetails)) {
            return Optional.empty();
        }
        
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        if (userDetails instanceof MemberDetails) {
            return Optional.ofNullable(((MemberDetails) userDetails).getProfile());
        }
        
        // Fallback to loading from repository if not MemberDetails
        return memberRepository.findByEmail(userDetails.getUsername())
                .map(member -> MemberProfileDTO.builder()
                        .id(member.getId())
                        .email(member.getEmail())
                        .nickname(member.getNickname())
                        .profileImageUrl(member.getProfileImageUrl())
                        .gender(member.getGenderAsString())
                        .build());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<MemberEntity> loadUserByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    @Override
    public void logout() {
        SecurityContextHolder.clearContext();
    }

    @Override
    @Transactional
    public boolean verifyEmail(String token) {
        if (token == null || token.length() != 36) { // UUID length
            log.warn("Invalid email verification token format");
            throw new InvalidTokenException("error.auth.invalid.token");
        }
        
        return memberRepository.findByEmailVerifyToken(token)
                .map(member -> {
                    // Check if token is expired
                    if (member.getEmailVerifyTokenExpiry() != null && 
                        member.getEmailVerifyTokenExpiry().isBefore(LocalDateTime.now())) {
                        log.warn("Expired email verification token for user: {}", member.getEmail());
                        throw new InvalidTokenException("error.auth.token.expired");
                    }
                    
                    // Already verified
                    if (member.isEmailVerified()) {
                        log.info("Email already verified for user: {}", member.getEmail());
                        return true;
                    }
                    
                    // Verify email
                    member.verifyEmail();
                    member.clearEmailVerificationToken();
                    memberRepository.save(member);
                    
                    log.info("Email verified successfully for user: {}", member.getEmail());
                    return true;
                })
                .orElseThrow(() -> {
                    log.warn("No user found with verification token");
                    return new InvalidTokenException("error.auth.invalid.token");
                });
    }

    @Override
    public boolean sendPasswordResetEmail(String email) {
        log.info("Sending password reset email to: {}", email);
        
        return memberRepository.findByEmail(email)
                .map(member -> {
                    String resetToken = generateEmailVerificationToken();
                    LocalDateTime expiryTime = LocalDateTime.now().plus(PASSWORD_RESET_TOKEN_EXPIRY);
                    
                    member.setPasswordResetToken(resetToken);
                    member.setPasswordResetExpiry(expiryTime);
                    memberRepository.save(member);
                    
                    // In production, send an actual email with the reset link
                    String resetLink = String.format("/auth/reset-password?token=%s", resetToken);
                    log.info("Password reset link for {}: {}", email, resetLink);
                    
                    // TODO: Implement email sending logic
                    // emailService.sendPasswordResetEmail(member.getEmail(), resetLink);
                    
                    return true;
                })
                .orElseGet(() -> {
                    // Return true even if email doesn't exist to prevent email enumeration
                    log.debug("Password reset requested for non-existent email: {}", email);
                    return true;
                });
    }

    @Override
    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        if (token == null || token.length() != 36) { // UUID length
            log.warn("Invalid password reset token format");
            throw new InvalidTokenException("error.auth.invalid.token");
        }
        
        validatePassword(newPassword, newPassword); // Validate new password
        
        return memberRepository.findByPasswordResetToken(token, LocalDateTime.now())
                .map(member -> {
                    // Check if token is expired
                    if (member.getPasswordResetExpiry() != null && 
                        member.getPasswordResetExpiry().isBefore(LocalDateTime.now())) {
                        log.warn("Expired password reset token for user: {}", member.getEmail());
                        throw new InvalidTokenException("error.auth.token.expired");
                    }
                    
                    // Update password and clear reset token
                    member.updatePassword(passwordEncoder.encode(newPassword));
                    member.clearPasswordResetToken();
                    memberRepository.save(member);
                    
                    log.info("Password reset successfully for user: {}", member.getEmail());
                    return true;
                })
                .orElseThrow(() -> {
                    log.warn("Invalid or expired password reset token");
                    return new InvalidTokenException("error.auth.invalid.token");
                });
    }

    // Helper methods
    
    private void validatePassword(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("error.auth.password.mismatch");
        }
        
        if (password.length() < minPasswordLength || password.length() > maxPasswordLength) {
            throw new IllegalArgumentException(
                String.format("error.auth.password.length", minPasswordLength, maxPasswordLength)
            );
        }
        
        // Add more password strength validation as needed
    }
    
    private boolean isValidEmail(String email) {
        // Simple email validation, consider using a library like Apache Commons Validator
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
    
    private String generateEmailVerificationToken() {
        return UUID.randomUUID().toString();
    }
    
    private void checkLoginAttempts(String email) {
        String emailLower = email.toLowerCase();
        LoginAttempt attempt = loginAttempts.get(emailLower);
        
        if (attempt != null && attempt.isLocked()) {
            log.warn("Account temporarily locked for: {}", email);
            throw new LockedException("error.auth.account.temporarily.locked");
        }
    }
    
    private void handleFailedLoginAttempt(String email) {
        String emailLower = email.toLowerCase();
        LoginAttempt attempt = loginAttempts.compute(emailLower, (k, v) -> 
            v == null ? new LoginAttempt() : v.incrementAttempts()
        );
        
        if (attempt.getAttempts() >= MAX_LOGIN_ATTEMPTS) {
            log.warn("Account locked due to too many failed login attempts: {}", email);
            attempt.lock();
        }
    }
    
    /**
     * Inner class to track login attempts
     */
    private static class LoginAttempt {
        private int attempts;
        private LocalDateTime lastAttemptTime;
        private boolean locked;
        
        public LoginAttempt() {
            this.attempts = 1;
            this.lastAttemptTime = LocalDateTime.now();
            this.locked = false;
        }
        
        public LoginAttempt incrementAttempts() {
            // Reset counter if last attempt was outside the window
            if (lastAttemptTime.plus(LOGIN_ATTEMPT_WINDOW).isBefore(LocalDateTime.now())) {
                this.attempts = 1;
            } else {
                this.attempts++;
            }
            this.lastAttemptTime = LocalDateTime.now();
            return this;
        }
        
        public void lock() {
            this.locked = true;
        }
        
        public boolean isLocked() {
            // Auto-unlock after window expires
            if (locked && lastAttemptTime.plus(LOGIN_ATTEMPT_WINDOW).isBefore(LocalDateTime.now())) {
                this.locked = false;
                this.attempts = 0;
                return false;
            }
            return locked;
        }
        
        public int getAttempts() {
            return attempts;
        }
    }
}
