package com.koreatravel.tabitomo.service.email;

import com.koreatravel.tabitomo.exception.EmailSendingException;

/**
 * 이메일 전송을 담당하는 서비스 인터페이스입니다.
 * 이 인터페이스는 다양한 유형의 이메일을 전송하기 위한 메서드를 정의합니다.
 */
public interface EmailService {
    
    /**
     * 일반 텍스트 형식의 이메일을 전송합니다.
     *
     * @param to      수신자 이메일 주소
     * @param subject 이메일 제목
     * @param content 이메일 내용 (일반 텍스트)
     * @return 이메일 전송 성공 여부
     * @throws EmailSendingException 이메일 전송 중 오류가 발생한 경우
     */
    boolean sendEmail(String to, String subject, String content) throws EmailSendingException;
    
    /**
     * HTML 형식의 이메일을 전송합니다.
     *
     * @param to          수신자 이메일 주소
     * @param subject     이메일 제목
     * @param htmlContent HTML 형식의 이메일 내용
     * @return 이메일 전송 성공 여부
     * @throws EmailSendingException 이메일 전송 중 오류가 발생한 경우
     */
    boolean sendHtmlEmail(String to, String subject, String htmlContent) throws EmailSendingException;
    
    /**
     * 이메일 인증 링크를 포함한 인증 이메일을 전송합니다.
     *
     * @param to                수신자 이메일 주소
     * @param verificationToken 이메일 인증 토큰
     * @return 이메일 전송 성공 여부
     * @throws EmailSendingException 이메일 전송 중 오류가 발생한 경우
     */
    boolean sendVerificationEmail(String to, String verificationToken) throws EmailSendingException;
    
    /**
     * 비밀번호 재설정 이메일을 전송합니다.
     *
     * @param to           수신자 이메일 주소
     * @param tempPassword 임시 비밀번호
     * @return 이메일 전송 성공 여부
     * @throws EmailSendingException 이메일 전송 중 오류가 발생한 경우
     */
    boolean sendPasswordResetEmail(String to, String tempPassword) throws EmailSendingException;
}