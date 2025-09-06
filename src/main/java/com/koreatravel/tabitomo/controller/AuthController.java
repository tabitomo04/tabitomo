package com.koreatravel.tabitomo.controller;

import com.koreatravel.tabitomo.domain.dto.member.MemberDTO;
import com.koreatravel.tabitomo.domain.entity.member.MemberEntity;
import com.koreatravel.tabitomo.service.member.MemberService;
import com.koreatravel.tabitomo.PathConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.WebAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthController {
    private final MemberService memberService;

    // 로그인 페이지 이동
    @GetMapping(PathConstants.LOGIN)
    public String loginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "exception", required = false) String exception,
            Model model) {
        
        model.addAttribute("error", error);
        model.addAttribute("exception", exception);
        return "loginform";
    }

    // 회원가입 페이지 이동
    @GetMapping(PathConstants.SIGNUP)
    public String signupPage(Model model) {
        if (!model.containsAttribute("memberDTO")) {
            model.addAttribute("memberDTO", new MemberDTO());
        }
        return "signupform";
    }

    // 회원가입 처리
    @PostMapping(PathConstants.SIGNUP)
    public String signup(
            @Valid @ModelAttribute("memberDTO") MemberDTO memberDTO,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        
        // 비밀번호 확인 검증
        if (!memberDTO.getPassword().equals(memberDTO.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.memberDTO", "비밀번호가 일치하지 않습니다.");
        }
        
        if (bindingResult.hasErrors()) {
            log.warn("회원가입 유효성 검사 실패: {}", bindingResult.getAllErrors());
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.memberDTO", bindingResult);
            redirectAttributes.addFlashAttribute("memberDTO", memberDTO);
            return "redirect:" + PathConstants.SIGNUP;
        }
        
        try {
            // DTO → Entity 변환
            MemberEntity memberEntity = MemberDTO.setEntity(memberDTO);
            // DB 저장
            memberService.register(memberEntity);
            
            redirectAttributes.addFlashAttribute("message", "회원가입이 완료되었습니다. 로그인해주세요.");
            return "redirect:" + PathConstants.LOGIN;
            
        } catch (DataIntegrityViolationException e) {
            log.error("회원가입 중 데이터 무결성 오류 발생: {}", e.getMessage());
            bindingResult.reject("signup.failed", "이미 사용 중인 이메일 또는 닉네임입니다.");
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.memberDTO", bindingResult);
            redirectAttributes.addFlashAttribute("memberDTO", memberDTO);
            return "redirect:" + PathConstants.SIGNUP;
            
        } catch (Exception e) {
            log.error("회원가입 처리 중 오류 발생: {}", e.getMessage(), e);
            bindingResult.reject("signup.failed", "회원가입 처리 중 오류가 발생했습니다.");
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.memberDTO", bindingResult);
            redirectAttributes.addFlashAttribute("memberDTO", memberDTO);
            return "redirect:" + PathConstants.SIGNUP;
        }
    }

    // 로그인 성공 핸들러 (Spring Security가 처리)
    @GetMapping("/login/success")
    public String loginSuccess() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("로그인 성공: {}", auth.getName());
        return "redirect:/";
    }
    
    // 로그인 실패 핸들러 (Spring Security가 처리)
    @GetMapping("/login/failure")
    public String loginFailure(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        HttpSession session = request.getSession(false);
        String errorMessage = "아이디 또는 비밀번호가 올바르지 않습니다.";
        
        if (session != null) {
            Exception exception = (Exception) request.getSession().getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
            if (exception != null) {
                errorMessage = exception.getMessage();
            }
        }
        
        redirectAttributes.addFlashAttribute("error", true);
        redirectAttributes.addFlashAttribute("message", errorMessage);
        return "redirect:" + PathConstants.LOGIN;
    }
    
    // 로그아웃 성공 핸들러 (Spring Security가 처리)
    @GetMapping("/logout/success")
    public String logoutSuccess(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("message", "로그아웃 되었습니다.");
        return "redirect:" + PathConstants.LOGIN;
    }
}
