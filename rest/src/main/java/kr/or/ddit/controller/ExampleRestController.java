package kr.or.ddit.controller;

import org.springframework.web.bind.annotation.RestController;

import kr.or.ddit.finalProject.dto.example.ExampleDto;
import kr.or.ddit.finalProject.dto.user.SignupRequestRecord;
import kr.or.ddit.finalProject.mapper.TestMapper;
import kr.or.ddit.finalProject.service.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@Slf4j
@RestController
@RequestMapping("/api")
public class ExampleRestController {

    @Autowired
    private UserService userService;

    @Autowired
    private TestMapper testMapper;

    @GetMapping("/hello")
    public ExampleDto getMethodName() {
        ExampleDto exampleDto = testMapper.getDate();
        return exampleDto;
    }

    @PostMapping("/test/signup")
    public ResponseEntity<String> signupReq(@RequestBody SignupRequestRecord requestRecord) {
        try {
            log.info("Signup request received: {}", requestRecord);
            userService.signup(requestRecord);
            log.info("Signup successful for: {}", requestRecord);
            return ResponseEntity.ok("Signup successful");
        } catch (Exception e) {
            log.error("Signup failed for: {}", requestRecord, e);
            return ResponseEntity.status(500).body("Signup failed");
        }
    }

}
