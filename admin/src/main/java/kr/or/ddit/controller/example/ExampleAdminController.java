package kr.or.ddit.controller.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import kr.or.ddit.finalProject.dto.example.ExampleDto;
import kr.or.ddit.finalProject.mapper.TestMapper;

@Controller
public class ExampleAdminController {

    @Autowired
    private TestMapper testMapper;

    @GetMapping("/hello")
    public String hello(Model model) {
        ExampleDto exampleDto = testMapper.getDate();
        model.addAttribute("message", "Hello, the date from DB is: " + exampleDto.getExampleDate());
        return "hello";
    }

}
