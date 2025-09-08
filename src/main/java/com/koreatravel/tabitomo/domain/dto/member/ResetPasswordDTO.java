package com.koreatravel.tabitomo.domain.dto.member;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 비밀번호 재설정을 위한 DTO
 */
@Getter
@Setter
public class ResetPasswordDTO {
    
    @NotBlank(message = "토큰은 필수 입력 값입니다.")
    private String token;
    
    @NotBlank(message = "이메일은 필수 입력 값입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;
    
    @NotBlank(message = "새 비밀번호는 필수 입력 값입니다.")
    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
    private String newPassword;
    
    @NotBlank(message = "비밀번호 확인은 필수 입력 값입니다.")
    private String confirmPassword;
    
    /**
     * 새 비밀번호와 확인 비밀번호가 일치하는지 검증
     * @return 비밀번호가 일치하면 true, 아니면 false
     */
    public boolean isPasswordMatching() {
        return newPassword != null && newPassword.equals(confirmPassword);
    }
}
