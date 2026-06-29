package kr.or.ddit.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainPageController {

    @GetMapping("/")
    public String mainPage(Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }

        boolean isDirector = hasAuthority(authentication, "D400")
                || hasAuthority(authentication, "Z001");
        if (isDirector) {
            return "forward:/dashboard/principal";
        }

        boolean isAdmin = hasAuthority(authentication, "D100");
        if (isAdmin) {
            return "forward:/dashboard/staff";
        }

        boolean isInstructor = hasAuthority(authentication, "D300");
        if (isInstructor) {
            return "forward:/dashboard/instructor";
        }

        return "admin:/main";
    }

    private boolean hasAuthority(Authentication auth, String authority) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(authority));
    }
}
