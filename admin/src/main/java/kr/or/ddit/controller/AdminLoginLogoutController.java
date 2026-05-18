package kr.or.ddit.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;



@Controller
public class AdminLoginLogoutController {

    @GetMapping("/login")
    public String getLoginPage(Authentication authentication,
            RedirectAttributes redirectAttributes) {
        if (authentication != null && authentication.isAuthenticated()) {
            String message = "이미 로그인된 사용자입니다: " + authentication.getName();
            redirectAttributes.addFlashAttribute("message", message);
            return "redirect:/"; // 이미 로그인된 사용자는 /admin/hello로 리다이렉트
        }
        return "login";
    }



}
