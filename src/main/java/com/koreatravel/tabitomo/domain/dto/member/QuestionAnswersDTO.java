package com.koreatravel.tabitomo.domain.dto.member;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class QuestionAnswersDTO {
    @NotNull(message = "회원 ID는 필수 입력값입니다.")
    private UUID memberId;
    private List<Long> hobbies;          // info_high_num = 1
    private String mbti;                 // info_high_num = 2
    private List<Long> travelStyles;     // info_high_num = 3
    private List<Long> companions;       // info_high_num = 4
    private List<Long> foodPreferences;  // info_high_num = 5
}
