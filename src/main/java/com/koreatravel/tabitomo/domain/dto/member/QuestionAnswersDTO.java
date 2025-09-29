package com.koreatravel.tabitomo.domain.dto.member;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * 회원의 설문 응답 정보를 담는 DTO
 * 각 필드는 설문조사의 질문 그룹에 대응됩니다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionAnswersDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 회원 ID */
    @NotNull(message = "회원 ID는 필수 입력값입니다.")
    private UUID memberId;

    /** 취미 목록 (info_high_num = 1) */
    @NotEmpty(message = "하나 이상의 취미를 선택해주세요.")
    private List<Long> hobbies;

    /** MBTI 유형 (info_high_num = 2) - 문자열 형식 (예: "INTJ") */
    private String mbti;
    
    /** MBTI ID (숫자) */
    private Long mbtiId;

    /** 여행 스타일 목록 (info_high_num = 3) */
    @Size(min = 1, message = "하나 이상의 여행 스타일을 선택해주세요.")
    private List<Long> travelStyles;

    /** 동반자 유형 목록 (info_high_num = 4) */
    @Size(min = 1, message = "하나 이상의 동반자 유형을 선택해주세요.")
    private List<Long> companions;

    /** 음식 선호도 목록 (info_high_num = 5) */
    @Size(min = 1, message = "하나 이상의 음식 선호도를 선택해주세요.")
    private List<Long> foodPreferences;

    /**
     * 모든 필수 필드가 채워져 있는지 확인합니다.
     * @return 모든 필수 필드가 유효하면 true, 그렇지 않으면 false
     */
    public boolean isComplete() {
        return memberId != null && 
               hobbies != null && !hobbies.isEmpty() &&
               mbti != null && !mbti.trim().isEmpty() &&
               travelStyles != null && !travelStyles.isEmpty() &&
               companions != null && !companions.isEmpty() &&
               foodPreferences != null && !foodPreferences.isEmpty();
    }
}
