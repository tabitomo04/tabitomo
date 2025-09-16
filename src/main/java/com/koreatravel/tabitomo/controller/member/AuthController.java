package com.koreatravel.tabitomo.controller.member;

import com.koreatravel.tabitomo.domain.dto.member.CountryDTO;
import com.koreatravel.tabitomo.domain.dto.member.LanguageDTO;
import com.koreatravel.tabitomo.domain.dto.auth.MemberDTO;
import com.koreatravel.tabitomo.service.member.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import org.springframework.validation.BindingResult;
import javax.validation.Valid;

@Controller
@RequestMapping("/auth")
public class AuthController {
    private final MemberService memberService;

    @Autowired
    public AuthController(MemberService memberService) {
        this.memberService = memberService;
    }

    // 로그인 페이지 이동
    @GetMapping("/login")
    public String loginPage() {
        return "loginform"; // templates/login.html
    }

    @GetMapping("/signup")
    public String signupPage(Model model) {
        List<CountryDTO> countries = memberService.getAllCountries();
        List<LanguageDTO> languages = memberService.getAllActiveLanguages();
        
        model.addAttribute("countries", countries);
        model.addAttribute("languages", languages);
        return "signupform";
    }

    // 회원가입 처리
    @PostMapping("/signup")
    public String signup(@Valid @ModelAttribute("member") MemberDTO memberDTO, 
                        BindingResult result, 
                        Model model) {
        if (result.hasErrors()) {
            model.addAttribute("countries", memberService.getAllCountries());
            model.addAttribute("languages", memberService.getAllActiveLanguages());
            return "signupform";
        }
        
        // Process the signup with country and language
        memberService.register(memberDTO);
        return "redirect:/auth/signup_success";
    }

    @GetMapping("/signup_success")
    public String signupSuccess() {
        return "signup_success";
    }

    // 로그인 처리
    @PostMapping("/login")
    public String login(Model model) {
        // TODO: 로그인 검증 (서비스 호출 → 세션 저장)
        model.addAttribute("message", "로그인 성공!");
        return "index"; // 로그인 성공 시 메인 페이지 이동
    }
}
