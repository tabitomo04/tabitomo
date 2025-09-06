package com.koreatravel.tabitomo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;

@Controller
public class MainController {

    @GetMapping("/")
    public String main(Model model) {
        // 메인 페이지
        // 세션에 이메일이 있는지 분석 후 로그인/프로필 이미지 변경
        
        return "index";
    }
}
