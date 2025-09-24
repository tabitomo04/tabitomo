package com.koreatravel.tabitomo.domain.dto.auth;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;

@Data
// 회원가입 DTO
public class SignUpDTO {
    private String emailId;          // 이메일 아이디
    private String emailDomain;      // 이메일 도메인
    private String password;         // 비밀번호
    private String confirmPassword;  // 비밀번호 확인
    private String nickname;         // 닉네임
    private Integer gender;          // 성별 (1: 남, 2: 여, 3: 기타)
    private String countryCode;      // 국가 코드
    private String languageCode;     // 언어 코드
    @NotNull(message = "생년월일은 필수 입력 값입니다.")
    @Past(message = "유효한 생년월일을 입력해주세요.")
    private LocalDate dateOfBirth;     // 생년월일
    
    // 전체 이메일 주소 반환
    public String getEmail() {
        if (emailId == null || emailDomain == null) {
            return null;
        }
        return emailId + "@" + emailDomain;
    }
}
