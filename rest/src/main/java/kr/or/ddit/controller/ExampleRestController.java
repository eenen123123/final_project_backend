package kr.or.ddit.controller;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import kr.or.ddit.finalProject.dto.example.ExampleDto;
import kr.or.ddit.finalProject.dto.pay.kakao.KakaoPayApproveResponse;
import kr.or.ddit.finalProject.dto.pay.kakao.KakaoPayReadyRequest;
import kr.or.ddit.finalProject.dto.pay.kakao.KakaoPayReadyResponse;
import kr.or.ddit.finalProject.dto.pay.toss.TossPayRequest;
import kr.or.ddit.finalProject.dto.user.SignupRequestRecord;
import kr.or.ddit.finalProject.exception.ErrorCode;
import kr.or.ddit.finalProject.exception.user.UserException;
import kr.or.ddit.finalProject.mapper.TestMapper;
import kr.or.ddit.finalProject.service.email.EmailService;
import kr.or.ddit.finalProject.service.pay.KakaoPayService;
import kr.or.ddit.finalProject.service.pay.TossPayService;
import kr.or.ddit.finalProject.service.user.UserService;
import kr.or.ddit.finalProject.util.PrintPrettyObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ExampleRestController {

    private final UserService userService;

    private final TestMapper testMapper;

    private final EmailService emailService;

    private final KakaoPayService kakaoPayService;
    private final TossPayService tossPayService;



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


    // MARK: - Email Test

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
            return ResponseEntity.status(400)
                    .body(Map.of("message", "Invalid code. Please try again."));
        }
    }


    // MARK: - Kakao Pay Test

    @PostMapping("/test/kakao-pay")
    public ResponseEntity<KakaoPayReadyResponse> kakaoPayTest(
            @RequestBody KakaoPayReadyRequest formData, Authentication authentication) {

        log.info("Received Kakao Pay request: {}", formData);

        KakaoPayReadyResponse response = kakaoPayService.payReady(formData, authentication);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/test/kakao-pay/success/{uuid}")
    public ResponseEntity<KakaoPayApproveResponse> getMethodName(@PathVariable("uuid") String uuid,
            @RequestParam("pg_token") String pgToken) {

        KakaoPayApproveResponse response = kakaoPayService.approvePayment(pgToken, uuid);
        String prettyResponse = PrintPrettyObject.toPrettyString(response);
        log.info("Kakao Pay approval response: {}", prettyResponse);

        return ResponseEntity.ok(response);
    }


    @GetMapping("/test/kakao-pay/cancel/{uuid}")
    public ResponseEntity<String> cancelPayment(@PathVariable("uuid") String uuid) {
        kakaoPayService.cancelPayment(uuid);
        return ResponseEntity.ok("Payment cancelled");
    }

    @GetMapping("/test/kakao-pay/fail/{uuid}")
    public ResponseEntity<String> failPayment(@PathVariable("uuid") String uuid) {
        kakaoPayService.failPayment(uuid);
        return ResponseEntity.ok("Payment failed");
    }

    // MARK: - Toss Pay Test 


    @PostMapping("/payments/confirm")
    public ResponseEntity<?> confirmPayment(@RequestBody TossPayRequest request) {
        log.info("Received Toss Pay confirm request: {}, {}, {}", request.getAmount(),
                request.getOrderId(), request.getPaymentKey());

        return ResponseEntity.ok(tossPayService.confirm(request));
    }

}
