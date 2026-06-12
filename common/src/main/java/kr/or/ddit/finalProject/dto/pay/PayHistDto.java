package kr.or.ddit.finalProject.dto.pay;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayHistDto {

    private Long payHistSn; // PK
    private String userId; // FK → MEMBER.USER_ID
    private String payDivCd; // 01:계좌이체 / 02:카드 / 03:카카오페이 / 04:토스
    private String tossPaymentKey; // [Toss]paymentKey · 취소 API 필수
    private Long ordSn; // FK → ORDERS.ORD_SN
    private String ptnrOrdId; // 가맹점 자체 주문번호 (토스 orderId)
    private String itemNm; // 주문명
    private Integer itemQty; // 총 상품 수량
    private String payStatCd; // PG status 값 그대로 (예: DONE)
    private Long totAmt; // 총 결제 금액
    private Long taxFreeAmt; // 면세 금액
    private Long vatAmt; // 부가세
    private LocalDateTime payReadyDt; // [Toss]requestedAt
    private LocalDateTime payAprvlDt; // [Toss]approvedAt
    private String rgtrId; // 등록자 ID

}
