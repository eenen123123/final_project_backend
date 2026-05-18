package kr.or.ddit.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/error")
public class ErrorPageController {

    @GetMapping
    public String handleError() {
        return "error/error";
    }
}
