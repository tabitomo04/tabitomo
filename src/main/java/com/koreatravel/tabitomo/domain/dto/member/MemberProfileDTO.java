package com.koreatravel.tabitomo.domain.dto.member;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.koreatravel.tabitomo.domain.dto.storybook.LikedbookDTO;
import com.koreatravel.tabitomo.domain.dto.storybook.StorybookDTO;
import com.koreatravel.tabitomo.domain.dto.storybook.TempsaveDTO;
import com.koreatravel.tabitomo.domain.dto.trip.TripPlan;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 회원 프로필 정보를 담는 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberProfileDTO {
    // 기본 정보
    private UUID id;
    
    @Email(message = "유효한 이메일 주소를 입력해주세요.")
    private String email;
    
    @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하로 입력해주세요.")
    private String nickname;
    
    private String profileImageUrl;
    private String introduction;
    private String role;
    
    // 개인 정보
    @Past(message = "유효한 생년월일을 입력해주세요.")
    private LocalDate dateOfBirth;
    
    // 1: 남성, 2: 여성, 3: 기타
    private Integer gender;
    
    // 국가 정보
    private String countryCode;
    private String countryName;
    
    // 선호 언어 정보
    private Integer preferredLanguageId;
    private String preferredLanguageName;
    
    // 추가 정보
    private boolean showGender;
    private boolean showAge;
    private boolean isActive;
    private boolean questionnaireCompleted;
    
    // 타임스탬프
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 연관 데이터 (필요한 경우에만 로드)
    private CountryDTO country;
    private LanguageDTO language;
    private List<StorybookDTO> storybooks;
    private List<LikedbookDTO> likedbooks;
    private List<TempsaveDTO> tempsaves;
    private List<TripPlan> tripplans;
    
    // Helper methods
    public Integer getAge() {
        if (dateOfBirth == null) {
            return null;
        }
        return LocalDate.now().getYear() - dateOfBirth.getYear();
    }
    
    public String getGenderDisplay() {
        if (gender == null) return "미설정";
        return switch (gender) {
            case 1 -> "남성";
            case 2 -> "여성";
            case 3 -> "기타";
            default -> "미설정";
        };
    }
}