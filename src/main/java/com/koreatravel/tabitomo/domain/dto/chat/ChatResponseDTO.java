package com.koreatravel.tabitomo.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor // 기본 생성자 추가
public class ChatResponseDTO {
    private String reply;
    private String answerSource;
    private String answer;
    private Integer feedbackId;

    public ChatResponseDTO(String reply) {
        this.reply = reply;
    }
}
