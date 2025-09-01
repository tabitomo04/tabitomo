package com.koreatravel.tabitomo.controller;

import lombok.RequiredArgsConstructor;
import com.koreatravel.tabitomo.PathConstants;
import com.koreatravel.tabitomo.domain.dto.MemberFormDTO;
import com.koreatravel.tabitomo.domain.entity.AddInfoEntity;
import com.koreatravel.tabitomo.domain.dto.MemberDTO;
import com.koreatravel.tabitomo.service.MemberService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@RequiredArgsConstructor
@Controller
public class MemberController {

    private final MemberService memberService;
    
    @GetMapping(PathConstants.MEMBER_INFO)
    public String memberInfo(@AuthenticationPrincipal String email, Model model) {
        MemberDTO member = memberService.getMemberByEmail(email);
        model.addAttribute("member", member);
        return "member/info";
    }

    @GetMapping(PathConstants.MEMBER_QUESTION_START)
    public String memberQuestionStart(@AuthenticationPrincipal String email) {
        return "member/question/start";
    }

    @GetMapping(PathConstants.MEMBER_QUESTION_FORM)
    public String memberQuestionForm(@AuthenticationPrincipal String email, Model model) {
        List<AddInfoEntity> addInfoList = memberService.getAddInfoList();
        model.addAttribute("addInfoList", addInfoList);
        return "member/question/form";
    }

    @PostMapping(PathConstants.MEMBER_QUESTION_FORM)
    public String memberQuestionFormSubmit(@AuthenticationPrincipal String email, @ModelAttribute List<MemberFormDTO> memberFormDTO) {
        memberService.saveUserSelectedInfo(email, memberFormDTO);
        return "redirect:" + PathConstants.MEMBER_QUESTION_COMPLETE;
    }

    @GetMapping(PathConstants.MEMBER_QUESTION_COMPLETE)
    public String memberQuestionComplete() {
        return "member/question/complete";
    }
}
