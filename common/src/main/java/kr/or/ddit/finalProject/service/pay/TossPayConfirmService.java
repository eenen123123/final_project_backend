package kr.or.ddit.finalProject.service.pay;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.ddit.finalProject.dto.cart.ProductType;
import kr.or.ddit.finalProject.dto.order.OrderDto;
import kr.or.ddit.finalProject.dto.order.OrderItemDto;
import kr.or.ddit.finalProject.dto.order.OrderStatus;
import kr.or.ddit.finalProject.dto.pay.PayHistDto;
import kr.or.ddit.finalProject.dto.pay.toss.TossPayRequest;
import kr.or.ddit.finalProject.dto.pay.toss.TossPaymentResponse;
import kr.or.ddit.finalProject.exception.ErrorCode;
import kr.or.ddit.finalProject.exception.FinalProjectException;
import kr.or.ddit.finalProject.mapper.cart.CartMapper;
import kr.or.ddit.finalProject.mapper.order.OrderMapper;
import kr.or.ddit.finalProject.mapper.pay.PayHistMapper;
import kr.or.ddit.finalProject.dto.coupon.AssetType;
import kr.or.ddit.finalProject.service.enrollment.CourseEnrollmentService;
import kr.or.ddit.finalProject.service.coupon.PointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
//@formatter:off
public class TossPayConfirmService {

    private static final String PAY_DIV_TOSS = "04"; // PAY_HIST.PAY_DIV_CD 토스 코드

    private final TossPayService tossPayService;
    private final OrderMapper orderMapper;
    private final PayHistMapper payHistMapper;
    private final CartMapper cartMapper;
    private final CourseEnrollmentService enrollmentService;
    private final PointService pointService;

    /**
     * 토스 결제 승인 + 결제/주문 확정 처리.
     * 1) 주문 검증 (존재·소유자·PENDING·금액 일치) → 2) 토스 승인 API → 3) PAY_HIST 저장,
     * 주문 PAID 확정, 장바구니에서 구매 상품 제거
     */
    @Transactional
    public TossPaymentResponse confirmPayment(String userId, TossPayRequest request) {
        OrderDto order = orderMapper.selectOrderByOrdId(request.getOrderId());
        if (order == null || !order.getUserId().equals(userId)) {
            throw new FinalProjectException(ErrorCode.ORDER_NOT_FOUND);
        }
        if (order.getOrdStatCd() != OrderStatus.PENDING) {
            throw new FinalProjectException(ErrorCode.ORDER_ALREADY_PAID);
        }
        if (!order.getTotAmt().equals(Long.valueOf(request.getAmount()))) {
            log.warn("토스 결제 금액 불일치 orderId={}, 주문 금액={}, 요청 금액={}",
                    request.getOrderId(), order.getTotAmt(), request.getAmount());
            throw new FinalProjectException(ErrorCode.ORDER_AMOUNT_MISMATCH);
        }

        // 승인 성공 시 TossPayService가 응답 전체(paymentKey 포함)를 로그로 남기므로
        // 이후 DB 저장이 실패해도 로그 기준으로 수동 복구 가능
        TossPaymentResponse response = tossPayService.confirm(request);

        List<OrderItemDto> items = orderMapper.selectOrderItemsByOrdSn(order.getOrdSn());
        payHistMapper.insertPayHist(toPayHist(userId, order, items, response));
        orderMapper.updateOrderStatus(order.getOrdSn(), OrderStatus.PAID);

        for (OrderItemDto item : items) {
            cartMapper.deleteCartByUserAndProd(userId, item.getProdDivCd(), item.getProdSn());
            if (item.getProdDivCd() == ProductType.COURSE) {
                // 강좌만 수강권한 부여/연장 (결제일 + 1년)
                enrollmentService.grantOrExtend(userId, item.getProdSn(), order.getOrdSn());
            }
        }

        // HM 포인트 적립 (결제금액의 1%, 0이면 스킵)
        long earnAmt = order.getTotAmt() / 100;
        if (earnAmt > 0) {
            pointService.earnPoint(userId, AssetType.HM_POINT, earnAmt, order.getOrdSn(), order.getOrdNm());
        }

        return response;
    }

    private PayHistDto toPayHist(String userId, OrderDto order, List<OrderItemDto> items,
            TossPaymentResponse response) {
        int totalQty = items.stream().mapToInt(OrderItemDto::getItemQty).sum();
        return PayHistDto.builder()
                .userId(userId)
                .payDivCd(PAY_DIV_TOSS)
                .tossPaymentKey(response.getPaymentKey())
                .ordSn(order.getOrdSn())
                .ptnrOrdId(response.getOrderId())
                .itemNm(order.getOrdNm())
                .itemQty(totalQty)
                .payStatCd(response.getStatus())
                .totAmt(response.getTotalAmount())
                .taxFreeAmt(response.getTaxFreeAmount())
                .vatAmt(response.getVat())
                .payReadyDt(toLocalDateTime(response.getRequestedAt()))
                .payAprvlDt(toLocalDateTime(response.getApprovedAt()))
                .rgtrId(userId)
                .build();
    }

    // 토스 일시 형식: 2024-02-13T12:18:14+09:00
    private LocalDateTime toLocalDateTime(String isoDateTime) {
        return isoDateTime == null ? null : OffsetDateTime.parse(isoDateTime).toLocalDateTime();
    }
}
//@formatter:on