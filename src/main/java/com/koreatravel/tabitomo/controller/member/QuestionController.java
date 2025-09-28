package com.koreatravel.tabitomo.controller.member;

import com.koreatravel.tabitomo.PathConstants;
import com.koreatravel.tabitomo.config.security.UserDetailsImpl;
import com.koreatravel.tabitomo.domain.dto.member.AddInfoDTO;
import com.koreatravel.tabitomo.domain.dto.member.QuestionAnswersDTO;
import com.koreatravel.tabitomo.exception.BusinessException;
import com.koreatravel.tabitomo.repository.member.MemberAddInfoRepository;
import com.koreatravel.tabitomo.service.member.AddInfoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
public class QuestionController {

    private final AddInfoService addInfoService;
    private final MemberAddInfoRepository memberAddInfoRepository;

    @GetMapping(PathConstants.QUESTION_FORM)
    public String showQuestionForm(Authentication authentication, Model model, HttpServletRequest request) {
        log.info("Accessing question form page");
        
        // Check authentication
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("User not authenticated, redirecting to login");
            
            // Save the requested URL for redirecting after login
            String referer = request.getHeader("Referer");
            String redirectUrl = referer != null ? referer : "/";
            
            // Add a message to be displayed after login
            request.getSession().setAttribute("redirectUrl", redirectUrl);
            request.getSession().setAttribute("loginMessage", "설문 작성을 위해 로그인이 필요합니다.");
            
            return "redirect:/auth/login";
        }
        
        try {
            // Get the authenticated user's ID
            Object principal = authentication.getPrincipal();
            if (!(principal instanceof UserDetailsImpl)) {
                log.error("Unexpected principal type: {}", principal != null ? principal.getClass().getName() : "null");
                model.addAttribute("error", "잘못된 사용자 정보입니다.");
                return "error";
            }
            
            UserDetailsImpl userDetails = (UserDetailsImpl) principal;
            if (userDetails == null || userDetails.getId() == null) {
                log.error("사용자 정보가 올바르지 않습니다.");
                model.addAttribute("error", "사용자 정보를 찾을 수 없습니다.");
                return "error";
            }
            
            UUID memberId = userDetails.getId();
            log.info("User {} accessing question form (ID: {})", userDetails.getUsername(), memberId);
            
            // Add memberId to the model for the form
            model.addAttribute("memberId", memberId);
            
            // Check if the user has already submitted the form
            boolean hasSubmitted = memberAddInfoRepository.existsByMemberId(memberId);
            if (hasSubmitted) {
                log.info("User has already submitted the form, redirecting to start page");
                model.addAttribute("message", "이미 설문을 완료하셨습니다.");
                return "redirect:" + PathConstants.QUESTION_START;
            }
            
            // Fetch questions by category using the service layer
            // Categories 1 to 5 represent different question categories
            List<Integer> categories = List.of(1, 2, 3, 4, 5);
            Map<Integer, List<AddInfoDTO>> questionsByCategory = 
                addInfoService.getQuestionsByCategories(categories);
            
            // Add questions to the model
            model.addAttribute("questionsByCategory", questionsByCategory);
            
            return "question/form";
            
        } catch (Exception e) {
            log.error("Error in showQuestionForm: {}", e.getMessage(), e);
            model.addAttribute("error", "설문 폼을 불러오는 중 오류가 발생했습니다.");
            return "error";
        }
    }
    
    @GetMapping(PathConstants.QUESTION_START)
    public String showStartPage(HttpSession session, Authentication authentication, Model model) {
        log.info("Accessing question start page");
        
        try {
            if (authentication == null) {
                log.warn("Authentication object is null");
                model.addAttribute("error", "인증 정보가 없습니다. 로그인 해주세요.");
                return "redirect:/auth/login?error=no_auth";
            }
            
            if (!authentication.isAuthenticated()) {
                log.warn("User not authenticated");
                return "redirect:/auth/login?error=not_authenticated";
            }
            
            Object principal = authentication.getPrincipal();
            if (!(principal instanceof UserDetailsImpl)) {
                String principalType = principal != null ? principal.getClass().getName() : "null";
                log.warn("Unexpected principal type: {}", principalType);
                model.addAttribute("error", "잘못된 사용자 정보입니다. (Type: " + principalType + ")");
                return "redirect:/error";
            }

            UserDetailsImpl userDetails = (UserDetailsImpl) principal;
            UUID memberId = userDetails.getId();
            log.info("User authenticated: {} (ID: {})", userDetails.getUsername(), memberId);
            
            boolean hasCompletedQuestionnaire = memberAddInfoRepository.existsByMemberId(memberId);
            if (hasCompletedQuestionnaire) {
                log.info("User {} has already completed the questionnaire", userDetails.getUsername());
                return "redirect:/";
            }
            
            session.setAttribute("userId", memberId);
            return "question/start";
            
        } catch (Exception e) {
            log.error("Error in showStartPage: {}", e.getMessage(), e);
            model.addAttribute("error", "시작 페이지를 불러오는 중 오류가 발생했습니다.");
            return "error";
        }
    }
    
    @PostMapping("/api" + PathConstants.QUESTION_SUBMIT)
    @ResponseBody
    @Transactional(rollbackFor = Exception.class, noRollbackFor = {BusinessException.class})
    public Object submitAnswers(
            @RequestBody(required = false) Map<String, Object> requestBody,
            @ModelAttribute(value = "questionAnswers", binding = false) QuestionAnswersDTO formAnswers,
            BindingResult bindingResult,
            HttpSession session,
            Authentication authentication,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        
        boolean isAjax = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
        log.info("=== 설문 응답 제출 시작 (AJAX: {}) ===", isAjax);
        
        try {
            // 인증 확인
            if (authentication == null || !authentication.isAuthenticated() || 
                !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
                log.warn("인증되지 않은 사용자 요청");
                if (isAjax) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", false);
                    response.put("message", "로그인이 필요합니다.");
                    response.put("redirect", "/auth/login");
                    return ResponseEntity.status(401).body(response);
                }
                return "redirect:/auth/login";
            }
            
            // Log authentication details
            log.info("Authentication details - Principal class: {}", 
                authentication.getPrincipal() != null ? 
                authentication.getPrincipal().getClass().getName() : "null");
            
            // Get authenticated user details
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            log.info("UserDetails: {}, User ID: {}", 
                userDetails,
                userDetails != null ? userDetails.getId() : "null");
            
            if (userDetails == null || userDetails.getId() == null) {
                log.error("사용자 정보를 가져올 수 없습니다. 인증 정보: {}", authentication);
                log.error("UserDetails: {}", userDetails);
                throw new BusinessException("사용자 정보를 가져올 수 없습니다. 다시 로그인해주세요.");
            }
            
            UUID memberId = userDetails.getId();
            log.info("Processing form submission for user ID: {}", memberId);
            
            // Initialize request data
            QuestionAnswersDTO requestData = new QuestionAnswersDTO();
            requestData.setMemberId(memberId);
            
            // Log the received data for debugging
            log.info("Received form data - hobbies: {}, mbti: {}, travelStyles: {}, companions: {}, foodPreferences: {}",
                requestBody != null ? requestBody.get("hobbies") : null,
                requestBody != null ? requestBody.get("mbti") : null,
                requestBody != null ? requestBody.get("travelStyles") : null,
                requestBody != null ? requestBody.get("companions") : null,
                requestBody != null ? requestBody.get("foodPreferences") : null);
            
            // Process request data based on content type
            if (isAjax && requestBody != null) {
                // Process AJAX request with JSON body
                log.info("Processing AJAX request with body: {}", requestBody);
                
                // Process hobbies
                if (requestBody.containsKey("hobbies")) {
                    try {
                        List<Long> hobbies = ((List<?>) requestBody.get("hobbies")).stream()
                            .map(obj -> Long.parseLong(String.valueOf(obj)))
                            .collect(Collectors.toList());
                        requestData.setHobbies(hobbies);
                    } catch (Exception e) {
                        log.error("Failed to parse hobbies", e);
                        throw new BusinessException("취미 정보를 처리하는 중 오류가 발생했습니다.");
                    }
                }
                
                // Process MBTI
                if (requestBody.containsKey("mbti")) {
                    requestData.setMbti(String.valueOf(requestBody.get("mbti")));
                }
                
                // Process travel styles
                if (requestBody.containsKey("travelStyles")) {
                    try {
                        List<Long> travelStyles = ((List<?>) requestBody.get("travelStyles")).stream()
                            .map(obj -> Long.parseLong(String.valueOf(obj)))
                            .collect(Collectors.toList());
                        requestData.setTravelStyles(travelStyles);
                    } catch (Exception e) {
                        log.error("Failed to parse travel styles", e);
                        throw new BusinessException("여행 스타일 정보를 처리하는 중 오류가 발생했습니다.");
                    }
                }
                
                // Process companions
                if (requestBody.containsKey("companions")) {
                    try {
                        List<Long> companions = ((List<?>) requestBody.get("companions")).stream()
                            .map(obj -> Long.parseLong(String.valueOf(obj)))
                            .collect(Collectors.toList());
                        requestData.setCompanions(companions);
                    } catch (Exception e) {
                        log.error("Failed to parse companions", e);
                        throw new BusinessException("동반자 정보를 처리하는 중 오류가 발생했습니다.");
                    }
                }
                
                // Process food preferences if available
                if (requestBody.containsKey("foodPreferences")) {
                    try {
                        List<Long> foodPreferences = ((List<?>) requestBody.get("foodPreferences")).stream()
                            .map(obj -> Long.parseLong(String.valueOf(obj)))
                            .collect(Collectors.toList());
                        requestData.setFoodPreferences(foodPreferences);
                    } catch (Exception e) {
                        log.error("Failed to parse food preferences", e);
                        // Don't fail the whole request if food preferences parsing fails
                    }
                }
            } else if (formAnswers != null) {
                // Handle regular form submission
                log.info("Processing regular form submission");
                requestData = formAnswers;
                requestData.setMemberId(memberId);
            }
            
            // Validate required fields in the request data
            if (requestData.getHobbies() == null || requestData.getHobbies().isEmpty() ||
                requestData.getMbti() == null || requestData.getTravelStyles() == null || 
                requestData.getCompanions() == null || requestData.getFoodPreferences() == null ||
                requestData.getTravelStyles().isEmpty() || requestData.getCompanions().isEmpty() ||
                requestData.getFoodPreferences().isEmpty()) {
                
                log.warn("필수 데이터가 누락되었습니다. hobbies: {}, mbti: {}, travelStyles: {}, companions: {}, foodPreferences: {}",
                    requestData.getHobbies(), requestData.getMbti(), 
                    requestData.getTravelStyles(), requestData.getCompanions(), 
                    requestData.getFoodPreferences());
                    
                if (isAjax) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", false);
                    response.put("message", "모든 필수 항목을 선택해주세요.");
                    return ResponseEntity.badRequest().body(response);
                }
                redirectAttributes.addFlashAttribute("error", "모든 필수 항목을 선택해주세요.");
                return "redirect:" + PathConstants.QUESTION_START;
            }
            
            // Log the processed request data
            log.info("Processed request data for user {}: {}", memberId, requestData);
            
            if (bindingResult.hasErrors()) {
                log.warn("유효성 검사 실패: {}", bindingResult.getAllErrors());
                if (isAjax) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", false);
                    response.put("message", "유효성 검사에 실패했습니다.");
                    response.put("errors", bindingResult.getAllErrors());
                    return ResponseEntity.badRequest().body(response);
                }
                redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.questionAnswers", bindingResult);
                redirectAttributes.addFlashAttribute("questionAnswers", requestData);
                return "redirect:" + PathConstants.QUESTION_FORM;
            }
            
            log.info("설문 응답 저장 시작 - memberId: {}", memberId);
            addInfoService.saveAnswers(memberId, requestData);
            log.info("설문 응답 저장 완료");
            
            session.setAttribute("questionnaireCompleted", true);
            
            if (isAjax) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "설문이 성공적으로 제출되었습니다.");
                return ResponseEntity.ok(response);
            }
            
            return "redirect:/question/complete";
            
        } catch (BusinessException e) {
            log.error("비즈니스 예외 발생: {}", e.getMessage(), e);
            if (isAjax) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", e.getMessage());
                return ResponseEntity.badRequest().body(response);
            }
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/question/start";
            
        } catch (Exception e) {
            log.error("설문 제출 중 오류 발생: {}", e.getMessage(), e);
            String errorMessage = "설문 제출 중 오류가 발생했습니다: " + e.getMessage();
            if (e.getCause() != null) {
                errorMessage += " (원인: " + e.getCause().getMessage() + ")";
            }
            
            if (isAjax) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", errorMessage);
                response.put("errorType", e.getClass().getSimpleName());
                return ResponseEntity.status(500).body(response);
            }
            redirectAttributes.addFlashAttribute("error", errorMessage);
            return "redirect:/question/start";
        }
    }

    @GetMapping("/question/complete")
    public String showCompletionPage(HttpSession session) {
        Boolean isCompleted = (Boolean) session.getAttribute("questionnaireCompleted");
        if (isCompleted == null || !isCompleted) {
            return "redirect:/question/start";
        }
        return "question/complete";
    }
}