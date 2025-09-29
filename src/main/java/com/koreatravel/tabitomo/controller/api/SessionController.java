package com.koreatravel.tabitomo.controller.api;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SessionController {

    @PostMapping("/update-session-prompt")
    public ResponseEntity<Map<String, Object>> updateSessionPrompt(
            @RequestBody Map<String, Object> requestBody,
            HttpSession session) {
        
        try {
            // Update session attributes based on the request
            if (requestBody.containsKey("showQuestionnairePrompt")) {
                boolean showPrompt = (boolean) requestBody.get("showQuestionnairePrompt");
                session.setAttribute("showQuestionnairePrompt", showPrompt);
                log.info("Updated showQuestionnairePrompt to: {}", showPrompt);
            }

            // Return the updated session data
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("showQuestionnairePrompt", session.getAttribute("showQuestionnairePrompt"));
            response.put("questionnaireCompleted", session.getAttribute("questionnaireCompleted"));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error updating session prompt status", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to update session: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
