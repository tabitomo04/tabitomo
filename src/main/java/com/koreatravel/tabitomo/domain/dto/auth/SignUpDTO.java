package com.koreatravel.tabitomo.domain.dto.auth;

import lombok.Data;
import java.time.LocalDate;

@Data
// 회원가입 DTO
public class SignUpDTO {
    private String email;            // 이메일 (hidden input)
    private String password;         // 비밀번호
    private String nickname;         // 닉네임
    private Integer gender;          // 성별 (1: 남, 2: 여, 3: 기타)
    private String countryCode;      // 국가 코드
    private String languageCode;     // 언어 코드
    private LocalDate birthDate;     // 생년월일
}
