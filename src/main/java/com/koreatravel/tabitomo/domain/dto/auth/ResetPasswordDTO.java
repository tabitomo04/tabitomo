package com.koreatravel.tabitomo.domain.dto.auth;

import lombok.Data;
import java.util.UUID;

@Data
public class ResetPasswordDTO {
    private String password;
    private String confirmPassword;
}
