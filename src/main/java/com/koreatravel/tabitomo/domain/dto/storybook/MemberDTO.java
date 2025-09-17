package com.koreatravel.tabitomo.domain.dto.storybook;

import java.util.UUID;

import lombok.Data; 

@Data
public class MemberDTO {
    private UUID id;
    private String email;
    private String password;
    private String nickname;
    private int countryid;
    private String createdAt;
    private String updatedAt;
    private String role;
}
