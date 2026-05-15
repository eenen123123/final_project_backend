package kr.or.ddit.controller;

import org.springframework.web.bind.annotation.RestController;

import kr.or.ddit.finalProject.dto.example.ExampleDto;
import kr.or.ddit.finalProject.dto.user.SignupRequestRecord;
import kr.or.ddit.finalProject.exception.ErrorCode;
import kr.or.ddit.finalProject.exception.user.UserException;
import kr.or.ddit.finalProject.mapper.TestMapper;
import kr.or.ddit.finalProject.service.email.EmailService;
import kr.or.ddit.finalProject.service.user.UserService;
import kr.or.ddit.finalProject.util.RandomSixDigits;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ExampleRestController {

    private final UserService userService;

    private final TestMapper testMapper;

    private final EmailService emailService;

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

    @GetMapping("/test/throw-error")
    public void throwError() {
        throw new UserException(ErrorCode.AUTHENTICATION_FAILED);
    }

    private String random = null;

    @GetMapping("/test/email")
    public ResponseEntity<Map<String, String>> getSixDigitString(@RequestParam String email) {
        log.info("Received request to send email to: {}", email);
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email is required."));
        }

        random = emailService.sendEmailSixDigits(email);

        return ResponseEntity.ok(Map.of("message", "6자리 코드를 전송했습니다."));
    }

    @PostMapping("/test/email")
    public ResponseEntity<Map<String, String>> postMethodName(@RequestParam String code) {
        if (random == null) {
            return ResponseEntity.status(400).body(Map.of("message", "No code generated yet."));
        }
        if (random.equals(code)) {
            random = null; // 인증 코드 초기화
            return ResponseEntity.ok(Map.of("message", "Code verified successfully!"));
        } else {
            return ResponseEntity.status(400).body(Map.of("message", "Invalid code. Please try again."));
        }

    }

}
