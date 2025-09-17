package com.koreatravel.tabitomo.controller;

import com.koreatravel.tabitomo.domain.dto.MemberDTO;
import com.koreatravel.tabitomo.domain.entity.MemberEntity;
import com.koreatravel.tabitomo.service.MemberService;
import jakarta.servlet.http.HttpSession; // 추가된 import
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException; // IOException 추가
import java.util.HashMap;
import java.util.Map;

@Controller
public class AuthController {
    private final MemberService memberService;

    @Autowired
    public AuthController(MemberService memberService) {
        this.memberService = memberService;
    }
    // 닉네임 중복 확인 엔드포인트
    @GetMapping("/checkNickname")
    @ResponseBody
    public Map<String, Boolean> checkNickname(@RequestParam("nickname") String nickname) {
        boolean isDuplicate = memberService.checkNicknameDuplicate(nickname);
        Map<String, Boolean> response = new HashMap<>();
        response.put("isDuplicate", isDuplicate);
        return response;
    }
    // 로그인 페이지 이동
    @GetMapping("/login")
    public String loginPage() {
        return "loginform";
    }

    // 회원가입 페이지 이동
    @GetMapping("/signup")
    public String signupPage() {
        return "signupform";
    }

    // 회원가입 처리
    @PostMapping("/signup")
    public String signup(@ModelAttribute MemberDTO member, Model model) {
        MemberEntity memberEntity = MemberDTO.setEntity(member);
        memberService.register(memberEntity);
        model.addAttribute("message", "회원가입이 완료되었습니다!");
        return "loginform";
    }

    // 로그인 처리
    @PostMapping("/login")
    public String login(@ModelAttribute MemberDTO member, Model model, HttpSession session) {
        MemberEntity loginMember = memberService.login(member.getEmail(), member.getPassword());

        if (loginMember != null) {
            session.setAttribute("memberId", loginMember.getEmail());
            session.setAttribute("loginNickname", loginMember.getNickname()); // 닉네임 추가
            model.addAttribute("message", "로그인 성공!");
            return "redirect:/"; // 로그인 성공 시 메인 페이지로 리디렉션
        } else {
            model.addAttribute("message", "로그인 실패: 아이디 또는 비밀번호가 올바르지 않습니다.");
            return "loginform";
        }
    }

    // 로그아웃 처리
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // 세션 무효화
        return "redirect:/";
    }

    // 회원정보 수정 페이지 이동
    @GetMapping("/member/edit")
    public String editMemberPage(HttpSession session, Model model) {
        String memberId = (String) session.getAttribute("memberId");

        // 로그인 여부 확인
        if (memberId == null) {
            return "redirect:/login"; // 로그인하지 않았다면 로그인 페이지로 리디렉션
        }

        MemberEntity memberEntity = memberService.getMemberById(memberId);
        model.addAttribute("member", memberEntity);

        return "membereditform"; // templates/membereditform.html
    }

    // 회원정보 수정 처리
    @PostMapping("/member/edit")
    public String editMember(
            @ModelAttribute MemberDTO member,
            @RequestParam("profileImage") MultipartFile file, // 프로필 이미지 파일 추가
            Model model,
            HttpSession session) {
        System.out.println(member);
        String memberId = (String) session.getAttribute("memberId");

        // 로그인 여부 확인
        if (memberId == null) {
            return "redirect:/login";
        }

        try {
            // MemberService의 updateMember 메서드에 DTO와 파일 모두 전달
            memberService.updateMember(member, file);
            // 세션 닉네임 업데이트
            session.setAttribute("loginNickname", member.getNickname());
            model.addAttribute("message", "회원정보가 수정되었습니다.");
        } catch (IOException e) {
            // 파일 업로드 실패 등 예외 처리
            model.addAttribute("error", "파일 업로드에 실패했습니다.");
            return "membereditform";
        }

        // 수정 후 다시 수정 페이지로 리디렉션
        return "redirect:/member/edit";
    }
}
