package com.koreatravel.tabitomo.domain.dto.auth.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.koreatravel.tabitomo.domain.dto.member.MemberProfileDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private MemberProfileDTO user;
    
    public static LoginResponse of(String accessToken, String refreshToken, MemberProfileDTO user) {
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(3600L) // 1 hour
                .user(user)
                .build();
    }
}
