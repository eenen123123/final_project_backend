package kr.or.ddit.controller.member;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import kr.or.ddit.finalProject.dto.email.EmailVerificationRequest;
import kr.or.ddit.finalProject.dto.member.MemberDto;
import kr.or.ddit.finalProject.dto.user.SignupRequestRecord;
import kr.or.ddit.finalProject.exception.ErrorCode;
import kr.or.ddit.finalProject.exception.FinalProjectException;
import kr.or.ddit.finalProject.service.email.EmailService;
import kr.or.ddit.finalProject.service.member.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;



@Slf4j
@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class RestMemberController {
    private final MemberService memberService;
    private final EmailService emailService;

    @PostMapping("/signup")
    public ResponseEntity<Boolean> signup(@Valid
    @RequestBody
    SignupRequestRecord signupRequestRecord) {
        // log.info("회원 가입 요청: {}", signupRequestRecord);

        MemberDto memberDto = memberService.signup(signupRequestRecord);

        if (memberDto == null) {
            throw new FinalProjectException(ErrorCode.SIGNUP_FAILED);
        }

        return ResponseEntity.ok(true);
    }

    @PostMapping("/email-code")
    public ResponseEntity<String> sendEmailCode(@RequestBody
    EmailVerificationRequest request) {
        log.info("이메일 인증 코드 요청: {}", request.emailAddr());

        emailService.sendEmailSixDigits(request.emailAddr());

        return ResponseEntity.ok("Email sent successfully");
    }

    @PostMapping("/email-code/verify")
    public ResponseEntity<Boolean> verifyEmailCode(@RequestBody
    EmailVerificationRequest request) {
        boolean isValid =
                emailService.checkEmailVerification(request.emailAddr(), request.emailCode());
        return ResponseEntity.ok(isValid);
    }

    @GetMapping("/check-userid")
    public ResponseEntity<Boolean> checkId(@RequestParam
    String userId) {
        boolean isAvailable = memberService.isUserIdAvailable(userId);
        return ResponseEntity.ok(isAvailable);
    }



}
