package com.koreatravel.tabitomo.domain.dto.chat;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

// ChatRequest.java
@Getter
@Setter
public class ChatRequestDTO {
    private String message;
    private String language;
    private List<Map<String, String>> chatHistory;
}