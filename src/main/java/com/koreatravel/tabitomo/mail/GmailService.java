package com.koreatravel.tabitomo.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.UnsupportedEncodingException;

@Slf4j
@Service
@RequiredArgsConstructor
public class GmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public void sendVerificationEmail(String toEmail, String verificationCode) {
        String subject = "[tabitomo] 이메일 인증 코드";

        try {
            // Create the Thymeleaf context and add variables
            Context context = new Context();
            context.setVariable("verificationCode", verificationCode);
            
            // Process the email template
            String emailContent = templateEngine.process("email-verification", context);
            
            // Send the email
            sendEmail(toEmail, subject, emailContent, true);
        } catch (Exception e) {
            log.error("이메일 전송 중 오류 발생: {}", toEmail, e);
            throw new RuntimeException("이메일 전송에 실패했습니다.", e);
        }
    }

    public void sendPasswordResetEmail(String toEmail, String tempPassword) {
        String subject = "[tabitomo] 임시 비밀번호 발급";
        
        try {
            // Create the Thymeleaf context and add variables
            Context context = new Context();
            context.setVariable("tempPassword", tempPassword);
            
            // Process the email template
            String emailContent = templateEngine.process("password-reset-email", context);
            
            // Send the email
            sendEmail(toEmail, subject, emailContent, true);
        } catch (Exception e) {
            log.error("비밀번호 재설정 이메일 전송 중 오류 발생: {}", toEmail, e);
            throw new RuntimeException("비밀번호 재설정 이메일 전송에 실패했습니다.", e);
        }
    }
    
    private void sendEmail(String to, String subject, String content, boolean isHtml) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom("noreply@tabitomo.com", "Tabitomo");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, isHtml);
            
            mailSender.send(message);
            log.info("이메일 전송 성공: {}", to);
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("이메일 전송 실패: {}", to, e);
            throw new RuntimeException("이메일 전송에 실패했습니다.", e);
        }
    }
}