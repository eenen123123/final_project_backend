package kr.or.ddit.controller.order;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import kr.or.ddit.finalProject.dto.order.OrderShippingDto;
import kr.or.ddit.finalProject.dto.order.ShippingStatus;
import kr.or.ddit.finalProject.service.order.OrderShippingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/shipping")
public class AdminOrderShippingController {

    private final OrderShippingService orderShippingService;

    // 주문별 배송 정보 조회 (AJAX)
    @GetMapping("/api/{ordSn}")
    @ResponseBody
    public ResponseEntity<OrderShippingDto> getOrderShipping(@PathVariable Long ordSn) {
        log.info("관리자 배송 정보 조회 - ordSn: {}", ordSn);
        return ResponseEntity.ok(orderShippingService.getOrderShippingByOrdSn(ordSn));
    }

    // 송장번호 등록 및 배송 상태 변경 (AJAX)
    @PostMapping("/api/{ordSn}/status")
    @ResponseBody
    public ResponseEntity<String> changeDeliveryStatus(
            @PathVariable Long ordSn,
            @RequestParam ShippingStatus dlvryStatCd,
            @RequestParam(required = false) String invoiceNo,
            Authentication authentication) {
        String adminId = authentication.getName();
        log.info("배송 상태 변경 - ordSn: {}, status: {}, invoice: {}, admin: {}", ordSn, dlvryStatCd, invoiceNo, adminId);
        orderShippingService.changeDeliveryStatus(ordSn, dlvryStatCd, invoiceNo, adminId);
        return ResponseEntity.ok("SUCCESS");
    }
}
