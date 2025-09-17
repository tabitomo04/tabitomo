package com.koreatravel.tabitomo.domain.dto.auth;

import lombok.Data;

@Data
public class ResetPasswordDTO {
    private String password;
    private String confirmPassword;
}
