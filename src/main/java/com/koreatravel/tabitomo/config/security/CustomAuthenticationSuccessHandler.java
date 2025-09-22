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
        try {
            // Get UserDetails from authentication principal
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            String email = userDetails.getUsername();
            
            // Reattach the entity to the current persistence context and initialize lazy-loaded relationships
            MemberEntity member = entityManager.find(MemberEntity.class, userDetails.getMember().getId());
            
            if (member == null) {
                log.error("Member not found for email: {}", email);
                response.sendRedirect("/auth/login?error=user_not_found");
                return;
            }
            
            // Force initialization of lazy-loaded relationships
            Hibernate.initialize(member.getCountry());
            Hibernate.initialize(member.getPreferredLanguage());
            
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
            
            // Redirect based on questionnaire status
            if (!member.isQuestionnaireCompleted()) {
                log.info("Redirecting user {} to questionnaire", email);
                response.sendRedirect("/question/start");
            } else {
                response.sendRedirect("/");
            }
            
        } catch (Exception e) {
            log.error("Error during authentication success handling: {}", e.getMessage(), e);
            response.sendRedirect("/auth/login?error=auth_error");
        }
    }
}
