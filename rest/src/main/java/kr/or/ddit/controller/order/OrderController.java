package kr.or.ddit.controller.order;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.dto.order.OrderCreateRequest;
import kr.or.ddit.finalProject.dto.order.OrderDto;
import kr.or.ddit.finalProject.exception.ErrorCode;
import kr.or.ddit.finalProject.exception.FinalProjectException;
import kr.or.ddit.finalProject.service.order.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // POST /api/orders : 결제 전 주문 선생성(PENDING)
    // 응답의 ordId/totAmt를 토스 결제창 requestPayment(orderId, amount)에 사용
    // [포인트 시스템] 요청 바디를 OrderCreateRequest로 변경 (items + pointAmt + pointType 포함)
    @PostMapping
    public ResponseEntity<OrderDto> createOrder(Authentication authentication,
            @RequestBody OrderCreateRequest request) {
        return ResponseEntity.ok(orderService.createOrder(
                authentication.getName(),
                request.getItems(),
                request.getPointAmt(),
                request.getPointType(),
                request.getShipping(),
                request.isSaveToAddressBook()));
    }

    @GetMapping("/my")
    public ResponseEntity<PageResponse<OrderDto>> getMyOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,

            Authentication authentication

    ) {
        log.info("내 주문 조회 요청: page={}, from={}, to={}, user={}", page, from, to, authentication.getName());
        // 2026-03-08 이렇게 날짜 범위가 문자열로 들어옴
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        LocalDateTime fromDate = null;
        LocalDateTime toDate = null;
        try {
            if (from != null && !from.isEmpty()) {
                fromDate = sdf.parse(from).toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
            }
            if (to != null && !to.isEmpty()) {
                toDate = sdf.parse(to).toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
            }
        } catch (Exception e) {
            log.error("날짜 파싱 오류: {}", e.getMessage());
            throw new FinalProjectException(ErrorCode.INVALID_DATE_FORMAT);
        }
        PageResponse<OrderDto> orders = orderService.getOrdersByUserId(authentication.getName(), page, fromDate,
                toDate);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/detail")
    public ResponseEntity<OrderDto> getOrderDetail(@RequestParam Long id, Authentication authentication) {

        OrderDto order = orderService.getOrderByOrderSn(id, authentication.getName());
        if (order == null) {
            throw new FinalProjectException(ErrorCode.ORDER_NOT_FOUND);
        }
        return ResponseEntity.ok(order);

    }

}
