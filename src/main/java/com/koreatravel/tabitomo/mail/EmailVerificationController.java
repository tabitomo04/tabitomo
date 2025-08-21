package com.koreatravel.tabitomo.mail;

import lombok.RequiredArgsConstructor;

import java.util.HashMap;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.koreatravel.tabitomo.domain.dto.MemberDTO;

@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
public class EmailVerificationController {

    private final GmailService gmailService;

    @PostMapping("/send-verification")
    public ResponseEntity<?> sendVerificationEmail(@RequestBody MemberDTO member) {
        try {
            String verificationCode = String.format("%06d", (int) (Math.random() * 1000000));
            gmailService.sendVerificationEmail(member.getEmail(), verificationCode);

            // Return the verification code in the response (for testing)
            HashMap<String, String> response = new HashMap<>();
            response.put("message", "Verification email sent successfully");
            response.put("verificationCode", verificationCode); // For testing only

            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to send verification email: " + e.getMessage());
        }
    }
}
