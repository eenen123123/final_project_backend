package kr.or.ddit.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@Controller
public class NoNoErrorController {
    @GetMapping("/.well-known/**")
    public String getMethodName() {
        return "redirect:/";
    }
    
}
