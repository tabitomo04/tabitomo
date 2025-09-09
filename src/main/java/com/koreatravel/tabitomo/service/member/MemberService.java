package com.koreatravel.tabitomo.service.member;

import com.koreatravel.tabitomo.domain.dto.member.*;
import com.koreatravel.tabitomo.domain.entity.member.MemberEntity;
import com.koreatravel.tabitomo.domain.entity.member.CountryEntity;
import com.koreatravel.tabitomo.domain.entity.member.AddInfoEntity;
import com.koreatravel.tabitomo.repository.member.AddInfoRepository;
import com.koreatravel.tabitomo.dto.member.QuestionAnswersDTO;
import com.koreatravel.tabitomo.service.token.TokenService;
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
import com.koreatravel.tabitomo.config.security.MemberDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 회원 관련 비즈니스 로직을 처리하는 서비스 클래스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final CountryRepository countryRepository;
    
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;
    
    @Value("${app.password-reset-token-expiry-hours:24}")
    private int passwordResetTokenExpiryHours;
    
    @Value("${app.verification-code-expiry-minutes:5}")
    private int verificationCodeExpiryMinutes;
    
    private final AddInfoRepository addInfoRepository;

    /**
     * 회원가입 처리 및 이메일 인증 메일 발송
     */
    /**
     * 이메일 사용 가능 여부 확인
     */
    public boolean existsByEmail(String email) {
        return memberRepository.existsByEmail(email);
    }
    
    /**
     * Save user's questionnaire answers
     * @param email User's email
     * @param answersDTO DTO containing user's answers
     */
    @Transactional
    public void saveQuestionAnswers(String email, QuestionAnswersDTO answersDTO) {
        MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        
        // Clear existing answers
        member.getAdditionalInfos().clear();
        
        // Add hobbies (info_high_num = 1)
        if (answersDTO.getHobbies() != null) {
            answersDTO.getHobbies().forEach(hobbyId -> {
                AddInfoEntity hobby = addInfoRepository.findByInfoHighNumAndInfoLowNum(1, hobbyId)
                        .orElseThrow(() -> new ResourceNotFoundException("Hobby not found with id: " + hobbyId));
                member.addAdditionalInfo(hobby);
            });
        }
        
        // Add MBTI (info_high_num = 2, single selection)
        if (answersDTO.getMbti() != null && !answersDTO.getMbti().isEmpty()) {
            AddInfoEntity mbti = addInfoRepository.findByInfoHighNumAndContent(2, answersDTO.getMbti())
                    .orElseThrow(() -> new ResourceNotFoundException("Invalid MBTI type: " + answersDTO.getMbti()));
            member.addAdditionalInfo(mbti);
        }
        
        // Add travel styles (info_high_num = 3)
        if (answersDTO.getTravelStyles() != null) {
            answersDTO.getTravelStyles().forEach(styleId -> {
                AddInfoEntity style = addInfoRepository.findByInfoHighNumAndInfoLowNum(3, styleId)
                        .orElseThrow(() -> new ResourceNotFoundException("Travel style not found with id: " + styleId));
                member.addAdditionalInfo(style);
            });
        }
        
        // Add companions (info_high_num = 4)
        if (answersDTO.getCompanions() != null) {
            answersDTO.getCompanions().forEach(companionId -> {
                AddInfoEntity companion = addInfoRepository.findByInfoHighNumAndInfoLowNum(4, companionId)
                        .orElseThrow(() -> new ResourceNotFoundException("Companion type not found with id: " + companionId));
                member.addAdditionalInfo(companion);
            });
        }
        
        // Add food preferences (info_high_num = 5)
        if (answersDTO.getFoodPreferences() != null) {
            answersDTO.getFoodPreferences().forEach(foodId -> {
                AddInfoEntity food = addInfoRepository.findByInfoHighNumAndInfoLowNum(5, foodId)
                        .orElseThrow(() -> new ResourceNotFoundException("Food preference not found with id: " + foodId));
                member.addAdditionalInfo(food);
            });
        }
        
        memberRepository.save(member);
    }
    
    /**
     * Mark questionnaire as completed for a user
     * @param email User's email
     */
    @Transactional
    public void markQuestionnaireCompleted(String email) {
        MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        
        member.completeQuestionnaire();
        memberRepository.save(member);
    }
    
    /**
     * Find member by email
     * @param email User's email
     * @return Member entity
     * @throws ResourceNotFoundException if user not found
     */
    public MemberEntity findByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }
    
    /**
     * 이메일이 사용 가능한지 확인
     * @param email 확인할 이메일 주소
     * @return 사용 가능하면 true, 이미 사용 중이면 false
     */
    public boolean isEmailAvailable(String email) {
        return !memberRepository.existsByEmail(email);
    }
    
    /**
     * 닉네임이 사용 가능한지 확인
     * @param nickname 확인할 닉네임
     * @return 사용 가능하면 true, 이미 사용 중이면 false
     */
    public boolean isNicknameAvailable(String nickname) {
        return !memberRepository.existsByNickname(nickname);
    }
    
    /**
     * 이메일 인증 처리
     * @param email 인증할 이메일 주소
     * @param code 인증 코드
     * @return 인증 성공 여부
     */
    public boolean verifyEmail(String email, String code) {
        // 인증 코드 검증
        boolean isValid = tokenService.validateToken(email, code);
        if (isValid) {
            // 이메일 인증 처리 (isActive를 true로 설정)
            MemberEntity member = memberRepository.findById(email)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다: " + email));
            member.setActive(true);
            memberRepository.save(member);
            
            // 인증 코드 삭제
            tokenService.invalidateToken(email);
            return true;
        }
        return false;
    }
    
    /**
     * 비밀번호 재설정 요청
     * @param email 비밀번호를 재설정할 이메일 주소
     */
    public void requestPasswordReset(String email) {
        // 이메일로 회원 조회
        MemberEntity member = memberRepository.findById(email)
            .orElseThrow(() -> new ResourceNotFoundException("가입되지 않은 이메일입니다."));
            
        // 토큰 생성 및 저장 (24시간 유효)
        String token = tokenService.generateAndSaveToken(email);
        
        // 비밀번호 재설정 이메일 발송
        sendPasswordResetEmail(member, token);
    }
    
    /**
     * 비밀번호 재설정 (이메일과 새 비밀번호로, 토큰 없이)
     * @param email 사용자 이메일
     * @param newPassword 새 비밀번호
     */
    public void resetPassword(String email, String newPassword) {
        // 이메일로 회원 조회
        MemberEntity member = memberRepository.findById(email)
            .orElseThrow(() -> new ResourceNotFoundException("가입되지 않은 이메일입니다."));
            
        // 비밀번호 업데이트
        member.setPassword(passwordEncoder.encode(newPassword));
        
        // 토큰 무효화 (1회용)
        tokenService.invalidateToken(email);
        
        memberRepository.save(member);
    }
    
    /**
     * 비밀번호 재설정 (토큰 기반)
     * @param resetPasswordDTO 비밀번호 재설정 DTO
     */
    public void resetPassword(ResetPasswordDTO resetPasswordDTO) {
        // 비밀번호 확인 검증
        if (!resetPasswordDTO.isPasswordMatching()) {
            throw new ValidationException("새 비밀번호가 일치하지 않습니다.");
        }
        
        // 토큰 검증 (이메일은 DTO에서 가져옴)
        String email = resetPasswordDTO.getEmail();
        if (!tokenService.validateToken(email, resetPasswordDTO.getToken())) {
            throw new UnauthorizedException("유효하지 않거나 만료된 토큰입니다.");
        }
        
        // 비밀번호 업데이트
        resetPassword(email, resetPasswordDTO.getNewPassword());
    }

    /**
     * 회원가입 처리
     * @param registerDTO 회원가입 정보 DTO
     * @return 저장된 회원 엔티티
     * @throws DuplicateResourceException 이메일 또는 닉네임이 이미 사용 중인 경우
     * @throws ResourceNotFoundException 국가를 찾을 수 없는 경우
     * @throws ValidationException 비밀번호 확인이 일치하지 않는 경우
     */
    @Transactional
    public MemberEntity register(RegisterDTO registerDTO) {
        log.info("회원가입 시도: {}", registerDTO.getEmail());
        
        // 입력 유효성 검사
        validateRegistration(registerDTO);
        
        // 국가 조회 (ISO 코드 또는 ID로 조회)
        CountryEntity country = findCountry(registerDTO);
        
        // 회원 엔티티 생성
        MemberEntity member = createMemberEntity(registerDTO, country);
        
        // 회원 정보 저장
        MemberEntity savedMember = memberRepository.save(member);
        
        // 이메일 인증 메일 발송
        sendVerificationEmail(savedMember.getEmail());
        
        return savedMember;
    }
    
    /**
     * 회원가입 정보 유효성 검사
     */
    private void validateRegistration(RegisterDTO registerDTO) {
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
    }
    
    /**
     * 국가 조회 (ID로 조회)
     */
    private CountryEntity findCountry(RegisterDTO registerDTO) {
        if (registerDTO.getCountryId() == null) {
            throw new ValidationException("국가 정보가 필요합니다.");
        }
        return countryRepository.findById(registerDTO.getCountryId())
            .orElseThrow(() -> new ResourceNotFoundException("해당 국가를 찾을 수 없습니다."));
    }
    
    /**
     * 회원 엔티티 생성
     */
    private MemberEntity createMemberEntity(RegisterDTO registerDTO, CountryEntity country) {
        return MemberEntity.builder()
            .email(registerDTO.getEmail())
            .password(passwordEncoder.encode(registerDTO.getPassword()))
            .nickname(registerDTO.getNickname())
            .gender(registerDTO.getGender())
            .country(country)
            .isActive(false) // 이메일 인증 전까지 비활성화
            .role(MemberEntity.MemberRole.ROLE_USER)
            .build();
    }
    
    /**
     * 이메일 인증 메일 발송
     * @param email 인증 이메일 주소
     */
    /**
     * 6자리 인증 코드 생성
     */
    private String generateVerificationCode() {
        return String.format("%06d", (int) (Math.random() * 1000000));
    }
    
    /**
     * 이메일 인증 메일 발송
     */
    private void sendVerificationEmail(String email) {
        String verificationCode = generateVerificationCode();
        // 토큰 서비스를 사용하여 인증 코드 저장 (24시간 유효)
        tokenService.generateAndSaveToken(email, verificationCode, 24 * 60); // 24시간을 분 단위로 변환
        emailService.sendVerificationEmail(email, verificationCode);
    }
    
    /**
     * 비밀번호 재설정 이메일 발송
     */
    private void sendPasswordResetEmail(MemberEntity member, String token) {
        String resetLink = String.format("%s/reset-password?token=%s", baseUrl, token);
        emailService.sendPasswordResetEmail(member.getEmail(), resetLink);
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
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new UnauthorizedException("No authenticated user found");
        }
        
        if (authentication.getPrincipal() instanceof MemberDetails) {
            return ((MemberDetails) authentication.getPrincipal()).getMember();
        } else if (authentication.getPrincipal() instanceof String) {
            throw new UnauthorizedException("User not authenticated");
        } else {
            throw new UnauthorizedException("Unexpected principal type: " + authentication.getPrincipal().getClass().getName());
        }
    }
    
    /**
     * 비밀번호 재설정을 위한 인증코드 생성 및 저장
     * @param email 이메일 주소
     * @return 생성된 인증 코드
     */
    /**
     * 인증 코드 생성 및 저장
     * @param email 이메일 주소
     * @return 생성된 인증 코드
     */
    @Transactional
    public String generateAndSaveVerificationCode(String email) {
        // 이메일 존재 여부 확인
        memberRepository.findById(email)
            .orElseThrow(() -> new ResourceNotFoundException("가입되지 않은 이메일입니다."));
            
        // 6자리 랜덤 숫자 생성
        String verificationCode = String.format("%06d", (int) (Math.random() * 1000000));
        
        // 토큰 서비스를 사용하여 인증 코드 저장
        tokenService.generateAndSaveToken(email, verificationCode, verificationCodeExpiryMinutes);
        
        log.info("Verification code for {}: {}", email, verificationCode);
        return verificationCode;
    }
    
    /**
     * 인증코드 검증
     * @param email 이메일 주소
     * @param code 인증 코드
     * @return 인증 성공 여부
     */
    /**
     * 인증 코드 검증
     * @param email 이메일 주소
     * @param code 검증할 인증 코드
     * @return 인증 성공 여부
     */
    @Transactional
    public boolean verifyCode(String email, String code) {
        return tokenService.validateToken(email, code);
    }
    
    /**
     * 비밀번호 재설정
     */
    /**
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
    
}

