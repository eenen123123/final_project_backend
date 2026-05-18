package kr.or.ddit.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import kr.or.ddit.finalProject.dto.example.ExampleDto;
import kr.or.ddit.finalProject.dto.user.SignupRequestRecord;
import kr.or.ddit.finalProject.exception.ErrorCode;
import kr.or.ddit.finalProject.exception.user.UserException;
import kr.or.ddit.finalProject.mapper.TestMapper;
import kr.or.ddit.finalProject.service.email.EmailService;
import kr.or.ddit.finalProject.service.user.UserService;
import kr.or.ddit.finalProject.util.RandomSixDigits;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
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

    @Value("${kakao.pay.secret.key}")
    private String kakaoPaySecretKey;

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
            return ResponseEntity.status(400)
                    .body(Map.of("message", "Invalid code. Please try again."));
        }
    }



    @Setter
    @Getter
    static class KakaoPayRequestTest {
        String cid;
        String partner_order_id;
        String partner_user_id;
        String item_name;
        int quantity;
        int total_amount;
        int tax_free_amount;
        String approval_url;
        String cancel_url;
        String fail_url;
        String pg_token;
        String tid;
    }


    @Getter
    @NoArgsConstructor
    @ToString
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class) // JSON 필드 이름을 snake_case로 매핑
    static class KakaoPayResponseTest {
        String tid;
        String next_redirect_pc_url;
        String next_redirect_mobile_url;
        String android_app_scheme;
        String ios_app_scheme;
        String created_at;
    }

    private String tid = null;
    private int totalAmount = 0;

    @PostMapping("/test/kakao-pay")
    public ResponseEntity<KakaoPayResponseTest> kakaoPayTest(
            @RequestBody KakaoPayRequestTest formData) {

        log.info("Received Kakao Pay request: {}", formData);

        KakaoPayRequestTest kakaoPayRequest = new KakaoPayRequestTest();
        kakaoPayRequest.setCid("TC0ONETIME");
        kakaoPayRequest.setPartner_order_id("112333");
        kakaoPayRequest.setPartner_user_id("user123");
        kakaoPayRequest.setItem_name(formData.getItem_name());
        kakaoPayRequest.setQuantity(formData.getQuantity());
        kakaoPayRequest.setTotal_amount(formData.getTotal_amount());
        totalAmount = formData.getTotal_amount();
        kakaoPayRequest.setTax_free_amount(formData.getTotal_amount() / 10);
        kakaoPayRequest.setApproval_url("http://localhost:8081/api/test/kakao-pay/success");
        kakaoPayRequest.setCancel_url("http://localhost:8081/api/test/kakao-pay/cancel");
        kakaoPayRequest.setFail_url("http://localhost:8081/api/test/kakao-pay/fail");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "DEV_SECRET_KEY " + kakaoPaySecretKey);
        headers.set("Content-Type", "application/json");

        RestTemplate restTemplate = new RestTemplate();
        try {

            String url = "https://open-api.kakaopay.com/online/v1/payment/ready";
            HttpEntity<KakaoPayRequestTest> request = new HttpEntity<>(kakaoPayRequest, headers);
            ResponseEntity<KakaoPayResponseTest> response =
                    restTemplate.postForEntity(url, request, KakaoPayResponseTest.class);

            tid = response.getBody().getTid(); // 결제 준비 응답에서 tid 저장
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            log.error("Error during Kakao Pay request: {}", e.getMessage());
            return ResponseEntity.status(500).body(null);
        }

    }

    @GetMapping("/test/kakao-pay/success")
    public ResponseEntity<String> getMethodName(@RequestParam("pg_token") String pgToken) {
        log.info("Kakao Pay approval callback received with pg_token: {}", pgToken);
        KakaoPayRequestTest kakaoPayRequest = new KakaoPayRequestTest();
        kakaoPayRequest.setPg_token(pgToken);
        kakaoPayRequest.setCid("TC0ONETIME");
        kakaoPayRequest.setTid(tid);
        kakaoPayRequest.setPartner_order_id("112333");
        kakaoPayRequest.setPartner_user_id("user123");
        kakaoPayRequest.setTotal_amount(totalAmount);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "DEV_SECRET_KEY " + kakaoPaySecretKey);
        headers.set("Content-Type", "application/json");

        RestTemplate restTemplate = new RestTemplate();

        try {
            String url = "https://open-api.kakaopay.com/online/v1/payment/approve";
            HttpEntity<KakaoPayRequestTest> request = new HttpEntity<>(kakaoPayRequest, headers);
            ResponseEntity<String> response =
                    restTemplate.postForEntity(url, request, String.class);

            log.info("Kakao Pay approval response: {}", response.getBody());
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            log.error("Error during Kakao Pay approval: {}", e.getMessage());
            return ResponseEntity.status(500).body("Payment approval failed.");
        }

    }


}
