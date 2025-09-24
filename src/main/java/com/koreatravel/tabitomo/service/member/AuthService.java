package com.koreatravel.tabitomo.service.member;

import com.koreatravel.tabitomo.domain.dto.member.MemberProfileDTO;
import com.koreatravel.tabitomo.domain.entity.member.CountryEntity;
import com.koreatravel.tabitomo.domain.entity.member.LanguageEntity;
import com.koreatravel.tabitomo.domain.entity.member.MemberEntity;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import com.koreatravel.tabitomo.repository.member.CountryRepository;
import com.koreatravel.tabitomo.repository.member.LanguageRepository;
import com.koreatravel.tabitomo.repository.member.MemberRepository;
import com.koreatravel.tabitomo.domain.dto.auth.SignUpDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * AuthService handles user authentication and registration.
 */
@Service
@RequiredArgsConstructor
public class AuthService implements AuthServiceInterface {

    private final MemberRepository memberRepository;
    private final CountryRepository countryRepository;
    private final LanguageRepository languageRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    /**
     * Checks if an email already exists.
     *
     * @param email Email to check
     * @return true if email exists, false otherwise
     */
    public boolean existsByEmail(String email) {
        return memberRepository.findByEmail(email).isPresent();
    }

    /**
     * Checks if a nickname already exists.
     *
     * @param nickname Nickname to check
     * @return true if nickname exists, false otherwise
     */
    public boolean existsByNickname(String nickname) {
        return memberRepository.existsByNickname(nickname);
    }

    /**
     * Signs up a new user.
     *
     * @param dto        SignUpDTO containing user information
     * @param countryId  ID of the user's country
     * @param languageId ID of the user's language
     * @throws IllegalStateException if email or nickname already exists
     */
    @Transactional
    public void signup(SignUpDTO dto, int countryId, int languageId) {
        log.info("Starting signup process for email: {}", dto.getEmail());
        
        // Check if email already exists
        if (existsByEmail(dto.getEmail())) {
            log.warn("Signup failed - Email already exists: {}", dto.getEmail());
            throw new IllegalStateException("이미 사용 중인 이메일입니다.");
        }
        
        // Check if nickname already exists
        if (existsByNickname(dto.getNickname())) {
            log.warn("Signup failed - Nickname already exists: {}", dto.getNickname());
            throw new IllegalStateException("이미 사용 중인 닉네임입니다.");
        }
        
        // Get country and language first
        CountryEntity country = countryRepository.findById(countryId);
        if (country == null) {
            log.error("Invalid country ID: {}", countryId);
            throw new IllegalStateException("유효하지 않은 국가 ID입니다.");
        }
                
        LanguageEntity language = languageRepository.findById(languageId);
        if (language == null) {
            log.error("Invalid language ID: {}", languageId);
            throw new IllegalStateException("유효하지 않은 언어 ID입니다.");
        }
        
        log.debug("Creating new member with email: {}", dto.getEmail());
        
        try {
            // Create new member with all required fields
            MemberEntity memberEntity = MemberEntity.builder()
                    .email(dto.getEmail())
                    .password(passwordEncoder.encode(dto.getPassword()))
                    .nickname(dto.getNickname())
                    .dateOfBirth(dto.getDateOfBirth())
                    .gender(dto.getGender())
                    .active(true)
                    .role("ROLE_USER")
                    .country(country)
                    .preferredLanguage(language)
                    .build();
                    
            log.debug("Saving member to database");
            memberRepository.save(memberEntity);
            log.info("Successfully created new user with email: {}", dto.getEmail());
        } catch (Exception e) {
            log.error("Error creating user: {}", e.getMessage(), e);
            throw new IllegalStateException("회원 가입 중 오류가 발생했습니다. 다시 시도해주세요.");
        }
    }

    /**
     * Logs in a user and returns their profile information.
     *
     * @param email    User's email
     * @param password User's password
     * @return MemberProfileDTO containing user profile information
     */
    @Transactional
    public MemberProfileDTO login(String email, String password) {
        // Authenticate user
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(email, password)
        );
        
        // Get member entity
        MemberEntity member = memberRepository.findByEmail(email)
            .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        
        // Convert to DTO and return
        return convertToProfileDTO(member);
    }
    
    /**
     * Gets member profile by email.
     *
     * @param email the email of the member
     * @return MemberProfileDTO containing member profile information
     * @throws BadCredentialsException if member not found
     */
    @Transactional(readOnly = true)
    public MemberProfileDTO getMemberProfileByEmail(String email) {
        MemberEntity member = memberRepository.findByEmail(email)
            .orElseThrow(() -> new BadCredentialsException("Member not found with email: " + email));
        return convertToProfileDTO(member);
    }

    /**
     * Converts a MemberEntity to a MemberProfileDTO.
     */
    private MemberProfileDTO convertToProfileDTO(MemberEntity member) {
        MemberProfileDTO dto = new MemberProfileDTO();
        dto.setId(member.getId());
        dto.setEmail(member.getEmail());
        dto.setNickname(member.getNickname());
        dto.setProfileImageUrl(member.getProfileImageUrl());
        dto.setRole(member.getRole() != null ? member.getRole().toString() : null);
        
        // Add country and language if needed
        // dto.setCountry(...);
        // dto.setLanguage(...);
        
        return dto;
    }

    /**
     * Finds a member by their ID.
     *
     * @param id Member ID
     * @return MemberEntity containing member information
     */
    public MemberEntity findById(UUID id) {
        return memberRepository.findById(id).orElse(null);
    }
    
    @Override
    public boolean resetPassword(String email, String newPassword) {
        // 비밀번호 업데이트
        try {
            MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
                
            member.setPassword(passwordEncoder.encode(newPassword));
            memberRepository.save(member);
            return true;
        } catch (Exception e) {
            log.error("비밀번호 재설정 중 오류 발생", e);
            return false;
        }
    }
}