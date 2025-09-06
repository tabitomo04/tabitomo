package com.koreatravel.tabitomo.domain.dto.chat;

import java.util.List;
import java.util.Map;

// ChatRequest.java
public class ChatRequestDTO {
    private String message;
    private List<Map<String, String>> chatHistory;

    // Getters and Setters
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public List<Map<String, String>> getChatHistory() {
        return chatHistory;
    }
    public void setChatHistory(List<Map<String, String>> chatHistory) {
        this.chatHistory = chatHistory;
    }
}