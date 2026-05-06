package kr.or.ddit.controller;

import org.springframework.web.bind.annotation.RestController;

import kr.or.ddit.finalProject.dto.example.ExampleDto;
import kr.or.ddit.finalProject.mapper.TestMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api")
public class ExampleRestController {

    @Autowired
    private TestMapper testMapper;

    @GetMapping("/hello")
    public ExampleDto getMethodName() {
        ExampleDto exampleDto = testMapper.getDate();
        return exampleDto;
    }
}
