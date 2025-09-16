package com.koreatravel.tabitomo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {
    @GetMapping("/")
    public String main() {
        // 메인 페이지 관련 로직 추가 예정
        return "index";
    }
}
