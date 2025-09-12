package com.koreatravel.tabitomo.domain.dto.member;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberRegisterDTO {

    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "유효한 이메일 주소를 입력해주세요.")
    private String email;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해주세요.")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()\\-\\[\\]{}:;',?/*~$^+=<>]).{8,20}$", 
            message = "비밀번호는 8-20자, 영문 대소문자, 숫자, 특수문자를 포함해야 합니다.")
    private String password;

    @NotBlank(message = "비밀번호 확인을 입력해주세요.")
    private String passwordConfirm;

    @NotBlank(message = "닉네임을 입력해주세요.")
    @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하로 입력해주세요.")
    @Pattern(regexp = "^[a-zA-Z0-9가-힣]+", message = "닉네임은 한글, 영문, 숫자만 사용 가능합니다.")
    private String nickname;

    @Pattern(regexp = "^(MALE|FEMALE|OTHER|PREFER_NOT_TO_SAY|)$", 
             message = "유효하지 않은 성별입니다.")
    private String gender;
    
    @NotBlank(message = "생년월일을 입력해주세요.")
    @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])$", 
             message = "생년월일은 YYYY-MM-DD 형식으로 입력해주세요.")
    private String dateOfBirth;

    @NotNull(message = "국가를 선택해주세요.")
    private Integer countryId;
    
    @NotNull(message = "선호 언어를 선택해주세요.")
    private Integer preferredLanguageId;

    // 비밀번호와 비밀번호 확인이 일치하는지 검증
    public boolean isPasswordMatching() {
        return password != null && password.equals(passwordConfirm);
    }
}
