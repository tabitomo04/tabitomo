package com.koreatravel.tabitomo.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {
    
    @GetMapping("/")
    public String main(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            model.addAttribute("isLoggedIn", true);
            model.addAttribute("nickname", auth.getName());
            // You can add more user details here if needed
            // model.addAttribute("email", auth.getPrincipal().getEmail());
        } else {
            model.addAttribute("isLoggedIn", false);
        }
        return "index";
    }
    
}
