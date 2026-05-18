package kr.or.ddit.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import kr.or.ddit.finalProject.exception.FinalProjectException;

@ControllerAdvice
public class AdminExceptionHandler {
    @ExceptionHandler(FinalProjectException.class)
    public String handle(FinalProjectException ex, RedirectAttributes model) {
        model.addFlashAttribute("status", ex.getErrorCode().getMessage());
        model.addFlashAttribute("errorMessage", ex.getErrorCode().getMessage());
        return "redirect:/admin/error";
    }
}
