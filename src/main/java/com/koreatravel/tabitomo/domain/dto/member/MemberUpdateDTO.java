package com.koreatravel.tabitomo.domain.dto.member;

import jakarta.validation.constraints.NotBlank;
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
public class MemberUpdateDTO {
    
    @NotBlank(message = "닉네임을 입력해주세요.")
    @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하로 입력해주세요.")
    private String nickname;
    
    @Pattern(regexp = "^(?i)(MALE|FEMALE|OTHER|PREFER_NOT_TO_SAY)$", 
             message = "유효하지 않은 성별입니다. MALE, FEMALE, OTHER, PREFER_NOT_TO_SAY 중 하나를 선택해주세요.")
    private String gender;
    
    @Pattern(regexp = "^$|^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])$", 
             message = "생년월일은 YYYY-MM-DD 형식으로 입력해주세요.")
    private String dateOfBirth;
    
    private String profileImageUrl;
    
    @NotBlank(message = "현재 비밀번호를 입력해주세요.")
    private String currentPassword;
    
    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해주세요.")
    private String newPassword;
    
    private String newPasswordConfirm;
    
    private Integer countryId;
    
    private Integer preferredLanguageId;
    
    public boolean isPasswordChangeRequested() {
        return newPassword != null && !newPassword.trim().isEmpty();
    }
}
