package com.koreatravel.tabitomo.domain.dto.member.member;

import lombok.Data;
import java.util.List;

@Data
public class QuestionnaireRequest {
    private Long hobbies;  // Single selection (radio button)
    private String mbti;   // Single selection (radio button)
    private List<Long> travelStyles;  // Multiple selection (checkboxes)
    private List<Long> companions;    // Multiple selection (checkboxes)
    private List<Long> foodPreferences; // Multiple selection (checkboxes)
}
