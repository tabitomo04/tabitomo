package com.koreatravel.tabitomo.domain.dto.auth;

import lombok.Data;
import java.util.UUID;

@Data
public class LoginDTO {
    private UUID memberId;
    private String email;
    private String password;
}
