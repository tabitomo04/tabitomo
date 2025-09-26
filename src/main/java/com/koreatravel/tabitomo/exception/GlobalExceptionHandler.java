package com.koreatravel.tabitomo.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, Model model, RedirectAttributes redirectAttributes) {
        String errorMessage = "오류가 발생했습니다: " + e.getMessage();
        redirectAttributes.addFlashAttribute("error", errorMessage);
        return "redirect:/error";
    }
}
