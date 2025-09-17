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

import javax.net.ssl.*;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

@Slf4j
@Service
@RequiredArgsConstructor
public class GmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    
    static {
        disableSslVerification();
    }
    
    private static void disableSslVerification() {
        try {
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
            };

            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            
            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = (hostname, session) -> true;
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
            
            log.info("SSL verification has been disabled for email sending");
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            log.error("Failed to disable SSL verification", e);
        }
    }

    public void sendVerificationEmail(String toEmail, String verificationCode) {
        String subject = "[tabitomo] 이메일 인증 코드";
        log.info("Preparing to send verification email to: {}", toEmail);

        try {
            // Create the Thymeleaf context and add variables
            Context context = new Context();
            context.setVariable("verificationCode", verificationCode);
            
            log.debug("Processing email template...");
            // Process the email template
            String emailContent = templateEngine.process("email-verification", context);
            log.debug("Email template processed successfully");
            
            // Send the email
            log.info("Sending verification email to: {}", toEmail);
            sendEmail(toEmail, subject, emailContent, true);
            log.info("Verification email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", toEmail, e);
            throw new RuntimeException("이메일 전송에 실패했습니다: " + e.getMessage(), e);
        }
    }

    public void sendPasswordResetEmail(String toEmail, String tempPassword) {
        String subject = "[tabitomo] 임시 비밀번호 발급";
        
        try {
            // Create the Thymeleaf context and add variables
            Context context = new Context();
            context.setVariable("tempPassword", tempPassword);
            
            log.debug("Processing email template...");
            // Process the email template
            String emailContent = templateEngine.process("password-reset-email", context);
            log.debug("Email template processed successfully");
            
            // Send the email
            log.info("Sending password reset email to: {}", toEmail);
            sendEmail(toEmail, subject, emailContent, true);
            log.info("Password reset email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", toEmail, e);
            throw new RuntimeException("비밀번호 재설정 이메일 전송에 실패했습니다: " + e.getMessage(), e);
        }
    }
    
    private void sendEmail(String to, String subject, String content, boolean isHtml) {
        log.debug("Creating MimeMessage...");
        MimeMessage message = mailSender.createMimeMessage();
        
        try {
            log.debug("Creating MimeMessageHelper...");
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            log.debug("Setting email properties...");
            helper.setFrom("noreply@tabitomo.com", "Tabitomo");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, isHtml);
            
            log.debug("Sending email to: {}", to);
            log.debug("Email content: {}", content);
            
            // Add some debug info
            log.info("MailSender class: {}", mailSender.getClass().getName());
            log.info("Protocol: {}", mailSender instanceof org.springframework.mail.javamail.JavaMailSenderImpl ? 
                ((org.springframework.mail.javamail.JavaMailSenderImpl)mailSender).getProtocol() : "unknown");
            
            mailSender.send(message);
            log.info("이메일 전송 성공: {}", to);
        } catch (MessagingException e) {
            log.error("이메일 메시지 생성 실패: {}", e.getMessage(), e);
            throw new RuntimeException("이메일 메시지 생성에 실패했습니다: " + e.getMessage(), e);
        } catch (UnsupportedEncodingException e) {
            log.error("인코딩 오류: {}", e.getMessage(), e);
            throw new RuntimeException("이메일 인코딩에 실패했습니다: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("이메일 전송 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
            // Add more detailed error information
            if (e.getCause() != null) {
                log.error("Root cause: {}", e.getCause().getMessage());
            }
            throw new RuntimeException("이메일 전송 중 오류가 발생했습니다: " + e.getMessage() + 
                (e.getCause() != null ? " (" + e.getCause().getMessage() + ")" : ""), e);
        }
    }
}