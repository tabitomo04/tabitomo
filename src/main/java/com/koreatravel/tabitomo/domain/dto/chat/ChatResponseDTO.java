package com.koreatravel.tabitomo.domain.dto.chat;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class ChatResponseDTO {
    private String reply;
    private String answerSource;
    private String answer;
    private Integer feedbackId;
    private String response;  // For chat response
    private String language;  // For language setting

    public ChatResponseDTO(String reply) {
        this.reply = reply;
    }
    
    // Builder pattern implementation
    public static ChatResponseDTOBuilder builder() {
        return new ChatResponseDTOBuilder();
    }
}
