package com.koreatravel.tabitomo.exception;

import com.koreatravel.tabitomo.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

import java.util.Objects;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    // 404 에러 처리
    @ExceptionHandler(NoHandlerFoundException.class)
    public String handleNoHandlerFound(NoHandlerFoundException e, HttpServletRequest request) {
        log.warn("No handler found for {} {}", request.getMethod(), request.getRequestURI());
        return "redirect:/error?status=404&message=" + "요청하신 페이지를 찾을 수 없습니다.";
    }

    // 405 에러 처리
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public String handleMethodNotSupported(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        log.warn("Method not supported: {} {}", request.getMethod(), request.getRequestURI());
        return "redirect:/error?status=405&message=" + "지원하지 않는 요청 방식입니다.";
    }

    // 필수 파라미터 누락 에러 처리
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public String handleMissingParams(MissingServletRequestParameterException e) {
        String paramName = e.getParameterName();
        log.warn("Missing request parameter: {}", paramName);
        return "redirect:/error?status=400&message=필수 파라미터가 누락되었습니다: " + paramName;
    }

    // 일반 예외 처리
    @ExceptionHandler(Exception.class)
    public Object handleException(Exception e, HttpServletRequest request, Model model) {
        log.error("Unexpected error occurred: {}", e.getMessage(), e);
        
        // AJAX 요청인 경우 JSON 응답 반환
        if (isAjaxRequest(request)) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of("SERVER_ERROR", "서버에서 오류가 발생했습니다: " + e.getMessage()));
        }
        
        // 일반 요청인 경우 에러 페이지로 리다이렉트
        String errorMessage = e.getMessage() != null ? e.getMessage() : "알 수 없는 오류가 발생했습니다.";
        return "redirect:/error?status=500&message=" + errorMessage;
    }

    // 유효성 검사 예외 처리
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public Object handleValidationException(Exception e, HttpServletRequest request) {
        String errorMessage = "유효하지 않은 입력값이 있습니다.";
        
        if (e instanceof MethodArgumentNotValidException ex) {
            errorMessage = ex.getBindingResult().getFieldError() != null ?
                    ex.getBindingResult().getFieldError().getDefaultMessage() :
                    "유효성 검사에 실패했습니다.";
        } else if (e instanceof BindException ex) {
            errorMessage = ex.getBindingResult().getFieldError() != null ?
                    ex.getBindingResult().getFieldError().getDefaultMessage() :
                    "바인딩 오류가 발생했습니다.";
        }
        
        log.warn("Validation error: {}", errorMessage);
        
        if (isAjaxRequest(request)) {
            return ResponseEntity
                    .badRequest()
                    .body(ErrorResponse.of("VALIDATION_ERROR", errorMessage));
        }
        
        return "redirect:/error?status=400&message=" + errorMessage;
    }
    
    // 제약 조건 위반 예외 처리
    @ExceptionHandler(ConstraintViolationException.class)
    public Object handleConstraintViolation(ConstraintViolationException e, HttpServletRequest request) {
        log.error("Constraint violation: {}", e.getMessage());
        
        if (isAjaxRequest(request)) {
            return ResponseEntity
                    .badRequest()
                    .body(ErrorResponse.of("CONSTRAINT_VIOLATION", e.getMessage()));
        }
        
        return "redirect:/error?status=400&message=" + e.getMessage();
    }
    
    // 커스텀 비즈니스 예외 처리
    @ExceptionHandler(BusinessException.class)
    public Object handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.error("Business error: {}", e.getMessage(), e);
        
        if (isAjaxRequest(request)) {
            return ResponseEntity
                    .status(e.getStatus())
                    .body(ErrorResponse.of(e.getErrorCode(), e.getMessage()));
        }
        
        return "redirect:/error?status=" + e.getStatus().value() + "&code=" + e.getErrorCode() + "&message=" + e.getMessage();
    }
    
    // 에러 페이지 매핑
    @org.springframework.web.bind.annotation.GetMapping("/error")
    public String errorPage(HttpServletRequest request, Model model) {
        String status = request.getParameter("status");
        String code = request.getParameter("code");
        String message = request.getParameter("message");
        
        if (status == null) status = "500";
        if (message == null) message = "알 수 없는 오류가 발생했습니다.";
        
        model.addAttribute("status", status);
        model.addAttribute("code", code != null ? code : "");
        model.addAttribute("message", message);
        
        return "error";
    }
    
    // AJAX 요청 여부 확인
    private boolean isAjaxRequest(HttpServletRequest request) {
        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With")) ||
               "application/json".equals(request.getHeader("Content-Type")) ||
               (request.getHeader("Accept") != null && 
                request.getHeader("Accept").contains("application/json"));
    }
}
