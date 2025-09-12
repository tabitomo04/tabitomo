package com.koreatravel.tabitomo.domain.dto.member;

import lombok.Data;

import java.util.List;

@Data
public class QuestionAnswersDTO {
    private List<Long> hobbies;          // info_high_num = 1
    private String mbti;                 // info_high_num = 2
    private List<Long> travelStyles;     // info_high_num = 3
    private List<Long> companions;       // info_high_num = 4
    private List<Long> foodPreferences;  // info_high_num = 5
}
