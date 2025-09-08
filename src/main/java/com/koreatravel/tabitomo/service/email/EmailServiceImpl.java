package com.koreatravel.tabitomo.service.email;

import com.koreatravel.tabitomo.exception.EmailSendingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * 이메일 전송을 담당하는 서비스 구현체입니다.
 * 비동기적으로 이메일을 전송하며, HTML 템플릿을 활용한 다양한 유형의 이메일을 지원합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private static final String EMAIL_SENDER_NAME = "Tabitomo";
    private static final String EMAIL_SENDER_ADDRESS = "noreply@tabitomo.com";
    
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    
    @Override
    @Async
    public boolean sendEmail(String to, String subject, String content) throws EmailSendingException {
        validateEmailParameters(to, subject, content);
        
        try {
            MimeMessage message = createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(EMAIL_SENDER_ADDRESS, EMAIL_SENDER_NAME);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, false);
            
            mailSender.send(message);
            log.info("이메일 전송 성공 - 수신자: {}, 제목: {}", to, subject);
            return true;
            
        } catch (Exception e) {
            String errorMessage = String.format("이메일 전송 실패 - 수신자: %s, 사유: %s", to, e.getMessage());
            log.error(errorMessage, e);
            throw new EmailSendingException(errorMessage, e);
        }
    }
    
    @Override
    @Async
    public boolean sendHtmlEmail(String to, String subject, String htmlContent) throws EmailSendingException {
        validateEmailParameters(to, subject, htmlContent);
        
        try {
            MimeMessage message = createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(EMAIL_SENDER_ADDRESS, EMAIL_SENDER_NAME);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("HTML 이메일 전송 성공 - 수신자: {}, 제목: {}", to, subject);
            return true;
            
        } catch (Exception e) {
            String errorMessage = String.format("HTML 이메일 전송 실패 - 수신자: %s, 사유: %s", to, e.getMessage());
            log.error(errorMessage, e);
            throw new EmailSendingException(errorMessage, e);
        }
    }
    
    @Override
    @Async
    public boolean sendVerificationEmail(String to, String verificationToken) throws EmailSendingException {
        if (verificationToken == null || verificationToken.trim().isEmpty()) {
            throw new IllegalArgumentException("인증 토큰이 유효하지 않습니다.");
        }
        
        try {
            String verificationUrl = "/api/auth/verify-email?token=" + verificationToken;
            
            Context context = new Context();
            context.setVariable("verificationUrl", verificationUrl);
            
            String emailContent = templateEngine.process("emails/verify-email", context);
            return sendHtmlEmail(to, "[Tabitomo] 이메일 인증을 완료해주세요", emailContent);
            
        } catch (Exception e) {
            String errorMessage = String.format("이메일 인증 메일 전송 중 오류 발생 - 수신자: %s, 사유: %s", to, e.getMessage());
            log.error(errorMessage, e);
            throw new EmailSendingException(errorMessage, e);
        }
    }
    
    @Override
    @Async
    public boolean sendPasswordResetEmail(String to, String tempPassword) throws EmailSendingException {
        if (tempPassword == null || tempPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("임시 비밀번호가 유효하지 않습니다.");
        }
        
        try {
            Context context = new Context();
            context.setVariable("tempPassword", tempPassword);
            
            String emailContent = templateEngine.process("emails/password-reset-email", context);
            return sendHtmlEmail(to, "[Tabitomo] 비밀번호 재설정", emailContent);
            
        } catch (Exception e) {
            String errorMessage = String.format("비밀번호 재설정 이메일 전송 중 오류 발생 - 수신자: %s, 사유: %s", to, e.getMessage());
            log.error(errorMessage, e);
            throw new EmailSendingException(errorMessage, e);
        }
    }
    
    /**
     * 이메일 파라미터 유효성 검사를 수행합니다.
     *
     * @param to      수신자 이메일 주소
     * @param subject 이메일 제목
     * @param content 이메일 내용
     * @throws IllegalArgumentException 파라미터가 유효하지 않은 경우
     */
    private void validateEmailParameters(String to, String subject, String content) {
        if (to == null || to.trim().isEmpty()) {
            throw new IllegalArgumentException("수신자 이메일 주소는 필수입니다.");
        }
        if (subject == null || subject.trim().isEmpty()) {
            throw new IllegalArgumentException("이메일 제목은 필수입니다.");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("이메일 내용은 필수입니다.");
        }
    }
    
    /**
     * MimeMessage 인스턴스를 생성합니다.
     *
     * @return 새로운 MimeMessage 인스턴스
     */
    private MimeMessage createMimeMessage() {
        return mailSender.createMimeMessage();
    }
}
