package kr.or.ddit.controller.example;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ExampleHtmlController {

    @GetMapping("/vod")
    public String example1(Model model) {
        model.addAttribute("pageTitle", "example1");
        return "admin:/vod_management";
    }

}
