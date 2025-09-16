package com.koreatravel.tabitomo.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor // 기본 생성자 추가
public class ChatResponse {
    private String reply;
    private String answerSource;
    private String answer;
    private Integer feedbackId;

    public ChatResponse(String reply) {
        this.reply = reply;
    }
}
