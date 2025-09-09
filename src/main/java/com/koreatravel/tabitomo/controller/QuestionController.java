package com.koreatravel.tabitomo.controller;

import com.koreatravel.tabitomo.PathConstants;
import com.koreatravel.tabitomo.domain.entity.member.AddInfoEntity;
import com.koreatravel.tabitomo.domain.entity.member.MemberEntity;
import com.koreatravel.tabitomo.domain.entity.member.UserSelectedInfoEntity;
import com.koreatravel.tabitomo.dto.member.QuestionnaireRequest;
import com.koreatravel.tabitomo.exception.ResourceNotFoundException;
import com.koreatravel.tabitomo.repository.member.AddInfoRepository;
import com.koreatravel.tabitomo.repository.member.UserSelectedInfoRepository;
import com.koreatravel.tabitomo.service.member.MemberService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping(PathConstants.QUESTION)
public class QuestionController {

    private final MemberService memberService;
    private final AddInfoRepository addInfoRepository;

    @GetMapping(PathConstants.QUESTION_START)
    public String showStartPage(HttpSession session, RedirectAttributes redirectAttributes) {
        // Check if user is authenticated
        String email = (String) session.getAttribute("authenticatedEmail");
        if (email == null) {
            redirectAttributes.addFlashAttribute("error", "로그인이 필요합니다.");
            return "redirect:/login";
        }
        
        // Check if user has already completed the questions
        try {
            MemberEntity member = memberService.findByEmail(email);
            if (member.isQuestionnaireCompleted()) {
                redirectAttributes.addFlashAttribute("info", "이미 설문조사를 완료하셨습니다.");
                return "redirect:/";
            }
        } catch (ResourceNotFoundException e) {
            log.error("사용자를 찾을 수 없습니다: {}", email);
            redirectAttributes.addFlashAttribute("error", "사용자 정보를 찾을 수 없습니다.");
            return "redirect:/login";
        }
        
        return "question/start";
    }

    @GetMapping(PathConstants.QUESTION_FORM)
    public String showQuestionForm(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        // Check if user is authenticated
        String email = (String) session.getAttribute("authenticatedEmail");
        if (email == null) {
            redirectAttributes.addFlashAttribute("error", "로그인이 필요합니다.");
            return "redirect:/login";
        }
        
        // Load question data from database
        Map<Integer, List<AddInfoEntity>> questions = Map.of(
            1, addInfoRepository.findByInfoHighNum(1), // Hobbies
            2, addInfoRepository.findByInfoHighNum(2), // MBTI
            3, addInfoRepository.findByInfoHighNum(3), // Travel style
            4, addInfoRepository.findByInfoHighNum(4), // Companion
            5, addInfoRepository.findByInfoHighNum(5)  // Food preference
        );
        
        model.addAttribute("questions", questions);
        return "question/form";
    }

    private final UserSelectedInfoRepository userSelectedInfoRepository;

    @PostMapping(PathConstants.QUESTION_SUBMIT)
    @Transactional
    public String submitAnswers(
            @ModelAttribute QuestionnaireRequest request,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        String email = (String) session.getAttribute("authenticatedEmail");
        if (email == null) {
            redirectAttributes.addFlashAttribute("error", "세션이 만료되었습니다. 다시 로그인해주세요.");
            return "redirect:/login";
        }
        
        log.info("Processing questionnaire submission for user: {}", email);
        
        try {
            // 1. Validate and process each question
            List<UserSelectedInfoEntity> userSelections = new ArrayList<>();
            
            // Process hobbies (single selection)
            if (request.getHobbies() != null) {
                processSingleSelection(email, 1, request.getHobbies(), userSelections, "hobby");
            }
            
            // Process MBTI (single selection)
            if (request.getMbti() != null) {
                processMbtiSelection(email, request.getMbti(), userSelections);
            }
            
            // Process travel styles (multiple selection)
            if (request.getTravelStyles() != null && !request.getTravelStyles().isEmpty()) {
                processMultipleSelections(email, 3, request.getTravelStyles(), userSelections, "travel style");
            }
            
            // Process companions (multiple selection)
            if (request.getCompanions() != null && !request.getCompanions().isEmpty()) {
                processMultipleSelections(email, 4, request.getCompanions(), userSelections, "companion");
            }
            
            // Process food preferences (multiple selection)
            if (request.getFoodPreferences() != null && !request.getFoodPreferences().isEmpty()) {
                processMultipleSelections(email, 5, request.getFoodPreferences(), userSelections, "food preference");
            }
            
            // 2. Delete existing selections for this user
            log.debug("Deleting existing selections for user: {}", email);
            userSelectedInfoRepository.deleteByEmail(email);
            
            // 3. Save all new selections if there are any
            if (!userSelections.isEmpty()) {
                log.debug("Saving {} new selections for user: {}", userSelections.size(), email);
                userSelectedInfoRepository.saveAll(userSelections);
            } else {
                log.warn("No valid selections found for user: {}", email);
            }
            
            // 4. Mark questionnaire as completed
            memberService.markQuestionnaireCompleted(email);
            log.info("Successfully completed questionnaire for user: {}", email);
            
            // 5. Clear the authenticated email from session
            session.removeAttribute("authenticatedEmail");
            
            return "redirect:" + PathConstants.QUESTION_COMPLETE;
            
        } catch (ResourceNotFoundException e) {
            log.error("Resource not found while processing questionnaire: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "선택하신 항목을 찾을 수 없습니다. 다시 시도해주세요.");
            return "redirect:" + PathConstants.QUESTION_FORM;
        } catch (Exception e) {
            log.error("Error processing questionnaire for user {}: {}", email, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "설문조사 처리 중 오류가 발생했습니다. 다시 시도해주세요.");
            return "redirect:" + PathConstants.QUESTION_FORM;
        }
    }
    
    private void processSingleSelection(String email, int infoHighNum, Long infoLowNum, 
                                      List<UserSelectedInfoEntity> selections, String selectionType) {
        AddInfoEntity item = addInfoRepository.findByInfoHighNumAndInfoLowNum(infoHighNum, infoLowNum)
            .orElseThrow(() -> new ResourceNotFoundException("Invalid " + selectionType + " selection"));
        
        selections.add(createUserSelectedInfo(email, item));
    }
    
    private void processMbtiSelection(String email, String mbti, List<UserSelectedInfoEntity> selections) {
        AddInfoEntity mbtiItem = addInfoRepository.findByInfoHighNumAndContent(2, mbti)
            .orElseThrow(() -> new ResourceNotFoundException("Invalid MBTI selection"));
        
        selections.add(createUserSelectedInfo(email, mbtiItem));
    }
    
    private void processMultipleSelections(String email, int infoHighNum, List<Long> infoLowNums, 
                                         List<UserSelectedInfoEntity> selections, String selectionType) {
        for (Long infoLowNum : infoLowNums) {
            AddInfoEntity item = addInfoRepository.findByInfoHighNumAndInfoLowNum(infoHighNum, infoLowNum)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid " + selectionType + " selection: " + infoLowNum));
            
            selections.add(createUserSelectedInfo(email, item));
        }
    }
    
    private UserSelectedInfoEntity createUserSelectedInfo(String email, AddInfoEntity item) {
        return UserSelectedInfoEntity.builder()
            .email(email)
            .infoHighNum(item.getInfoHighNum())
            .infoLowNum(item.getInfoLowNum())
            .build();
    }

    @GetMapping(PathConstants.QUESTION_COMPLETE)
    public String showCompletionPage() {
        return "question/complete";
    }
}
