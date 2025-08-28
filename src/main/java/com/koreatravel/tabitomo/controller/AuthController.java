package com.koreatravel.tabitomo.controller;

import com.koreatravel.tabitomo.PathConstants;
import com.koreatravel.tabitomo.domain.dto.MemberDTO;
import com.koreatravel.tabitomo.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final MemberService memberService;

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
        memberService.registerNewMember(memberDTO);
        return "redirect:" + PathConstants.LOGIN + "?registered";
    }
    
    @PostMapping(PathConstants.LOGOUT)
    public String logout() {
        return "redirect:" + PathConstants.LOGIN + "?logout";
    }
}
