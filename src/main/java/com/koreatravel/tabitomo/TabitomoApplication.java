package com.koreatravel.tabitomo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.ai.model.vertexai.autoconfigure.gemini.VertexAiGeminiChatAutoConfiguration;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class, VertexAiGeminiChatAutoConfiguration.class})
public class TabitomoApplication {
    public static void main(String[] args) {
        SpringApplication.run(TabitomoApplication.class, args);
    }
}
