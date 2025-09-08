package com.koreatravel.tabitomo.domain.dto.member;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 비밀번호 재설정 요청을 위한 DTO
 */
@Getter
@Setter
public class PasswordResetRequestDTO {
    
    @NotBlank(message = "이메일은 필수 입력 값입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;
    
    @NotBlank(message = "비밀번호 재설정 토큰은 필수 입력 값입니다.")
    private String token;
}
