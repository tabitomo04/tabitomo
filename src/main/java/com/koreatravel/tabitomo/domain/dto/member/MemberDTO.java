package com.koreatravel.tabitomo.domain.dto.member;

import com.koreatravel.tabitomo.domain.entity.MemberEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MemberDTO {
    private String email;
    private String password;
    private String nickname;
    private int country_id;
    private String createdAt;
    private String updatedAt;
    private String role;

    public static MemberEntity setEntity(MemberDTO dto){
        return MemberEntity.builder()
                .email(dto.getEmail())
                .password(dto.getPassword())
                .nickname(dto.getNickname())

                .build();
    }
}
