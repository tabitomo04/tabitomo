package com.koreatravel.tabitomo.controller;

import com.koreatravel.tabitomo.domain.dto.storybook.StorybookListDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import jakarta.servlet.http.HttpSession;
import com.koreatravel.tabitomo.service.trip.CitiesService;
import com.koreatravel.tabitomo.domain.dto.trip.CitiesDTO;
import org.springframework.ui.Model;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;
import com.koreatravel.tabitomo.service.storybook.EditorService;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import java.util.List;
import org.springframework.security.core.Authentication;
import com.koreatravel.tabitomo.domain.dto.member.MemberProfileDTO;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WebController {

    private final ResourceLoader resourceLoader;

    private final CitiesService citiesService;

    private final EditorService editorService;

    @GetMapping("/")
    public String index(Model model, HttpSession session, Authentication authentication) {
        log.info("===== Accessing root path =====");
        log.info("Session ID: {}", session.getId());
        log.info("Authentication: {}", authentication != null ? authentication.getName() : "anonymous");
        
        // Log all request attributes for debugging
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null && requestAttributes instanceof ServletRequestAttributes) {
            HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
            log.info("Request URI: {}", request.getRequestURI());
            log.info("Request URL: {}", request.getRequestURL());
            log.info("Requested Session ID: {}", request.getRequestedSessionId());
            log.info("Request is from valid session: {}", request.isRequestedSessionIdValid());
        }
        
        try {
            // Add cities and story list to the model
            List<CitiesDTO> cities = citiesService.getRecommendCities();
            model.addAttribute("cities", cities);
            
            List<StorybookListDTO> storybookList = editorService.getmainStory();
            model.addAttribute("storylist", storybookList);
            
            // Get member profile from session and check authentication
            MemberProfileDTO memberProfile = (MemberProfileDTO) session.getAttribute("memberProfile");
            boolean isAuthenticated = memberProfile != null;
            
            // Debug logging
            log.info("Session ID: {}", session.getId());
            log.info("Is authenticated: {}", isAuthenticated);
            
            if (isAuthenticated && memberProfile != null) {
                // Log user info
                log.info("User email: {}", memberProfile.getEmail());
                log.info("Questionnaire completed: {}", memberProfile.isQuestionnaireCompleted());
                
                // Add user info to model
                String nickname = memberProfile.getNickname() != null ? memberProfile.getNickname() : "";
                String email = memberProfile.getEmail() != null ? memberProfile.getEmail() : "";
                
                model.addAttribute("username", nickname);
                model.addAttribute("email", email);
                
                // Set questionnaire prompt based on member's completion status
                if (memberProfile.isQuestionnaireCompleted()) {
                    session.removeAttribute("showQuestionnairePrompt");
                    model.addAttribute("showQuestionnairePrompt", false);
                    model.addAttribute("questionnaireCompleted", true);
                } else {
                    // Check if we've already shown the prompt in this session
                    Boolean showPrompt = (Boolean) session.getAttribute("showQuestionnairePrompt");
                    if (showPrompt == null) {
                        // First time in this session, show the prompt
                        showPrompt = true;
                        session.setAttribute("showQuestionnairePrompt", true);
                    }
                    model.addAttribute("showQuestionnairePrompt", showPrompt);
                    model.addAttribute("questionnaireCompleted", false);
                    log.info("Show questionnaire prompt: {}", showPrompt);
                }
            } else {
                // Not authenticated, make sure no prompt is shown
                session.removeAttribute("showQuestionnairePrompt");
                model.addAttribute("showQuestionnairePrompt", false);
                model.addAttribute("questionnaireCompleted", false);
            }
            
            // Always add isAuthenticated to model
            model.addAttribute("isAuthenticated", isAuthenticated);
            model.addAttribute("sessionId", session.getId());
            
            // For debugging
            if (log.isDebugEnabled()) {
                log.debug("Session attributes:");
                session.getAttributeNames().asIterator()
                    .forEachRemaining(name -> 
                        log.debug("  {} = {}", name, session.getAttribute(name))
                    );
            }
            
            log.debug("Successfully prepared index page");
            log.debug("isAuthenticated: {}, showPrompt: {}", model.getAttribute("isAuthenticated"), model.getAttribute("showQuestionnairePrompt"));
            return "index";
            
        } catch (Exception e) {
            log.error("Error preparing index page", e);
            // In case of error, still return the page but with minimal processing
            return "index";
        }
    }
    

    @GetMapping("/menu")
    public String menu() {
        log.debug("Accessing menu page");
        return "menu";
    }

    @GetMapping("/chat")
    public String chat() {
        log.debug("Accessing chat page");
        return "chat";
    }
    
    @GetMapping(value = "/image/logo.png", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public ResponseEntity<Resource> getLogo() {
        Resource resource = resourceLoader.getResource("classpath:static/image/logo.png");
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache().mustRevalidate())
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=logo.png")
                .body(resource);
    }

}
