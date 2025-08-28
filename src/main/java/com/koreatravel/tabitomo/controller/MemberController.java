package com.koreatravel.tabitomo.controller;

import com.koreatravel.tabitomo.PathConstants;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MemberController {
    
    @GetMapping(PathConstants.MEMBER_INFO)
    public String memberInfo() {
        return "member/info";
    }

    @GetMapping(PathConstants.MEMBER_QUESTION_START)
    public String memberQuestionStart() {
        return "member/question/start";
    }

    @GetMapping(PathConstants.MEMBER_QUESTION_FORM)
    public String memberQuestionForm() {
        return "member/question/form";
    }

    @GetMapping(PathConstants.MEMBER_QUESTION_COMPLETE)
    public String memberQuestionComplete() {
        return "member/question/complete";
    }
}
