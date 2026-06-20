package kr.or.ddit.controller.order;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import kr.or.ddit.finalProject.dto.order.OrderShippingDto;
import kr.or.ddit.finalProject.service.order.OrderShippingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/shipping")
public class OrderShippingController {

    private final OrderShippingService orderShippingService;

    // 내 주문 배송 정보 조회 (소유자 검증 포함)
    @GetMapping("/{ordSn}")
    public ResponseEntity<OrderShippingDto> getMyOrderShipping(
            @PathVariable Long ordSn, Authentication authentication) {
        String userId = authentication.getName();
        log.info("배송 정보 조회 요청 - ordSn: {}, user: {}", ordSn, userId);
        return ResponseEntity.ok(orderShippingService.getMyOrderShipping(ordSn, userId));
    }

    // 배송지 수정 (READY 상태일 때만, 본인 주문만)
    @PostMapping("/{ordSn}")
    public ResponseEntity<String> modifyOrderShipping(
            @PathVariable Long ordSn,
            Authentication authentication,
            @RequestBody OrderShippingDto orderShippingDto) {
        String userId = authentication.getName();
        log.info("배송지 수정 요청 - ordSn: {}, user: {}", ordSn, userId);

        orderShippingDto.setOrdSn(ordSn);
        orderShippingDto.setLastMdfrId(userId);
        orderShippingService.modifyOrderShipping(orderShippingDto);
        return ResponseEntity.ok("SUCCESS");
    }
}
