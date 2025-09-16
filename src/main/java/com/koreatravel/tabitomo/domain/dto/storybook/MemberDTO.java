package com.koreatravel.tabitomo.domain.dto;

import lombok.Data; 

@Data
public class MemberDTO {
    private String email;
    private String password;
    private String nickname;
    private int countryid;
    private String createdAt;
    private String updatedAt;
    private String role;
}
