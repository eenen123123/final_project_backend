package kr.or.ddit.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import kr.or.ddit.finalProject.exception.FinalProjectException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
public class AdminExceptionHandler {

    @ExceptionHandler(FinalProjectException.class)
    public String handle(FinalProjectException ex, RedirectAttributes model) {
        model.addFlashAttribute("status", ex.getErrorCode().getStatus());
        model.addFlashAttribute("errorMessage", ex.getErrorCode().getMessage());
        log.error("ex : {}", ex);
        return "redirect:/admin/error";
    }
}
