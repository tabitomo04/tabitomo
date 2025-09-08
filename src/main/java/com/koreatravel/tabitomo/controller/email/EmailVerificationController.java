package com.koreatravel.tabitomo.controller.email;

import com.koreatravel.tabitomo.service.email.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
public class EmailVerificationController {

    private final EmailService emailService;

    @PostMapping("/send-verification")
    public ResponseEntity<?> sendVerificationEmail(@RequestBody Map<String, String> emailRequest) {
        try {
            String email = emailRequest.get("email");
            String verificationCode = String.format("%06d", (int) (Math.random() * 1000000));
            
            // Send verification email using the main email service
            boolean sent = emailService.sendVerificationEmail(email, verificationCode);
            
            if (sent) {
                // For testing purposes, include the verification code in the response
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Verification email sent successfully");
                response.put("verificationCode", verificationCode);
                
                return ResponseEntity.ok().body(response);
            } else {
                return ResponseEntity.badRequest().body("Failed to send verification email");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Failed to send verification email: " + e.getMessage());
        }
    }
}
