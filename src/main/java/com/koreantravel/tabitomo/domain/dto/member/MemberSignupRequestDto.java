package com.koreantravel.tabitomo.domain.dto.member;

import lombok.Getter;
import lombok.Setter;
import com.koreantravel.tabitomo.domain.entity.Member;

@Getter @Setter
public class MemberSignupRequestDto {
    private String email;
    private String password;
    private byte gender;
    private String nickname;
    private int countryId;

    public Member toEntity() {
        return Member.builder()
                .email(email)
                .password(password) // Will be encoded later
                .gender(gender)
                .nickname(nickname)
                .countryId(countryId)
                .build();
    }
}
