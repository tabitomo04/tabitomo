package com.koreatravel.tabitomo.config;

import com.koreatravel.tabitomo.PathConstants;
import com.koreatravel.tabitomo.domain.entity.MemberEntity;
import com.koreatravel.tabitomo.repository.MemberRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final MemberRepository memberRepository;

    public CustomAuthenticationSuccessHandler(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                      HttpServletResponse response, 
                                      Authentication authentication) throws IOException, ServletException {
        
        String email = authentication.getName();
        MemberEntity member = memberRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if it's first login (createdAt equals updatedAt)
        if (member.getCreatedAt() != null && member.getCreatedAt().equals(member.getUpdatedAt())) {
            getRedirectStrategy().sendRedirect(request, response, PathConstants.MEMBER_QUESTION_START);
        } else {
            super.onAuthenticationSuccess(request, response, authentication);
        }
    }
}
