package kr.or.ddit.controller.pay;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kr.or.ddit.finalProject.dto.pay.toss.TossPayRequest;
import kr.or.ddit.finalProject.dto.pay.toss.TossPaymentResponse;
import kr.or.ddit.finalProject.service.pay.TossPayConfirmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PayController {

    private final TossPayConfirmService tossPayConfirmService;

    // POST /api/payments/toss/confirm : 주문 검증 → 토스 승인 → 결제/주문 확정
    @PostMapping("/toss/confirm")
    public ResponseEntity<TossPaymentResponse> confirmTossPayment(Authentication authentication,
            @RequestBody TossPayRequest request) {
        log.info("Received Toss payment confirmation request: {}", request);
        return ResponseEntity
                .ok(tossPayConfirmService.confirmPayment(authentication.getName(), request));
    }
}
