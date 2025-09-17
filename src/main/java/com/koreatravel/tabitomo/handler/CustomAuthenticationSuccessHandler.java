package com.koreatravel.tabitomo.handler;
import com.koreatravel.tabitomo.domain.entity.MemberEntity;
import com.koreatravel.tabitomo.service.MemberService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final MemberService memberService;

    @Autowired
    public CustomAuthenticationSuccessHandler(MemberService memberService) {
        this.memberService = memberService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // 로그인한 사용자의 이메일(아이디)을 가져옴
        String userEmail = authentication.getName();

        // 서비스 계층을 통해 닉네임을 포함한 사용자 정보를 조회
        MemberEntity member = memberService.getMemberByEmail(userEmail);

        // 세션에 닉네임과 이메일을 저장
        HttpSession session = request.getSession();
        session.setAttribute("memberId", member.getEmail());
        session.setAttribute("memberNickname", member.getNickname());

        // 로그인 성공 후 리다이렉션할 기본 URL
        response.sendRedirect("/");
    }
}
