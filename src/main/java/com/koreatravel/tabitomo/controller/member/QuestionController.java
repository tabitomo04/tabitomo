package com.koreatravel.tabitomo.controller.member;

import com.koreatravel.tabitomo.PathConstants;
import com.koreatravel.tabitomo.domain.dto.member.MemberProfileDTO;
import com.koreatravel.tabitomo.domain.dto.member.QuestionAnswersDTO;
import com.koreatravel.tabitomo.domain.entity.member.AddInfoEntity;
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
    private final AddInfoService addInfoService;
    private final AddInfoRepository addInfoRepository;
    private final MemberService memberService;

    @GetMapping(PathConstants.QUESTION_START)
    public String showStartPage(HttpSession session) {
        MemberProfileDTO userProfile = (MemberProfileDTO) session.getAttribute("user");
        if (userProfile == null || userProfile.isQuestionnaireCompleted()) {
            return "redirect:/";
        }
        return "question/start";
    }

    @GetMapping(PathConstants.QUESTION_FORM)
    public String showQuestionForm(Model model, HttpSession session) {
        MemberProfileDTO userProfile = (MemberProfileDTO) session.getAttribute("user");
        if (userProfile == null || userProfile.isQuestionnaireCompleted()) {
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
    }

    @PostMapping(PathConstants.QUESTION_FORM)
    @Transactional
    public String submitAnswers(
        @Valid @ModelAttribute("questionAnswers") QuestionAnswersDTO answers,
        BindingResult bindingResult,
        HttpSession session,
        RedirectAttributes redirectAttributes) {
        
        // 1. 세션에서 사용자 정보 확인
        MemberProfileDTO userProfile = (MemberProfileDTO) session.getAttribute("user");
        if (userProfile == null) {
            redirectAttributes.addFlashAttribute("error", "로그인이 필요합니다.");
            return "redirect:/login";
        }

        // 2. 이미 설문 완료한 경우
        if (userProfile.isQuestionnaireCompleted()) {
            return "redirect:/";
        }

        // 3. 유효성 검사
        if (bindingResult.hasErrors()) {
            // Load all questions by category for the form
            Map<Integer, List<AddInfoEntity>> questionsByCategory = new HashMap<>();
            for (int i = 1; i <= 5; i++) {
                questionsByCategory.put(i, addInfoRepository.findByInfoHighNum(i));
            }
            redirectAttributes.addFlashAttribute("questionsByCategory", questionsByCategory);
            return "question/form";
        }

        try {
            // 4. 답변 저장
            saveAnswers(userProfile.getId(), answers);
            
            // 5. 세션 업데이트
            userProfile.setQuestionnaireCompleted(true);
            session.setAttribute("user", userProfile);
            session.setAttribute("questionnaireCompleted", true);
            
            // 6. DB 업데이트
            memberService.updateQuestionnaireStatus(userProfile.getId(), true);
            
            return "redirect:" + PathConstants.QUESTION_COMPLETE;
        } catch (Exception e) {
            log.error("Error saving answers: ", e);
            redirectAttributes.addFlashAttribute("error", "답변 저장 중 오류가 발생했습니다.");
            return "redirect:" + PathConstants.QUESTION_FORM;
        }
    }

    private void saveAnswers(UUID memberId, QuestionAnswersDTO answers) {
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
    public String showCompletionPage(HttpSession session) {
        MemberProfileDTO userProfile = (MemberProfileDTO) session.getAttribute("user");
        if (userProfile == null || !userProfile.isQuestionnaireCompleted()) {
            return "redirect:/";
        }
        return "question/complete";
    }
}
