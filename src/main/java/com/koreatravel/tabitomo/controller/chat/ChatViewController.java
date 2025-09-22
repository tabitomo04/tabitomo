package com.koreatravel.tabitomo.controller.chat;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller // View(HTML)를 반환하는 컨트롤러
public class ChatViewController {

    @GetMapping("/chatbot/intro")
    public String chatbotIntro() {
        return "chatbot-intro"; // src/main/resources/templates/chatbot-intro.html 반환
    }
}