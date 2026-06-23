package kr.or.ddit.finalProject.dto.order;

import java.time.LocalDateTime;
import java.util.List;

import kr.or.ddit.finalProject.dto.coupon.AssetType;
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
    private Long totAmt;   // 실제 결제 금액 (할인 후)
    private Long origAmt;  // 할인 전 원본 금액 (상품합계 + 배송비)
    private OrderStatus ordStatCd; // PENDING / PAID / CANCELED
    private LocalDateTime regDt; // 등록일시
    private LocalDateTime mdfcnDt; // 수정일시

    private Long pointAmt;             // 사용 포인트량 (0이면 미사용)
    private AssetType pointType;       // HM_POINT | STUDY_POINT | HM_MONEY
    private String couponNm;           // 사용 쿠폰명 (상세 표시용)

    private boolean hasTextbook;       // 교재 포함 여부 (배송 정보 표시용)

    private CancelReason cancelRsnCd;  // 취소 사유 코드
    private String cancelRsnDtl;       // 취소 상세 사유
    private java.time.LocalDateTime cancelReqDt; // 취소 요청 일시

    private List<OrderItemDto> items; // 주문 상품 목록

}
