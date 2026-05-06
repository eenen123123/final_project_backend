package kr.or.ddit.finalProject.controller.example;

import org.springframework.stereotype.Controller;
import kr.or.ddit.finalProject.service.example.ExampleService;
import kr.or.ddit.finalProject.service.example.ExampleServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequiredArgsConstructor
public class ExampleController {
    private final ExampleServiceImpl exampleService;

    @GetMapping("/test")
    public String getMethodName() {
        String exampleDate = exampleService.getExampleDate();
        System.out.println("Example date: " + exampleDate);
        log.info("Example date: {}", exampleDate);
        return exampleDate;
    }

}
