package com.koreatravel.tabitomo.service.token;

import com.koreatravel.tabitomo.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final MemberRepository memberRepository;
    private static final int TOKEN_EXPIRY_HOURS = 24;

    /**
     * 인증 토큰 생성 및 저장
     * @param email 사용자 이메일
     * @param token 사용자 정의 토큰 (null인 경우 UUID 생성)
     * @param expiryMinutes 토큰 만료 시간(분)
     * @return 생성된 토큰
     */
    @Transactional
    public String generateAndSaveToken(String email, String token, int expiryMinutes) {
        String finalToken = (token != null) ? token : UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(expiryMinutes);
        memberRepository.updateResetToken(email, finalToken, expiryDate);
        return finalToken;
    }
    
    /**
     * 기본 토큰 생성 및 저장 (24시간 유효)
     */
    @Transactional
    public String generateAndSaveToken(String email) {
        return generateAndSaveToken(email, null, TOKEN_EXPIRY_HOURS * 60);
    }

    @Transactional(readOnly = true)
    public boolean validateToken(String email, String token) {
        return memberRepository.findByEmailAndResetToken(email, token)
                .map(member -> member.getResetTokenExpiry() != null && 
                              member.getResetTokenExpiry().isAfter(LocalDateTime.now()))
                .orElse(false);
    }

    @Transactional
    public void invalidateToken(String email) {
        memberRepository.clearResetToken(email);
    }
}
