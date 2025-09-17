package com.koreatravel.tabitomo.domain.dto;

import com.koreatravel.tabitomo.domain.entity.MemberEntity;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberDTO {
    private String email;
    private String password;
    private String nickname;
    private String gender;
    private int country_id;
    private int age;
    private String profileImagePath;
    private String statusMessage;
    private String hashtags;

    public static MemberEntity setEntity(MemberDTO dto){
        return MemberEntity.builder()
                .email(dto.getEmail())
                .password(dto.getPassword())
                .nickname(dto.getNickname())
                .gender(dto.getGender())
                .country_id(dto.getCountry_id())
                .age(dto.getAge())
                .profileImagePath(dto.getProfileImagePath())
                .statusMessage(dto.getStatusMessage())
                .hashtags(dto.getHashtags())
                .build();
    }
}