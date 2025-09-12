package com.koreatravel.tabitomo.service.member;

import com.koreatravel.tabitomo.domain.dto.member.MemberProfileDTO;
import com.koreatravel.tabitomo.domain.dto.member.MemberUpdateDTO;
import com.koreatravel.tabitomo.domain.dto.member.RegisterDTO;
import com.koreatravel.tabitomo.domain.entity.member.MemberEntity;
import com.koreatravel.tabitomo.domain.entity.member.CountryEntity;
import com.koreatravel.tabitomo.exception.DuplicateResourceException;
import com.koreatravel.tabitomo.exception.ResourceNotFoundException;
import com.koreatravel.tabitomo.exception.ValidationException;
import com.koreatravel.tabitomo.repository.member.CountryRepository;
import com.koreatravel.tabitomo.repository.member.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.koreatravel.tabitomo.service.email.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
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
    private final PasswordEncoder passwordEncoder;
    private final CountryRepository countryRepository;
    private final EmailService emailService;
    
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;
    
    /**
     * 회원 프로필 조회
     */
    public MemberProfileDTO getMemberProfileById(UUID id) {
        MemberEntity member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return buildProfileDTO(member);
    }
    
    public MemberProfileDTO getMemberProfile(String email) {
        MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return buildProfileDTO(member);
    }
    
    private MemberProfileDTO buildProfileDTO(MemberEntity member) {
        // Get gender as string from member entity
        String gender = member.getGender();
        MemberProfileDTO.MemberProfileDTOBuilder builder = MemberProfileDTO.builder()
                .email(member.getEmail())
                .nickname(member.getNickname())
                .gender(gender)
                .dateOfBirth(member.getDateOfBirth() != null ? member.getDateOfBirth().toString() : null)
                .profileImageUrl(member.getProfileImageUrl())
                .createdAt(member.getCreatedAt())
                .updatedAt(member.getUpdatedAt())
                .status(member.isActive() ? "ACTIVE" : "INACTIVE")
                .role(member.getRole() != null ? member.getRole().name() : "ROLE_USER");
                
        // Add country information if available
        if (member.getCountry() != null) {
            builder.countryId(member.getCountry().getCountryId());
            builder.countryName(member.getCountry().getCountryName());
        }
        
        // Add preferred language information if available
        if (member.getPreferredLanguage() != null) {
            builder.preferredLanguageId(member.getPreferredLanguage().getLanguageId());
            builder.preferredLanguageName(member.getPreferredLanguage().getNameEn()); // Using nameEn field
        }
        
        return builder.build();
    }
    
    /**
     * 회원 정보 수정
     */
    @Transactional
    public MemberProfileDTO updateMemberProfileById(UUID id, MemberUpdateDTO updateDTO) {
        MemberEntity member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return updateMemberProfile(member, updateDTO);
    }
    
    public MemberProfileDTO updateMemberProfile(String email, MemberUpdateDTO updateDTO) {
        MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return updateMemberProfile(member, updateDTO);
    }
    
    private MemberProfileDTO updateMemberProfile(MemberEntity member, MemberUpdateDTO updateDTO) {
        
        // 닉네임 변경 시 중복 확인
        if (updateDTO.getNickname() != null && !updateDTO.getNickname().equals(member.getNickname())) {
            if (memberRepository.existsByNickname(updateDTO.getNickname())) {
                throw new DuplicateResourceException("Nickname already in use");
            }
            member.setNickname(updateDTO.getNickname());
        }
        
        // 생년월일 업데이트
        if (updateDTO.getDateOfBirth() != null && !updateDTO.getDateOfBirth().isEmpty()) {
            try {
                member.setDateOfBirth(java.time.LocalDate.parse(updateDTO.getDateOfBirth()));
            } catch (Exception e) {
                log.error("Invalid date format for dateOfBirth: " + updateDTO.getDateOfBirth(), e);
                throw new ValidationException("유효하지 않은 날짜 형식입니다. YYYY-MM-DD 형식으로 입력해주세요.");
            }
        }
        
        // 비밀번호 변경
        if (updateDTO.getNewPassword() != null && !updateDTO.getNewPassword().isEmpty()) {
            if (!passwordEncoder.matches(updateDTO.getCurrentPassword(), member.getPassword())) {
                throw new ValidationException("Current password is incorrect");
            }
            member.setPassword(passwordEncoder.encode(updateDTO.getNewPassword()));
        }
        
        // 프로필 이미지 업데이트
        if (updateDTO.getProfileImageUrl() != null) {
            member.setProfileImageUrl(updateDTO.getProfileImageUrl());
        }
        
        memberRepository.save(member);
        return buildProfileDTO(member);
    }
    
    /**
     * 회원 탈퇴 (비활성화)
     */
    @Transactional
    public void deactivateMember(String email) {
        MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        
        // 회원을 비활성화 (soft delete)
        member.setActive(false);
        memberRepository.save(member);
    }
    
    /**
     * Generates a verification code and sends it via email
     * @param email The email to send the verification code to
     * @return The generated verification code (for testing purposes)
     */
    public String sendVerificationCode(String email) {
        // Generate a 6-digit verification code
        String verificationCode = String.format("%06d", (int)(Math.random() * 1000000));
        
        try {
            // Send verification email
            emailService.sendVerificationEmail(email, verificationCode);
            return verificationCode;
        } catch (Exception e) {
            log.error("Failed to send verification email: {}", e.getMessage());
            throw new RuntimeException("이메일 전송에 실패했습니다. 나중에 다시 시도해주세요.", e);
        }
    }
    
    /**
     * Verifies a verification code (in a real app, you might want to use Redis for this)
     * @param inputCode The code entered by the user
     * @param sentCode The code that was sent to the user
     * @return true if the codes match, false otherwise
     */
    public boolean verifyCode(String inputCode, String sentCode) {
        return inputCode != null && inputCode.equals(sentCode);
    }

    /**
     * 이메일 사용 가능 여부 확인
     */
    public boolean existsByEmail(String email) {
        return memberRepository.existsByEmail(email);
    }
    
    /**
     * 설문조사 완료 처리
     */
    @Transactional
    public void markQuestionnaireCompleted(String email) {
        MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        
        member.completeQuestionnaire();
        memberRepository.save(member);
    }
    
    /**
     * 회원가입 처리
     */
    @Transactional
    public MemberEntity register(RegisterDTO registerDTO) {
        // Check if email is already registered
        if (memberRepository.existsByEmail(registerDTO.getEmail())) {
            throw new DuplicateResourceException("이미 사용 중인 이메일입니다.");
        }
        
        // Check if nickname is available
        if (memberRepository.existsByNickname(registerDTO.getNickname())) {
            throw new DuplicateResourceException("이미 사용 중인 닉네임입니다.");
        }
        
        // Verify password match
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            throw new ValidationException("비밀번호가 일치하지 않습니다.");
        }
        
        // Create new member using builder pattern
        MemberEntity newMember = MemberEntity.builder()
            .email(registerDTO.getEmail())
            .password(passwordEncoder.encode(registerDTO.getPassword()))
            .nickname(registerDTO.getNickname())
            .gender(registerDTO.getGender())
            .dateOfBirth(registerDTO.getDateOfBirth() != null && !registerDTO.getDateOfBirth().isEmpty() ? 
                java.time.LocalDate.parse(registerDTO.getDateOfBirth()) : null)
            .isActive(true) // Active immediately since we'll verify via session/redis if needed
            .questionnaireCompleted(false)
            .build();
        
        // Set country if provided
        if (registerDTO.getCountryId() != null) {
            CountryEntity country = countryRepository.findById(registerDTO.getCountryId())
                .orElseThrow(() -> new ResourceNotFoundException("국가를 찾을 수 없습니다."));
            newMember.setCountry(country);
        }
        
        // Save the new member
        return memberRepository.save(newMember);
    }
    
    /**
     * 이메일로 회원 조회
     */
    /**
     * 이메일로 회원 조회
     */
    public MemberEntity findByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }
    
    /**
     * 이메일로 회원 ID 조회
     * @param email 조회할 회원 이메일
     * @return 회원 ID (UUID)
     * @throws ResourceNotFoundException 해당 이메일의 회원이 없을 경우
     */
    public UUID getMemberIdByEmail(String email) {
        return memberRepository.findByEmail(email)
                .map(MemberEntity::getId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }
    
    /**
     * 이메일 사용 가능 여부 확인
     */
    public boolean isEmailAvailable(String email) {
        return !memberRepository.existsByEmail(email);
    }
    
    /**
     * 닉네임 사용 가능 여부 확인
     */
    public boolean isNicknameAvailable(String nickname) {
        return !memberRepository.existsByNickname(nickname);
    }
    
    /**
     * 비밀번호 재설정
     */
    @Transactional
    public void resetPassword(String email, String newPassword) {
        Optional<MemberEntity> memberOpt = memberRepository.findByEmail(email);
        if (memberOpt.isPresent()) {
            MemberEntity member = memberOpt.get();
            // 비밀번호 업데이트
            member.setPassword(passwordEncoder.encode(newPassword));
            memberRepository.save(member);
            
            log.info("Password reset for user: {}", email);
        } else {
            // 보안을 위해 존재하지 않는 이메일인 경우에도 예외를 던지지 않음
            log.warn("Password reset attempt for non-existent email: {}", email);
        }
    }
    
    /**
     * 회원 탈퇴
     */
    @Transactional
    public void deactivateAccountById(UUID id, String password) {
        MemberEntity member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        deactivateAccount(member, password);
    }
    
    public void deactivateAccount(String email, String password) {
        MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        deactivateAccount(member, password);
    }
    
    private void deactivateAccount(MemberEntity member, String password) {
        
        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new ValidationException("Password is incorrect");
        }
        
        member.setActive(false);
        memberRepository.save(member);
        
        log.info("Account deactivated for user: {}", member.getEmail());
    }
    
    /**
     * 비밀번호 변경
     */
    @Transactional
    public void changePasswordById(UUID id, String currentPassword, String newPassword) {
        MemberEntity member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        changePassword(member, currentPassword, newPassword);
    }
    
    public void changePassword(String email, String currentPassword, String newPassword) {
        MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        changePassword(member, currentPassword, newPassword);
    }
    
    private void changePassword(MemberEntity member, String currentPassword, String newPassword) {
        
        if (!passwordEncoder.matches(currentPassword, member.getPassword())) {
            throw new ValidationException("Current password is incorrect");
        }
        
        member.setPassword(passwordEncoder.encode(newPassword));
        memberRepository.save(member);
    }
}
