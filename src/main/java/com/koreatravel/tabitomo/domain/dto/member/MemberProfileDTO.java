package com.koreatravel.tabitomo.domain.dto.member;

import lombok.Data;

@Data
public class MemberProfileDTO {
    private String id;
    private String email;
    private String nickname;
    private String profileImageUrl;
    private String introduction;

    
}