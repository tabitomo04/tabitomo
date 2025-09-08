package com.koreatravel.tabitomo.service.member;

import com.koreatravel.tabitomo.domain.dto.member.*;
import com.koreatravel.tabitomo.domain.entity.member.MemberEntity;
import com.koreatravel.tabitomo.domain.entity.member.CountryEntity;
import com.koreatravel.tabitomo.exception.TokenExpiredException;
// MemberRole is an inner class of MemberEntity
import com.koreatravel.tabitomo.exception.DuplicateResourceException;
import com.koreatravel.tabitomo.exception.ResourceNotFoundException;
import com.koreatravel.tabitomo.exception.UnauthorizedException;
import com.koreatravel.tabitomo.exception.ValidationException;
import com.koreatravel.tabitomo.repository.member.MemberRepository;
import com.koreatravel.tabitomo.repository.member.CountryRepository;
import com.koreatravel.tabitomo.service.email.EmailService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 회원 관련 비즈니스 로직을 처리하는 서비스 클래스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final CountryRepository countryRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;
    
    @Value("${app.password-reset-token-expiry-hours:24}")
    private int passwordResetTokenExpiryHours;

    /**
     * 회원가입 처리 및 이메일 인증 메일 발송
     */
    @Transactional
    public MemberEntity registerMember(String email, String password, String nickname, Integer gender, String countryCode) {
        // 이메일 중복 확인
        if (memberRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("이미 사용 중인 이메일 주소입니다.");
        }
        
        // 닉네임 중복 확인
        if (memberRepository.existsByNickname(nickname)) {
            throw new DuplicateResourceException("이미 사용 중인 닉네임입니다.");
        }
        
        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(password);
        
        // 국가 엔티티 조회 (ISO 코드로 조회)
        CountryEntity country = countryRepository.findByIsoCode(countryCode)
            .orElseThrow(() -> new ResourceNotFoundException("유효하지 않은 국가 코드입니다: " + countryCode));
        
        // 회원 엔티티 생성 (이메일 인증 토큰은 @PrePersist에서 자동 생성됨)
        MemberEntity member = MemberEntity.builder()
                .email(email)
                .password(encodedPassword)
                .nickname(nickname)
                .gender(gender)
                .country(country)
                .isActive(false) // 이메일 인증 전까지 비활성화
                .emailVerified(false)
                .build();
        
        // 회원 정보 저장
        MemberEntity savedMember = memberRepository.save(member);
        
        // 이메일 인증 메일 발송 (비동기 처리)
        emailService.sendVerificationEmail(email, savedMember.getEmailVerifyToken());
        
        return savedMember;
    }
    
    
    /**
     * 회원가입 처리 (기존 메서드 유지)
     */
    public MemberEntity register(RegisterDTO registerDTO) {
        log.info("회원가입 시도: {}", registerDTO.getEmail());
        
        // 이메일 중복 체크
        if (memberRepository.existsByEmail(registerDTO.getEmail())) {
            log.warn("이미 가입된 이메일: {}", registerDTO.getEmail());
            throw new DuplicateResourceException("이미 가입된 이메일입니다.");
        }
        
        // 닉네임 중복 체크
        if (memberRepository.existsByNickname(registerDTO.getNickname())) {
            log.warn("이미 사용 중인 닉네임: {}", registerDTO.getNickname());
            throw new DuplicateResourceException("이미 사용 중인 닉네임입니다.");
        }
        
        // 비밀번호 확인
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            throw new ValidationException("비밀번호가 일치하지 않습니다.");
        }
        
        // 국가 조회
        CountryEntity country = countryRepository.findById(registerDTO.getCountryId())
            .orElseThrow(() -> new ResourceNotFoundException("해당 국가를 찾을 수 없습니다."));
        
        // 회원 엔티티 생성
        MemberEntity member = MemberEntity.builder()
            .email(registerDTO.getEmail())
            .password(passwordEncoder.encode(registerDTO.getPassword()))
            .nickname(registerDTO.getNickname())
            .gender(registerDTO.getGender())
            .country(country)
            .role(MemberEntity.MemberRole.ROLE_USER)
            .emailVerifyToken(generateToken())
            .build();
            
        // 회원 저장
        MemberEntity savedMember = memberRepository.save(member);
        
        // 이메일 인증 메일 발송
        sendVerificationEmail(savedMember);
        
        return savedMember;
    }

    /**
     * 로그인 처리
     */
    public MemberEntity login(LoginDTO loginDTO) {
        // 인증 시도
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginDTO.getEmail(),
                loginDTO.getPassword()
            )
        );
        
        // 인증 성공 시 사용자 정보 반환
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return (MemberEntity) authentication.getPrincipal();
    }
    
    /**
     * 현재 인증된 사용자 정보 조회
     */
    public MemberEntity getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("인증된 사용자가 없습니다.");
        }
        
        String email = authentication.getName();
        return memberRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));
    }
    
    /**
     * 이메일로 사용자 조회
     */
    public MemberEntity findByEmail(String email) {
        return memberRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));
    }
    
    /**
     * 이메일 중복 확인
     */
    public boolean isEmailAvailable(String email) {
        return !memberRepository.existsByEmail(email);
    }
    
    /**
     * 닉네임 중복 확인
     */
    public boolean isNicknameAvailable(String nickname) {
        return !memberRepository.existsByNickname(nickname);
    }
    
    /**
     * 이메일 인증 처리
     */
    @Transactional
    public boolean verifyEmail(String token) {
        // 토큰으로 회원 조회
        MemberEntity member = memberRepository.findByEmailVerifyToken(token)
            .orElseThrow(() -> new ResourceNotFoundException("유효하지 않은 인증 토큰입니다."));
        
        // 이미 인증된 계정인지 확인
        if (member.isEmailVerified()) {
            return true;
        }
        
        // 토큰 유효 기간 확인 (24시간 이내)
        if (member.getCreatedAt().isBefore(LocalDateTime.now().minusHours(24))) {
            // 토큰 만료 시 새 토큰 발급
            member.setEmailVerifyToken(UUID.randomUUID().toString());
            memberRepository.save(member);
            // 새 인증 이메일 발송
            emailService.sendVerificationEmail(member.getEmail(), member.getEmailVerifyToken());
            throw new TokenExpiredException("인증 토큰이 만료되었습니다. 새 인증 메일을 발송했습니다.");
        }
        
        // 계정 활성화
        member.setEmailVerified(true);
        member.setActive(true);
        member.setEmailVerifyToken(null);
        memberRepository.save(member);
        
        return true;
    }
    
    /**
     * 비밀번호 재설정 요청
     */
    public void requestPasswordReset(String email) {
        MemberEntity member = memberRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("가입되지 않은 이메일입니다."));
            
        String token = generateToken();
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(passwordResetTokenExpiryHours);
        
        memberRepository.setPasswordResetToken(email, token, expiryDate);
        
        // 비밀번호 재설정 이메일 발송
        sendPasswordResetEmail(member, token);
    }
    
    /**
     * 비밀번호 재설정
     */
    public void resetPassword(ResetPasswordDTO resetPasswordDTO) {
        // 비밀번호 확인 검증
        if (!resetPasswordDTO.isPasswordMatching()) {
            throw new ValidationException("새 비밀번호가 일치하지 않습니다.");
        }
        
        // 토큰 유효성 검사
        MemberEntity member = memberRepository.findByPasswordResetToken(resetPasswordDTO.getToken(), LocalDateTime.now())
            .orElseThrow(() -> new UnauthorizedException("유효하지 않거나 만료된 토큰입니다."));
            
        // 이메일 일치 확인
        if (!member.getEmail().equals(resetPasswordDTO.getEmail())) {
            throw new UnauthorizedException("이메일이 일치하지 않습니다.");
        }
        
        // 새 비밀번호로 업데이트
        int updated = memberRepository.updatePassword(member.getEmail(), passwordEncoder.encode(resetPasswordDTO.getNewPassword()));
        if (updated == 0) {
            log.error("Failed to update password for user: {}", member.getEmail());
            throw new RuntimeException("비밀번호 업데이트에 실패했습니다.");
        }
    }
    
    /**
     * 프로필 수정
     */
    public MemberEntity updateProfile(String email, String nickname, String profileImageUrl) {
        MemberEntity member = memberRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));
            
        // 닉네임이 변경된 경우 중복 체크
        if (StringUtils.hasText(nickname) && !member.getNickname().equals(nickname)) {
            if (memberRepository.existsByNickname(nickname)) {
                throw new DuplicateResourceException("이미 사용 중인 닉네임입니다.");
            }
            member.setNickname(nickname);
        }
        
        // 프로필 이미지 업데이트
        if (profileImageUrl != null) {
            member.setProfileImageUrl(profileImageUrl);
        }
        
        return member;
    }
    
    /**
     * 회원 탈퇴
     */
    public void deactivateAccount(String email) {
        // Check if the user exists before deactivating
        if (!memberRepository.existsByEmail(email)) {
            throw new ResourceNotFoundException("사용자를 찾을 수 없습니다.");
        }
            
        int updated = memberRepository.deactivateByEmail(email);
        if (updated == 0) {
            log.error("Failed to deactivate account: {}", email);
            throw new RuntimeException("회원 탈퇴 처리에 실패했습니다.");
        }
        log.info("회원 탈퇴 처리 완료: {}", email);
    }
    
    // ===== PRIVATE HELPER METHODS =====
    
    private String generateToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }
    
    private void sendVerificationEmail(MemberEntity member) {
        String verificationUrl = String.format(
            "%s/api/auth/verify-email?token=%s", 
            baseUrl,
            member.getEmailVerifyToken()
        );
        
        // TODO: 이메일 발송 로직 구현
        String subject = "[Tabitomo] 이메일 인증을 완료해주세요";
        String content = String.format(
            "안녕하세요 %s님,\n\n" +
            "아래 링크를 클릭하여 이메일 인증을 완료해주세요.\n" +
            "%s\n\n" +
            "감사합니다.\n" +
            "Tabitomo 팀 드림",
            member.getNickname(),
            verificationUrl
        );
        
        emailService.sendEmail(member.getEmail(), subject, content);
    }
    
    private void sendPasswordResetEmail(MemberEntity member, String token) {
        String resetUrl = String.format(
            "%s/reset-password?token=%s", 
            baseUrl,
            token
        );
        
        String subject = "[Tabitomo] 비밀번호 재설정 안내";
        String content = String.format(
            "안녕하세요 %s님,\n\n" +
            "비밀번호 재설정을 위해 아래 링크를 클릭해주세요.\n" +
            "%s\n\n" +
            "이 링크는 24시간 동안 유효합니다.\n\n" +
            "감사합니다.\n" +
            "Tabitomo 팀 드림",
            member.getNickname(),
            resetUrl
        );
        
        emailService.sendEmail(member.getEmail(), subject, content);
    }
}

