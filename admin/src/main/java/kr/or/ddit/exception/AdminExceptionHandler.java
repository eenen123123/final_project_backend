package kr.or.ddit.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import kr.or.ddit.finalProject.exception.FinalProjectException;

@ControllerAdvice
public class AdminExceptionHandler {
    @ExceptionHandler(FinalProjectException.class)
    public String handle(FinalProjectException ex, Model model) {
        model.addAttribute("status", ex.getErrorCode().getMessage());
        model.addAttribute("message", ex.getErrorCode().getMessage());
        return "error/error";
    }
}
