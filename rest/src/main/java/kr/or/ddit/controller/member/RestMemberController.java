package kr.or.ddit.controller.member;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import kr.or.ddit.finalProject.dto.user.SignupRequestRecord;
import kr.or.ddit.finalProject.service.email.EmailService;
import kr.or.ddit.finalProject.service.member.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@Slf4j
@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class RestMemberController {
    private final MemberService memberService;
    private final EmailService emailService;

    @PostMapping("/signup")
    public String signup(@RequestBody
    SignupRequestRecord signupRequestRecord) {
        log.info("회원 가입 요청: {}", signupRequestRecord);

        return null;
    }

    @PostMapping("/email-code")
    public ResponseEntity<String> sendEmailCode(@RequestBody
    String emailAddr) {

        String result = emailService.sendEmailSixDigits(emailAddr);

        return ResponseEntity.ok(result);
    }


}
