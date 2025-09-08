package com.koreatravel.tabitomo.domain.dto.member;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckNicknameDTO {
    @NotBlank(message = "닉네임을 입력해주세요.")
    @Size(min = 2, max = 45, message = "닉네임은 2자 이상 45자 이하로 입력해주세요.")
    @Pattern(regexp = "^[가-힣a-zA-Z0-9]*$", message = "닉네임은 한글, 영문, 숫자만 사용 가능합니다.")
    private String nickname;
}
