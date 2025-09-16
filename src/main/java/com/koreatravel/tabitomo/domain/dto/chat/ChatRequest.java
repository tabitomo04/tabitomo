package com.koreatravel.tabitomo.dto.chat;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
// ChatRequest.java
public class ChatRequest {
    private String message;
    private List<Map<String, String>> chatHistory;
    private String language;

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