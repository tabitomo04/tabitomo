package com.koreatravel.tabitomo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "home";  // templates/home.html 렌더링
    }

    @GetMapping("/chat")
    public String chatPage() {
        return "chat";  // templates/chat.html 렌더링
    }
}

