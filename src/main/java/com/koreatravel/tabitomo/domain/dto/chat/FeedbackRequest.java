package com.koreatravel.tabitomo.domain.dto.chat;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class FeedbackRequest {
    private Integer rating;
    private String feedbackText;
    private List<Map<String, String>> chatHistory; // chatHistory 필드 추가
    private Date timestamp;
    private Integer isHelpful;
}
