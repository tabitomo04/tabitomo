package com.koreatravel.tabitomo.controller;

import com.koreatravel.tabitomo.PathConstants;
import com.koreatravel.tabitomo.domain.dto.MemberDTO;
import com.koreatravel.tabitomo.service.AuthService;
import lombok.RequiredArgsConstructor;

import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping(PathConstants.LOGIN)
    public String login() {
        return "auth/login";
    }

    @GetMapping(PathConstants.JOIN)
    public String showRegistrationForm(Model model) {
        model.addAttribute("memberDTO", new MemberDTO());
        return "auth/join";
    }

    @PostMapping(PathConstants.JOIN)
    public String registerUser(@ModelAttribute("memberDTO") MemberDTO memberDTO) {
        authService.registerNewMember(memberDTO);
        return "redirect:" + PathConstants.LOGIN + "?registered";
    }
    
    @PostMapping(PathConstants.LOGOUT)
    public String logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.logout(request, response, authentication);
        return "redirect:" + PathConstants.LOGIN + "?logout";
    }
}
