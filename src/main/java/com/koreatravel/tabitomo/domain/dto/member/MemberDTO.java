package com.koreatravel.tabitomo.domain.dto.member;

import com.koreatravel.tabitomo.domain.entity.member.MemberEntity;
import jakarta.validation.constraints.*;
import jakarta.persistence.Transient;
import lombok.*;

/**
 * 회원 가입 및 수정 시 사용되는 DTO
 */

/**
 * 회원 가입 및 수정 시 사용되는 DTO
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberDTO {
    
    @NotBlank(message = "이메일은 필수 입력값입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;
    
    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
            message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다.")
    private String password;
    
    @NotBlank(message = "비밀번호 확인은 필수 입력값입니다.")
    private String confirmPassword;
    
    @NotBlank(message = "닉네임은 필수 입력값입니다.")
    @Size(min = 2, max = 10, message = "닉네임은 2자 이상 10자 이하로 입력해주세요.")
    private String nickname;
    
    private int countryId;
    
    @NotBlank(message = "국가는 필수 선택값입니다.")
    private String country;
    
    /**
     * 비밀번호 확인을 위한 필드
     */
    @Transient
    @AssertTrue(message = "비밀번호가 일치하지 않습니다.")
    public boolean isPasswordMatching() {
        if (this.password == null || this.confirmPassword == null) {
            return false;
        }
        return this.password.equals(this.confirmPassword);
    }
    
    // Explicit getter for confirmPassword to ensure it's recognized
    public String getConfirmPassword() {
        return this.confirmPassword;
    }
    
    private String role;

    /**
     * DTO를 Entity로 변환합니다.
     * @param dto 변환할 DTO
     * @return 변환된 MemberEntity
     */
    public static MemberEntity setEntity(MemberDTO dto) {
        return MemberEntity.builder()
                .email(dto.getEmail())
                .password(dto.getPassword())
                .nickname(dto.getNickname())
                .countryId(dto.getCountryId())
                .isActive(true)
                .build();
    }
}
