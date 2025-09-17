package com.koreatravel.tabitomo.controller.member;

import com.koreatravel.tabitomo.domain.dto.member.CountryDTO;
import com.koreatravel.tabitomo.domain.dto.member.LanguageDTO;
import com.koreatravel.tabitomo.domain.dto.member.MemberProfileDTO;
import com.koreatravel.tabitomo.domain.dto.auth.SignUpDTO;
import com.koreatravel.tabitomo.domain.entity.member.MemberEntity;
import com.koreatravel.tabitomo.service.member.AuthService;
import com.koreatravel.tabitomo.service.member.MemberService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import org.springframework.validation.BindingResult;
import jakarta.validation.Valid;

@Controller
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    private final MemberService memberService;

    // 로그인 페이지 이동
    @GetMapping("/login")
    public String loginPage() {
        return "loginform"; // templates/login.html
    }

    @GetMapping("/signup")
    public String signupPage(Model model) {
        List<CountryDTO> countries = memberService.getAllCountries();
        List<LanguageDTO> languages = memberService.getAllLanguages();

        model.addAttribute("countries", countries);
        model.addAttribute("languages", languages);
        model.addAttribute("member", new SignUpDTO()); // SignUpDTO 추가
        return "signupform";
    }

    // 회원가입 처리
    @PostMapping("/signup")
    public String signup(@Valid @ModelAttribute("member") SignUpDTO member,
            @RequestParam("countryId") int countryId,
            @RequestParam("languageId") int languageId,
            BindingResult result,
            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("countries", memberService.getAllCountries());
            model.addAttribute("languages", memberService.getAllLanguages());
            return "signupform";
        }

        // Process the signup with country and language
        authService.signup(member, countryId, languageId);
        return "redirect:/auth/signup_success";
    }

    @GetMapping("/signup_success")
    public String signupSuccess() {
        return "signup_success";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            // 서비스를 통해 로그인 처리 및 사용자 프로필 가져오기
            MemberProfileDTO memberProfile = authService.login(email, password);
            
            // 세션에 사용자 프로필 저장
            session.setAttribute("user", memberProfile);
            session.setAttribute("authenticatedEmail", email);
            
            // 사용자 정보 조회
            MemberEntity member = memberService.findByEmail(email);
            
            // 질문 완료 여부 확인
            if (!member.isQuestionnaireCompleted()) {
                return "redirect:/question/start";
            }
            
            return "redirect:/";
            
        } catch (BadCredentialsException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/auth/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "로그인 처리 중 오류가 발생했습니다.");
            return "redirect:/auth/login";
        }
    }
}
