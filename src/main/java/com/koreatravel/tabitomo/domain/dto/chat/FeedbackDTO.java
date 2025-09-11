package com.koreatravel.tabitomo.domain.dto.chat;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeedbackDTO {
    private Integer booknum;
    private String email;
    private Integer rating; // 1-5
    private String comment;
}
