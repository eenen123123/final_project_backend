package kr.or.ddit.controller.example;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ExampleHtmlController {

    @GetMapping("/vod")
    public String example1() {

        return "admin:/vod_management";
    }

}
