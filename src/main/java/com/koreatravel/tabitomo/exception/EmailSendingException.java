package com.koreatravel.tabitomo.exception;

/**
 * 이메일 전송 중 발생하는 예외를 나타내는 클래스입니다.
 */
public class EmailSendingException extends RuntimeException {
    
    /**
     * 기본 생성자입니다.
     *
     * @param message 예외 메시지
     */
    public EmailSendingException(String message) {
        super(message);
    }
    
    /**
     * 원인 예외를 포함하는 생성자입니다.
     *
     * @param message 예외 메시지
     * @param cause 원인 예외
     */
    public EmailSendingException(String message, Throwable cause) {
        super(message, cause);
    }
}
