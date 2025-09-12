package com.koreatravel.tabitomo.controller;

import com.koreatravel.tabitomo.PathConstants;
import com.koreatravel.tabitomo.domain.dto.member.QuestionAnswersDTO;
import com.koreatravel.tabitomo.domain.entity.member.AddInfoEntity;
import com.koreatravel.tabitomo.domain.entity.member.MemberAddInfoEntity;
import com.koreatravel.tabitomo.domain.entity.member.MemberEntity;
import com.koreatravel.tabitomo.id.MemberAddInfoId;
import java.util.Optional;
import java.util.UUID;
import com.koreatravel.tabitomo.exception.ResourceNotFoundException;
import com.koreatravel.tabitomo.repository.member.AddInfoRepository;
import com.koreatravel.tabitomo.service.member.MemberService;
import com.koreatravel.tabitomo.repository.member.MemberAddInfoRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping(PathConstants.QUESTION)
public class QuestionController {

    private final MemberAddInfoRepository memberAddInfoRepository;
    private final AddInfoRepository addInfoRepository;
    private final MemberService memberService;

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

    @PostMapping(PathConstants.QUESTION_SUBMIT)
    @Transactional
    public String submitAnswers(
            @ModelAttribute QuestionAnswersDTO request,
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
            List<MemberAddInfoEntity> memberAddInfos = new ArrayList<>();
            
            // Process hobbies (single selection)
            if (request.getHobbies() != null) {
                processMultipleSelections(email, 1, request.getHobbies(), memberAddInfos, "hobby");
            }
            
            // Process MBTI (single selection)
            if (request.getMbti() != null) {
                processMbtiSelection(email, request.getMbti(), memberAddInfos);
            }
            
            // Process travel style (multiple selection)
            if (request.getTravelStyles() != null && !request.getTravelStyles().isEmpty()) {
                processMultipleSelections(email, 3, request.getTravelStyles(), memberAddInfos, "travel style");
            }
            
            // Process companion (multiple selection)
            if (request.getCompanions() != null && !request.getCompanions().isEmpty()) {
                processMultipleSelections(email, 4, request.getCompanions(), memberAddInfos, "companion");
            }
            
            // Process food preference (multiple selection)
            if (request.getFoodPreferences() != null && !request.getFoodPreferences().isEmpty()) {
                processMultipleSelections(email, 5, request.getFoodPreferences(), memberAddInfos, "food preference");
            }
            
            // 2. Delete existing user selections by member ID
            UUID memberId = memberService.getMemberIdByEmail(email);
            memberAddInfoRepository.deleteByMemberId(memberId);
            
            // 3. Save new selections if any
            if (!memberAddInfos.isEmpty()) {
                memberAddInfoRepository.saveAll(memberAddInfos);
            }
            
            // 4. Mark questionnaire as completed
            memberService.markQuestionnaireCompleted(email);
            log.info("Successfully completed questionnaire for user: {}", email);
            
            // Save all selected items
            memberAddInfoRepository.saveAll(memberAddInfos);
        
            // Add user's email to the session to mark as completed
            session.setAttribute("questionnaireCompleted", true);
            session.setAttribute("userEmail", email);
            
            // Redirect to completion page
            return "redirect:" + PathConstants.QUESTION + PathConstants.QUESTION_COMPLETE;
            
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
    
    private void processMbtiSelection(String email, String mbti, List<MemberAddInfoEntity> selections) {
        // MBTI is stored with infoHighNum = 2
        int mbtiInfoHighNum = 2;
        
        // Find the AddInfoEntity for the MBTI type
        Optional<AddInfoEntity> addInfoOpt = addInfoRepository.findByInfoHighNumAndContent(mbtiInfoHighNum, mbti);
        
        if (addInfoOpt.isEmpty()) {
            log.warn("MBTI type not found: {}", mbti);
            return;
        }
        
        AddInfoEntity addInfo = addInfoOpt.get();
        
        // Create a new MemberAddInfoEntity
        UUID memberId = memberService.getMemberIdByEmail(email);
        MemberEntity member = memberService.findByEmail(email);
        
        // Create the composite ID
        MemberAddInfoId id = new MemberAddInfoId(memberId, addInfo.getInfoHighNum(), addInfo.getInfoLowNum());
        
        // Create and set up the entity
        MemberAddInfoEntity memberAddInfo = MemberAddInfoEntity.builder()
            .id(id)
            .member(member)
            .addInfo(addInfo)
            .createdAt(LocalDateTime.now())
            .build();
            
        selections.add(memberAddInfo);
    }
    
    private void processMultipleSelections(String email, int infoHighNum, List<Long> infoLowNums, 
                                         List<MemberAddInfoEntity> selections, String selectionType) {
        // Get member ID and member entity
        UUID memberId = memberService.getMemberIdByEmail(email);
        MemberEntity member = memberService.findByEmail(email);
        
        // Find all items with the given infoHighNum and infoLowNums
        for (Long infoLowNum : infoLowNums) {
            // Convert Long to Integer for infoLowNum
            final int lowNum = infoLowNum.intValue();
            
            // Find the item with matching infoHighNum and infoLowNum
            AddInfoEntity item = addInfoRepository.findByInfoHighNumAndInfoLowNum(infoHighNum, lowNum)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid " + selectionType + " selection: " + infoLowNum));
            
            // Check if the member already has this info
            List<MemberAddInfoEntity> existingInfos = memberAddInfoRepository.findByMemberIdAndAddInfoInfoHighNumAndAddInfoInfoLowNum(
                memberId, infoHighNum, lowNum);
                
            if (existingInfos.isEmpty()) {
                // Create the composite ID
                MemberAddInfoId id = new MemberAddInfoId(memberId, item.getInfoHighNum(), item.getInfoLowNum());
                
                // Create and set up the entity
                MemberAddInfoEntity memberAddInfo = MemberAddInfoEntity.builder()
                    .id(id)
                    .member(member)
                    .addInfo(item)
                    .createdAt(LocalDateTime.now())
                    .build();
                
                selections.add(memberAddInfo);
            } else {
                // Add existing info to selections
                selections.addAll(existingInfos);
            }
        }
    }
    

    @GetMapping(PathConstants.QUESTION_COMPLETE)
    public String showCompletionPage() {
        return "question/complete";
    }
}
