package com.koreatravel.tabitomo.controller.member;

import com.koreatravel.tabitomo.PathConstants;
import com.koreatravel.tabitomo.domain.dto.member.QuestionAnswersDTO;
import com.koreatravel.tabitomo.domain.dto.member.AddInfoDTO;
import com.koreatravel.tabitomo.service.member.AddInfoService;
import com.koreatravel.tabitomo.service.member.MemberService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Arrays;
import java.nio.charset.StandardCharsets;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import com.koreatravel.tabitomo.domain.entity.member.MemberEntity;
import com.koreatravel.tabitomo.domain.entity.member.AddInfoEntity;
import jakarta.validation.Valid;


@Slf4j
@Controller
public class QuestionController {
    private final AddInfoService addInfoService;
    private final MemberService memberService;

    public QuestionController(AddInfoService addInfoService, MemberService memberService) {
        this.addInfoService = addInfoService;
        this.memberService = memberService;
    }

    @GetMapping(PathConstants.QUESTION_START)
    public String showStartPage(HttpSession session) {
        // 세션에서 필요한 정보만 확인
        Boolean isCompleted = (Boolean) session.getAttribute("questionnaireCompleted");
        if (session.getAttribute("userId") == null || Boolean.TRUE.equals(isCompleted)) {
            return "redirect:/";
        }
        return "question/start";
    }

    @GetMapping(PathConstants.QUESTION_FORM)
    public String showQuestionForm(Model model, HttpSession session) {
        log.info("Displaying question form");
        
        // 이미 설문을 완료한 경우 홈으로 리다이렉트
        Boolean isCompleted = (Boolean) session.getAttribute("questionnaireCompleted");
        if (Boolean.TRUE.equals(isCompleted)) {
            log.warn("User has already completed the questionnaire, redirecting to home");
            return "redirect:/";
        }

        // 서비스를 통해 모든 카테고리의 질문을 로드
        Map<Integer, List<AddInfoDTO>> questionsByCategory = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            List<AddInfoDTO> questions = addInfoService.getAddInfoByType(i);
            log.debug("Loaded {} questions for category {}", questions.size(), i);
            questionsByCategory.put(i, questions);
        }

        // 모델에 질문 데이터 추가
        model.addAttribute("questionsByCategory", questionsByCategory);
        log.debug("Added questionsByCategory to model");
        
        // QuestionAnswersDTO가 모델에 없을 경우에만 새로 생성하여 추가
        if (!model.containsAttribute("questionAnswers")) {
            log.debug("No existing questionAnswers found in model, creating new instance");
            model.addAttribute("questionAnswers", new QuestionAnswersDTO());
        } else {
            log.debug("Using existing questionAnswers from model");
            Object questionAnswers = model.getAttribute("questionAnswers");
            if (questionAnswers != null) {
                log.debug("questionAnswers class: {}", questionAnswers.getClass().getName());
                if (questionAnswers instanceof QuestionAnswersDTO) {
                    log.debug("questionAnswers content: {}", questionAnswers);
                }
            }
        }

        return "question/form";
    }

    @PostMapping(PathConstants.QUESTION_SUBMIT)
    @Transactional
    @ResponseBody
    public ResponseEntity<Map<String, Object>> submitAnswers(
            @Valid @RequestBody QuestionAnswersDTO answers,
            BindingResult bindingResult,
            HttpSession session,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        MemberEntity member = null;
        
        log.info("=== Form Submission Started ===");
        log.info("Session ID: {}", session.getId());
        log.info("Authentication: {}", authentication != null ? authentication.getName() : "Not authenticated");
        
        // Validate request body
        if (answers == null) {
            log.error("Form data is null! Check if the JSON is properly formatted.");
            response.put("success", false);
            response.put("message", "폼 데이터가 유효하지 않습니다. 다시 시도해주세요.");
            return ResponseEntity.badRequest().body(response);
        }
        
        // Log form data in detail
        log.info("=== Form Data Details ===");
        log.info("Member ID: {}", answers.getMemberId());
        log.info("Hobbies: {}", answers.getHobbies() != null ? answers.getHobbies().size() : 0);
        log.info("MBTI: {}", answers.getMbti());
        log.info("Travel Styles: {}", answers.getTravelStyles() != null ? answers.getTravelStyles().size() : 0);
        log.info("Companions: {}", answers.getCompanions() != null ? answers.getCompanions().size() : 0);
        log.info("Food Preferences: {}", answers.getFoodPreferences() != null ? answers.getFoodPreferences().size() : 0);
        log.debug("Full DTO content: {}", answers);
        
        try {
            // 1. 인증 확인
            if (authentication == null || !authentication.isAuthenticated()) {
                log.warn("User not authenticated, returning 401");
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(401).body(response);
            }
            
            log.info("User authenticated: {}", authentication.getName());

            // 2. 이미 설문 완료한 경우
            Boolean isCompleted = (Boolean) session.getAttribute("questionnaireCompleted");
            if (Boolean.TRUE.equals(isCompleted)) {
                log.warn("Questionnaire already completed, returning 400");
                response.put("success", false);
                response.put("message", "이미 설문을 완료하셨습니다.");
                return ResponseEntity.badRequest().body(response);
            }

            // 3. 유효성 검사
            if (bindingResult.hasErrors()) {
                log.warn("=== Validation Errors ===");
                log.warn("Error count: {}", bindingResult.getErrorCount());
                
                Map<String, String> errors = new HashMap<>();
                bindingResult.getFieldErrors().forEach(error -> {
                    errors.put(error.getField(), error.getDefaultMessage());
                    log.warn("Field error - Field: {}, Rejected Value: {}, Message: {}", 
                        error.getField(),
                        error.getRejectedValue(),
                        error.getDefaultMessage());
                });
                
                response.put("success", false);
                response.put("message", "유효성 검사 실패");
                response.put("errors", errors);
                return ResponseEntity.badRequest().body(response);
            }

            log.info("Saving answers for user: {}", authentication.getName());
            
            // Set memberId from the authenticated user
            member = memberService.findByEmail(authentication.getName());
            if (member == null) {
                log.error("Member not found with email: {}", authentication.getName());
                response.put("success", false);
                response.put("message", "회원 정보를 찾을 수 없습니다. 다시 로그인해주세요.");
                return ResponseEntity.status(401).body(response);
            }
            answers.setMemberId(member.getId());
            
            log.info("Answer data before save: {}", answers);
            
            // Log each field for debugging
            log.info("Member ID: {}", answers.getMemberId());
            log.info("Hobbies: {}", answers.getHobbies());
            log.info("MBTI: {}", answers.getMbti());
            log.info("Travel Styles: {}", answers.getTravelStyles());
            log.info("Companions: {}", answers.getCompanions());
            log.info("Food Preferences: {}", answers.getFoodPreferences());
            
            // 4. 답변 저장 (트랜잭션 내에서 처리)
            log.info("=== BEFORE SAVE ANSWERS ===");
            log.info("Member ID: {}", member.getId());
            log.info("MBTI value: {}", answers.getMbti());
                
            // 5. Save answers and update status
            try {
                saveAnswers(member.getId(), answers);
                memberService.updateQuestionnaireStatus(member.getId(), true);
                log.info("Answers saved and status updated successfully");
                
                response.put("success", true);
                response.put("message", "설문이 성공적으로 제출되었습니다.");
                return ResponseEntity.ok(response);
                
            } catch (Exception e) {
                log.error("Error saving answers: {}", e.getMessage(), e);
                response.put("success", false);
                response.put("message", "답변 저장 중 오류가 발생했습니다: " + e.getMessage());
                return ResponseEntity.status(500).body(response);
            }
            // The above code is unreachable due to the return statement in the try-catch block
            
        } catch (Exception e) {
            log.error("=== CRITICAL ERROR IN FORM SUBMISSION ===");
            log.error("Error details:", e);
            
            // Log the complete error hierarchy
            log.error("Error class: {}", e.getClass().getName());
            log.error("Error message: {}", e.getMessage());
            
            // Log the root cause
            Throwable cause = e.getCause();
            int level = 0;
            while (cause != null && level < 5) { // Prevent infinite loops
                log.error("Caused by (level {}): {}: {}", level, cause.getClass().getName(), cause.getMessage());
                cause = cause.getCause();
                level++;
            }
            
            // Log the form data that caused the error
            try {
                ObjectMapper mapper = new ObjectMapper();
                log.error("Form data: {}", mapper.writeValueAsString(answers));
            } catch (Exception jsonError) {
                log.error("Could not serialize form data: {}", jsonError.getMessage());
            }
            
            // Return error response
            response.put("success", false);
            response.put("message", "답변 저장 중 오류가 발생했습니다: " + 
                (e.getMessage() != null ? e.getMessage() : "알 수 없는 오류"));
                
            // Add debug information
            response.put("debug_error", true);
            response.put("error_details", 
                e.getMessage() != null ? e.getMessage() : "No error message provided");
                
            return ResponseEntity.status(500).body(response);
        }
    }

    private void saveAnswers(UUID memberId, QuestionAnswersDTO answers) {
        log.info("=== SAVE ANSWERS STARTED ===");
        log.info("Member ID: {}", memberId);
        log.info("MBTI value: {}", answers.getMbti());
        log.info("Hobbies: {}", answers.getHobbies());
        log.info("Travel Styles: {}", answers.getTravelStyles());
        log.info("Companions: {}", answers.getCompanions());
        log.info("Food Preferences: {}", answers.getFoodPreferences());
        
        try {
            // 멤버 존재 여부 확인
            log.info("Looking up member with ID: {}", memberId);
            MemberEntity member = memberService.findById(memberId);
            if (member == null) {
                log.error("Member not found with ID: {}", memberId);
                throw new RuntimeException("회원 정보를 찾을 수 없습니다. 로그인 상태를 확인해주세요.");
            }
            log.info("Found member: {} ({})", member.getEmail(), member.getId());

            // Save hobbies (info_high_num = 1)
            if (answers.getHobbies() != null && !answers.getHobbies().isEmpty()) {
                log.info("Saving {} hobbies", answers.getHobbies().size());
                for (Long infoLowNum : answers.getHobbies()) {
                    log.debug("Saving hobby with infoLowNum: {}", infoLowNum);
                    addInfoService.saveMemberAddInfo(memberId, 1, infoLowNum.intValue());
                }
            } else {
                log.warn("No hobbies provided for member ID: {}", memberId);
            }

            // Save MBTI (info_high_num = 2)
            if (answers.getMbti() != null && !answers.getMbti().isEmpty()) {
                String mbti = answers.getMbti().trim().toUpperCase(); // Ensure uppercase and trim whitespace
                log.info("=== MBTI DEBUG ===");
                log.info("Raw MBTI from form: '{}' (length: {})", mbti, mbti.length());
                log.info("MBTI value class: {}", mbti.getClass().getName());
                log.info("MBTI value bytes: {}", Arrays.toString(mbti.getBytes(StandardCharsets.UTF_8)));
                
                // Log all available MBTI values for debugging
                List<AddInfoDTO> availableMbtis = addInfoService.getAddInfoByType(2);
                log.info("Available MBTI values in DB:");
                availableMbtis.forEach(m -> log.info("- {} (ID: {})", m.getInfoName(), m.getInfoLowNum()));
                
                try {
                    // Find the MBTI in the database to get its infoLowNum
                    log.info("Looking up MBTI in database: {}", mbti);
                    AddInfoEntity mbtiEntity = addInfoService.getAddInfoByTypeAndName(2, mbti);
                    
                    if (mbtiEntity != null) {
                        log.info("Found MBTI entity: infoLowNum={}", mbtiEntity.getInfoLowNum());
                        log.info("Saving member add info - memberId: {}, infoHighNum: 2, infoLowNum: {}", 
                                memberId, mbtiEntity.getInfoLowNum());
                        
                        addInfoService.saveMemberAddInfo(memberId, 2, mbtiEntity.getInfoLowNum());
                        log.info("Successfully saved MBTI for member");
                    } else {
                        log.warn("MBTI value '{}' not found in the database. Available MBTI values in DB:", mbti);
                        // Log available MBTI values for debugging
                        List<AddInfoDTO> allMbtis = addInfoService.getAddInfoByType(2);
                        allMbtis.forEach(m -> log.info("Available MBTI: {}", m.getInfoName()));
                        
                        throw new RuntimeException("유효하지 않은 MBTI 값입니다: " + mbti);
                    }
                } catch (Exception e) {
                    log.error("Error saving MBTI: {}", e.getMessage(), e);
                    throw new RuntimeException("MBTI 저장 중 오류가 발생했습니다: " + e.getMessage(), e);
                }
            } else {
                log.warn("No MBTI provided for member ID: {}", memberId);
            }

            // Save travel styles (info_high_num = 3)
            if (answers.getTravelStyles() != null && !answers.getTravelStyles().isEmpty()) {
                log.info("Saving {} travel styles", answers.getTravelStyles().size());
                for (Long infoLowNum : answers.getTravelStyles()) {
                    log.debug("Saving travel style with infoLowNum: {}", infoLowNum);
                    addInfoService.saveMemberAddInfo(memberId, 3, infoLowNum.intValue());
                }
            } else {
                log.warn("No travel styles provided for member ID: {}", memberId);
            }

            // Save companions (info_high_num = 4)
            if (answers.getCompanions() != null && !answers.getCompanions().isEmpty()) {
                log.info("Saving {} companions", answers.getCompanions().size());
                for (Long infoLowNum : answers.getCompanions()) {
                    log.debug("Saving companion with infoLowNum: {}", infoLowNum);
                    addInfoService.saveMemberAddInfo(memberId, 4, infoLowNum.intValue());
                }
            } else {
                log.warn("No companions provided for member ID: {}", memberId);
            }

            // Save food preferences (info_high_num = 5)
            if (answers.getFoodPreferences() != null && !answers.getFoodPreferences().isEmpty()) {
                log.info("Saving {} food preferences", answers.getFoodPreferences().size());
                for (Long infoLowNum : answers.getFoodPreferences()) {
                    log.debug("Saving food preference with infoLowNum: {}", infoLowNum);
                    addInfoService.saveMemberAddInfo(memberId, 5, infoLowNum.intValue());
                }
            } else {
                log.warn("No food preferences provided for member ID: {}", memberId);
            }
        } catch (Exception e) {
            log.error("Error saving answers for member ID {}: {}", memberId, e.getMessage(), e);
            throw new RuntimeException("Failed to save answers: " + e.getMessage(), e);
        }
    }
    @GetMapping(PathConstants.QUESTION_COMPLETE)
    public String showCompletionPage(HttpSession session) {
        // 세션에서 필요한 정보만 확인
        Boolean isCompleted = (Boolean) session.getAttribute("questionnaireCompleted");
        if (session.getAttribute("userId") == null || !Boolean.TRUE.equals(isCompleted)) {
            return "redirect:/";
        }
        return "question/complete";
    }
}
