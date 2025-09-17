package com.koreantravel.tabitomo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CustomerServiceController {

    @GetMapping("/about")
    public String about() {
        return "customerservice/about";
    }

    @GetMapping("/contact")
    public String contact() {
        return "customerservice/contact";
    }

    @GetMapping("/privacy")
    public String privacy() {
        return "customerservice/privacy";
    }

    @GetMapping("/terms")
    public String terms() {
        return "customerservice/terms";
    }
}
