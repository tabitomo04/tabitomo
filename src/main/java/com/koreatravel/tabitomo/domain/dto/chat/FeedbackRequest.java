package com.koreatravel.tabitomo.domain.dto.chat;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class FeedbackRequest {
    private String email; // 추가
    private String nickname; // 추가
    private Integer rating;
    private String feedbackText;
    private Integer isHelpful;
    private Date timestamp;
}
