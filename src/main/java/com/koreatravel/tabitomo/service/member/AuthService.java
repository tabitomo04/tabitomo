package com.koreatravel.tabitomo.service.member;

import com.koreatravel.tabitomo.config.security.UserDetailsImpl;
import com.koreatravel.tabitomo.domain.dto.member.MemberProfileDTO;
import com.koreatravel.tabitomo.repository.member.MemberRepository;
import com.koreatravel.tabitomo.domain.dto.auth.SignUpDTO;
import com.koreatravel.tabitomo.domain.entity.member.MemberEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * AuthService handles user authentication and registration.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

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
        return memberRepository.findByNickname(nickname).isPresent();
    }

    /**
     * Signs up a new user.
     *
     * @param dto        SignUpDTO containing user information
     * @param countryId  ID of the user's country
     * @param languageId ID of the user's language
     * @throws IllegalStateException if email or nickname already exists
     */
    public void signup(SignUpDTO dto, int countryId, int languageId) {
        // Check if email already exists
        if (existsByEmail(dto.getEmail())) {
            throw new IllegalStateException("이미 사용 중인 이메일입니다.");
        }
        
        // Check if nickname already exists
        if (existsByNickname(dto.getNickname())) {
            throw new IllegalStateException("이미 사용 중인 닉네임입니다.");
        }
        
        // Encrypt password and create new member
        MemberEntity memberEntity = new MemberEntity(
            dto.getEmail(),
            dto.getBirthDate(),
            passwordEncoder.encode(dto.getPassword()),
            dto.getNickname(),
            dto.getGender()
        );
        memberEntity.setCountry(countryId);
        memberEntity.setLanguage(languageId);
        memberRepository.save(memberEntity);
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
        try {
            // Attempt authentication
            Authentication authentication = new UsernamePasswordAuthenticationToken(email, password);
            Authentication authenticated = authenticationManager.authenticate(authentication);

            // Get MemberEntity from UserDetails
            UserDetailsImpl userDetails = (UserDetailsImpl) authenticated.getPrincipal();
            MemberEntity member = userDetails.getMember();

            // Convert MemberEntity to MemberProfileDTO
            return convertToProfileDTO(member);

        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.", e);
        } catch (Exception e) {
            throw new RuntimeException("로그인 처리 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * Converts a MemberEntity to a MemberProfileDTO.
     */
    private MemberProfileDTO convertToProfileDTO(MemberEntity member) {
        MemberProfileDTO dto = new MemberProfileDTO();
        dto.setId(member.getId() != null ? member.getId().getId() : null);
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
    public MemberEntity findById(Long id) {
        return memberRepository.findById(id).orElse(null);
    }
}