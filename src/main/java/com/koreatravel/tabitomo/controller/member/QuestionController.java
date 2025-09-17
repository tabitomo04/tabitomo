package com.koreatravel.tabitomo.controller.member;

import com.koreatravel.tabitomo.PathConstants;
import com.koreatravel.tabitomo.domain.dto.member.QuestionAnswersDTO;
import com.koreatravel.tabitomo.domain.entity.member.AddInfoEntity;
import com.koreatravel.tabitomo.domain.entity.member.MemberEntity;
import com.koreatravel.tabitomo.domain.entity.member.MemberId;
import com.koreatravel.tabitomo.exception.ResourceNotFoundException;
import com.koreatravel.tabitomo.repository.member.AddInfoRepository;
import com.koreatravel.tabitomo.service.member.AddInfoService;
import com.koreatravel.tabitomo.service.member.MemberService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

import java.util.*;

@Slf4j
@Controller
@RequiredArgsConstructor
public class QuestionController {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(QuestionController.class);

    private final AddInfoService addInfoService;
    private final AddInfoRepository addInfoRepository;
    private final MemberService memberService;

    @GetMapping(PathConstants.QUESTION_START)
    public String showStartPage(HttpSession session, RedirectAttributes redirectAttributes) {
        String email = (String) session.getAttribute("authenticatedEmail");
        if (email == null) {
            redirectAttributes.addFlashAttribute("error", "로그인이 필요합니다.");
            return "redirect:/login";
        }
        
        try {
            MemberEntity member = memberService.findByEmail(email);
            if (member.isQuestionnaireCompleted()) {
                return "redirect:/";
            }
            return "question/start";
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", "사용자 정보를 찾을 수 없습니다.");
            return "redirect:/";
        }
    }

    @GetMapping(PathConstants.QUESTION_FORM)
    public String showQuestionForm(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        String email = (String) session.getAttribute("authenticatedEmail");
        if (email == null) {
            redirectAttributes.addFlashAttribute("error", "로그인이 필요합니다.");
            return "redirect:/login";
        }

        try {
            MemberEntity member = memberService.findByEmail(email);
            if (member.isQuestionnaireCompleted()) {
                return "redirect:/";
            }

            // Load all questions by category
            Map<Integer, List<AddInfoEntity>> questionsByCategory = new HashMap<>();
            for (int i = 1; i <= 5; i++) {
                questionsByCategory.put(i, addInfoRepository.findByInfoHighNum(i));
            }
            model.addAttribute("questionsByCategory", questionsByCategory);
            model.addAttribute("questionAnswers", new QuestionAnswersDTO());
            
            return "question/form";
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", "사용자 정보를 찾을 수 없습니다.");
            return "redirect:/";
        }
    }

    @PostMapping(PathConstants.QUESTION_FORM)
    @Transactional
    public String submitAnswers(@Valid @ModelAttribute("questionAnswers") QuestionAnswersDTO answers,
                              BindingResult bindingResult,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        // 세션에서 인증된 사용자 ID 확인
        Long memberId = (Long) session.getAttribute("memberId");
        if (memberId == null) {
            redirectAttributes.addFlashAttribute("error", "로그인이 필요합니다.");
            return "redirect:/login";
        }

        try {
            // DTO의 memberId와 세션의 사용자 ID 일치 여부 확인 (보안 강화)
            if (!memberId.equals(answers.getMemberId())) {
                redirectAttributes.addFlashAttribute("error", "잘못된 접근입니다.");
                return "redirect:/";
            }
            
            // 사용자 조회
            MemberId id = new MemberId(memberId);
            MemberEntity member = memberService.findById(id);
            
            // 이미 설문을 완료한 경우
            if (member.isQuestionnaireCompleted()) {
                return "redirect:/";
            }

            // Save answers
            saveAnswers(answers.getMemberId(), answers);
            
            // Update member's questionnaire status
            member.setQuestionnaireCompleted(true);
            memberService.updateMember(member);
            
            return "redirect:" + PathConstants.QUESTION_COMPLETE;
        } catch (ResourceNotFoundException e) {
            log.error("Member not found: ", e);
            redirectAttributes.addFlashAttribute("error", "사용자 정보를 찾을 수 없습니다.");
            return "redirect:/";
        } catch (Exception e) {
            log.error("Error saving answers: ", e);
            redirectAttributes.addFlashAttribute("error", "답변 저장 중 오류가 발생했습니다.");
            return "redirect:" + PathConstants.QUESTION_FORM;
        }
    }

    private void saveAnswers(Long memberId, QuestionAnswersDTO answers) {
        // Save hobbies (info_high_num = 1)
        if (answers.getHobbies() != null) {
            for (Long infoLowNum : answers.getHobbies()) {
                addInfoService.saveMemberAddInfo(memberId, 1, infoLowNum.intValue());
            }
        }

        // Save MBTI (info_high_num = 2)
        if (answers.getMbti() != null && !answers.getMbti().isEmpty()) {
            addInfoService.saveMemberAddInfo(memberId, 2, 0);
        }

        // Save travel styles (info_high_num = 3)
        if (answers.getTravelStyles() != null) {
            for (Long infoLowNum : answers.getTravelStyles()) {
                addInfoService.saveMemberAddInfo(memberId, 3, infoLowNum.intValue());
            }
        }

        // Save companions (info_high_num = 4)
        if (answers.getCompanions() != null) {
            for (Long infoLowNum : answers.getCompanions()) {
                addInfoService.saveMemberAddInfo(memberId, 4, infoLowNum.intValue());
            }
        }

        // Save food preferences (info_high_num = 5)
        if (answers.getFoodPreferences() != null) {
            for (Long infoLowNum : answers.getFoodPreferences()) {
                addInfoService.saveMemberAddInfo(memberId, 5, infoLowNum.intValue());
            }
        }
    }


    @GetMapping(PathConstants.QUESTION_COMPLETE)
    public String showCompletionPage() {
        return "question/complete";
    }
}
