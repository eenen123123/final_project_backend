package kr.or.ddit.finalProject.dto.order;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {

    private Long ordSn; // PK
    private String ordId; // 토스 orderId · 서버 발급 UUID · 금액 검증 기준
    private String userId; // FK → MEMBER.USER_ID
    private String ordNm; // 주문명 예: 수학 정석 강의 외 2건
    private Long totAmt; // 서버가 재계산한 총 결제 금액
    private OrderStatus ordStatCd; // PENDING / PAID / CANCELED
    private LocalDateTime regDt; // 등록일시
    private LocalDateTime mdfcnDt; // 수정일시

    private List<OrderItemDto> items; // 주문 상품 목록

}
