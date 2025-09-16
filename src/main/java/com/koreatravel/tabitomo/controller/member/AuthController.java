package com.koreatravel.tabitomo.controller;

import com.koreatravel.tabitomo.domain.dto.MemberDTO;
import com.koreatravel.tabitomo.domain.entity.MemberEntity;
import com.koreatravel.tabitomo.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.koreatravel.tabitomo.PathConstants;

@Controller
public class AuthController {
    private final MemberService memberService;

    @Autowired
    public AuthController(MemberService memberService) {
        this.memberService = memberService;
    }

    // 로그인 페이지 이동
    @GetMapping(PathConstants.LOGIN)
    public String loginPage() {
        return "loginform"; // templates/login.html
    }

    // 회원가입 페이지 이동
    @GetMapping(PathConstants.SIGNUP)
    public String signupPage() {
        return "signupform"; // templates/signup.html
    }

    // 회원가입 처리
    @PostMapping(PathConstants.SIGNUP)
    public String signup(@ModelAttribute MemberDTO member, Model model) {
        // DTO → Entity 변환
        MemberEntity memberEntity = MemberDTO.setEntity(member);

        // DB 저장
        memberService.register(memberEntity);

        model.addAttribute("message", "회원가입이 완료되었습니다!");
        return "loginform"; // 회원가입 후 로그인 페이지로 이동
    }

    // 로그인 처리
    @PostMapping(PathConstants.LOGIN)
    public String login(Model model) {
        // TODO: 로그인 검증 (서비스 호출 → 세션 저장)
        model.addAttribute("message", "로그인 성공!");
        return "index"; // 로그인 성공 시 메인 페이지 이동
    }
}
