package com.koreatravel.tabitomo.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class FeedbackRequestDTO {
    private Integer rating;
    private String feedbackText;
    private List<Map<String, String>> chatHistory; // chatHistory 필드 추가
    private Date timestamp;
}
