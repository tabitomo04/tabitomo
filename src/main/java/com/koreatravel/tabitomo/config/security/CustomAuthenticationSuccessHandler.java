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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                      Authentication authentication) throws IOException, ServletException {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
            log.error("Invalid authentication object or principal");
            response.sendRedirect("/auth/login?error=invalid_auth");
            return;
        }

        HttpSession session = request.getSession();
        try {
            // Get UserDetails from authentication principal
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            if (userDetails == null || userDetails.getEmail() == null) {
                log.error("User details or username is null");
                response.sendRedirect("/auth/login?error=invalid_user_details");
                return;
            }

            String email = userDetails.getEmail();
            log.debug("Processing authentication success for user: {}", email);

            // Create MemberProfileDTO from UserDetails
            MemberProfileDTO memberProfile = MemberProfileDTO.builder()
                    .id(userDetails.getMemberId())
                    .email(userDetails.getEmail())
                    .nickname(userDetails.getNickname())
                    .profileImageUrl(userDetails.getProfileImageUrl())
                    .gender(userDetails.getGender())
                    .isActive(userDetails.isEnabled())
                    .build();

            boolean isQuestionnaireCompleted = false;
            
            // Get additional member info from database if needed
            MemberEntity member = entityManager.find(MemberEntity.class, userDetails.getId());
            if (member != null) {
                memberProfile.setDateOfBirth(member.getDateOfBirth());
                isQuestionnaireCompleted = member.isQuestionnaireCompleted();
                memberProfile.setQuestionnaireCompleted(isQuestionnaireCompleted);
                memberProfile.setCreatedAt(member.getCreatedAt());
                memberProfile.setUpdatedAt(member.getUpdatedAt());

                // 국가 정보 설정
                if (member.getCountry() != null) {
                    Hibernate.initialize(member.getCountry());
                    memberProfile.setCountryCode(member.getCountry().getCountryCode());
                    memberProfile.setCountryName(member.getCountry().getNameEn());
                }

                // 언어 설정
                if (member.getPreferredLanguage() != null) {
                    Hibernate.initialize(member.getPreferredLanguage());
                    memberProfile.setPreferredLanguageId(member.getPreferredLanguage().getLanguageId());
                    memberProfile.setPreferredLanguageName(member.getPreferredLanguage().getNameEn());
                }
            }

            // Set session attributes
            session.setAttribute("userId", memberProfile.getId());
            session.setAttribute("authenticatedEmail", email);
            
            // Always get the latest questionnaire status from the database
            boolean latestQuestionnaireStatus = member != null && member.isQuestionnaireCompleted();
            
            // Update session attributes
            session.setAttribute("questionnaireCompleted", latestQuestionnaireStatus);
            session.setAttribute("showQuestionnairePrompt", !latestQuestionnaireStatus);
            
            // Update the authentication object with the latest questionnaire status
            if (authentication.getPrincipal() instanceof UserDetailsImpl) {
                UserDetailsImpl updatedUserDetails = ((UserDetailsImpl) authentication.getPrincipal())
                    .withQuestionnaireCompleted(latestQuestionnaireStatus);
                
                // Create a new authentication token with the updated user details
                Authentication newAuth = new UsernamePasswordAuthenticationToken(
                    updatedUserDetails,
                    authentication.getCredentials(),
                    authentication.getAuthorities()
                );
                
                // Update the security context
                SecurityContextHolder.getContext().setAuthentication(newAuth);
                log.debug("Updated authentication with questionnaireCompleted={}", latestQuestionnaireStatus);
            }

            log.info("Login successful - User: {}, Questionnaire completed: {}", email, latestQuestionnaireStatus);
            log.info("Setting showQuestionnairePrompt={} for user {}", !latestQuestionnaireStatus, email);
            log.info("Session ID after login: {}", session.getId());

            // Check if we need to redirect to the questionnaire
            String redirectUrl = "/";
            if (!latestQuestionnaireStatus) {
                // Add a flag to indicate we just logged in and should show the questionnaire
                session.setAttribute("justLoggedIn", true);
                redirectUrl = "/question/start";
            }
            
            // Redirect to the appropriate page
            response.sendRedirect(redirectUrl);
            
        } catch (ClassCastException e) {
            log.error("Invalid user details type in authentication: {}", e.getMessage(), e);
            response.sendRedirect("/auth/login?error=invalid_user_type");
        } catch (Exception e) {
            log.error("Error during authentication success handling: {}", e.getMessage(), e);
            response.sendRedirect("/auth/login?error=auth_error");
        }
    }
}