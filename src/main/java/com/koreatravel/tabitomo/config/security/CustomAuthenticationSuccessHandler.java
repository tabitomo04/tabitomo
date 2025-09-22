package com.koreatravel.tabitomo.config.security;

import com.koreatravel.tabitomo.domain.dto.member.MemberProfileDTO;
import com.koreatravel.tabitomo.domain.entity.member.MemberEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                      Authentication authentication) throws IOException, ServletException {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
            log.error("Invalid authentication object or principal");
            response.sendRedirect("/auth/login?error=invalid_auth");
            return;
        }

        try {
            // Get UserDetails from authentication principal
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            if (userDetails == null || userDetails.getUsername() == null) {
                log.error("User details or username is null");
                response.sendRedirect("/auth/login?error=invalid_user_details");
                return;
            }
            
            String email = userDetails.getUsername();
            log.debug("Processing authentication success for user: {}", email);
            
            // Check if member exists
            if (userDetails.getMember() == null || userDetails.getMember().getId() == null) {
                log.error("Member information is missing in user details for email: {}", email);
                response.sendRedirect("/auth/login?error=missing_member_info");
                return;
            }
            
            // Reattach the entity to the current persistence context
            MemberEntity member = entityManager.find(MemberEntity.class, userDetails.getMember().getId());
            
            if (member == null) {
                log.error("Member not found in database for email: {}", email);
                response.sendRedirect("/auth/login?error=user_not_found");
                return;
            }
            
            try {
                // Force initialization of lazy-loaded relationships
                if (member.getCountry() != null) {
                    Hibernate.initialize(member.getCountry());
                }
                if (member.getPreferredLanguage() != null) {
                    Hibernate.initialize(member.getPreferredLanguage());
                }
                
                // MemberEntity를 MemberProfileDTO로 변환
                MemberProfileDTO memberProfile = MemberProfileDTO.builder()
                    .id(member.getId())
                    .email(member.getEmail())
                    .nickname(member.getNickname())
                    .profileImageUrl(member.getProfileImageUrl())
                    .dateOfBirth(member.getDateOfBirth())
                    .gender(member.getGender())
                    .isActive(member.isActive())
                    .questionnaireCompleted(member.isQuestionnaireCompleted())
                    .createdAt(member.getCreatedAt())
                    .updatedAt(member.getUpdatedAt())
                    .build();
                    
                // 국가 정보 설정
                if (member.getCountry() != null) {
                    memberProfile.setCountryCode(member.getCountry().getCountryCode());
                    memberProfile.setCountryName(member.getCountry().getNameEn());
                }
                
                // 언어 정보 설정
                if (member.getPreferredLanguage() != null) {
                    memberProfile.setPreferredLanguageId(member.getPreferredLanguage().getLanguageId());
                    memberProfile.setPreferredLanguageName(member.getPreferredLanguage().getNameEn());
                }
                
                // 세션에 사용자 정보 저장
                HttpSession session = request.getSession();
                session.setAttribute("user", memberProfile);
                session.setAttribute("authenticatedEmail", email);
                session.setAttribute("questionnaireCompleted", memberProfile.isQuestionnaireCompleted());
                
                log.info("Login successful - User: {}, Questionnaire completed: {}", 
                        email, memberProfile.isQuestionnaireCompleted());
                
                // Set a flag in session to show questionnaire prompt
                if (!member.isQuestionnaireCompleted()) {
                    log.info("Setting showQuestionnairePrompt flag for user {}", email);
                    session.setAttribute("showQuestionnairePrompt", true);
                }
                
                // Always redirect to home page
                response.sendRedirect("/");
                
            } catch (Exception e) {
                log.error("Error processing member profile for {}: {}", email, e.getMessage(), e);
                response.sendRedirect("/auth/login?error=profile_processing_error");
            }
            
        } catch (ClassCastException e) {
            log.error("Invalid user details type in authentication: {}", e.getMessage(), e);
            response.sendRedirect("/auth/login?error=invalid_user_type");
        } catch (Exception e) {
            log.error("Unexpected error during authentication success handling: {}", e.getMessage(), e);
            response.sendRedirect("/auth/login?error=auth_error");
        }
    }
}
