package com.koreantravel.tabitomo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.koreantravel.tabitomo.domain.dto.member.MemberSignupRequestDto;
import com.koreantravel.tabitomo.domain.entity.Member;
import com.koreantravel.tabitomo.service.MemberService;

@Controller
@RequiredArgsConstructor
@RequestMapping
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/signup")
    public String signupForm() {
        return "signup";
    }

    @PostMapping("/signup")
    public String signup(MemberSignupRequestDto requestDto) {
        Member member = requestDto.toEntity();
        memberService.join(member);
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }
}
