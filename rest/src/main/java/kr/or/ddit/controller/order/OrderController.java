package kr.or.ddit.controller.order;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kr.or.ddit.finalProject.dto.order.OrderDto;
import kr.or.ddit.finalProject.dto.order.OrderItemDto;
import kr.or.ddit.finalProject.service.order.OrderService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // POST /api/orders : 결제 전 주문 선생성(PENDING)
    // 응답의 ordId/totAmt를 토스 결제창 requestPayment(orderId, amount)에 사용
    @PostMapping
    public ResponseEntity<OrderDto> createOrder(Authentication authentication,
            @RequestBody List<OrderItemDto> items) {
        return ResponseEntity.ok(orderService.createOrder(authentication.getName(), items));
    }
}
