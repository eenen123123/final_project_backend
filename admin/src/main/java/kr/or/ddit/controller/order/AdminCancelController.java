package kr.or.ddit.controller.order;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import kr.or.ddit.finalProject.dto.order.OrderDto;
import kr.or.ddit.finalProject.service.order.OrderService;
import kr.or.ddit.finalProject.service.pay.TossPayCancelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/admin/cancel")
@RequiredArgsConstructor
public class AdminCancelController {

    private final TossPayCancelService tossPayCancelService;
    private final OrderService orderService;

    // 주문 상세 조회 (모달용 AJAX)
    @GetMapping("/api/{ordSn}")
    @ResponseBody
    public ResponseEntity<OrderDto> getCancelDetail(@PathVariable Long ordSn) {
        OrderDto order = orderService.getCancelDetailByOrdSn(ordSn);
        return ResponseEntity.ok(order);
    }

    // 취소 승인 (AJAX)
    @PostMapping("/api/{ordSn}/approve")
    @ResponseBody
    public ResponseEntity<String> approveCancel(
            @PathVariable Long ordSn,
            Authentication authentication) {
        String adminId = authentication.getName();
        log.info("취소 승인 - ordSn: {}, adminId: {}", ordSn, adminId);
        tossPayCancelService.approveCancel(ordSn, adminId);
        return ResponseEntity.ok("SUCCESS");
    }
}
