package com.koreatravel.tabitomo.domain.dto.member;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterDTO {
    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    @Email(message = "유효한 이메일 주소를 입력해주세요.")
    @Size(max = 80, message = "이메일은 최대 80자까지 입력 가능합니다.")
    private String email;

    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    @Size(min = 8, max = 60, message = "비밀번호는 8자 이상 60자 이하로 입력해주세요.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
            message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다.")
    private String password;

    @NotBlank(message = "비밀번호 확인은 필수 입력 항목입니다.")
    private String confirmPassword;

    @NotBlank(message = "닉네임은 필수 입력 항목입니다.")
    @Size(min = 2, max = 45, message = "닉네임은 2자 이상 45자 이하로 입력해주세요.")
    @Pattern(regexp = "^[가-힣a-zA-Z0-9]*$", message = "닉네임은 한글, 영문, 숫자만 사용 가능합니다.")
    private String nickname;

    @NotNull(message = "성별을 선택해주세요.")
    @Min(value = 1, message = "유효하지 않은 성별 값입니다.")
    @Max(value = 4, message = "유효하지 않은 성별 값입니다.")
    private Integer gender;  // 1: Male, 2: Female, 3: Other, 4: Prefer not to say

    @NotNull(message = "국적을 선택해주세요.")
    private Long countryId;
    
    @NotNull(message = "선호 언어를 선택해주세요.")
    private Long preferredLanguageId;

    public boolean isPasswordMatching() {
        return password != null && password.equals(confirmPassword);
    }
}
