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
import kr.or.ddit.finalProject.mapper.coupon.CouponMapper;
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
    private final CouponMapper couponMapper;

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

        TossPaymentResponse response = tossPayService.confirm(request);

        try {
            List<OrderItemDto> items = orderMapper.selectOrderItemsByOrdSn(order.getOrdSn());
            payHistMapper.insertPayHist(toPayHist(userId, order, items, response));
            orderMapper.updateOrderStatus(order.getOrdSn(), OrderStatus.PAID);

            for (OrderItemDto item : items) {
                cartMapper.deleteCartByUserAndProd(userId, item.getProdDivCd(), item.getProdSn());
                if (item.getProdDivCd() == ProductType.COURSE) {
                    enrollmentService.grantOrExtend(userId, item.getProdSn(), order.getOrdSn());
                }
            }

            if (order.getPointAmt() != null && order.getPointAmt() > 0) {
                pointService.usePoint(userId, order.getPointType(), order.getPointAmt(),
                        order.getOrdSn(), "주문 결제 사용 - " + order.getOrdNm());
            }

            long earnAmt = order.getTotAmt() / 100;
            if (earnAmt > 0) {
                pointService.earnPoint(userId, AssetType.HM_POINT, earnAmt, order.getOrdSn(), order.getOrdNm());
            }

            // 쿠폰 사용 확정 (예약된 쿠폰 USE_YN = 'Y')
            couponMapper.confirmCoupons(order.getOrdSn());

        } catch (Exception e) {
            // DB 처리 실패 시 토스 결제 자동 취소
            log.error("결제 DB 처리 실패 - 토스 자동 취소 시도. paymentKey={}", response.getPaymentKey(), e);
            try {
                tossPayService.cancel(response.getPaymentKey(), "결제 처리 오류로 인한 자동 취소");
                log.info("토스 자동 취소 완료 - paymentKey={}", response.getPaymentKey());
            } catch (Exception cancelEx) {
                log.error("토스 자동 취소 실패 - 수동 처리 필요. paymentKey={}", response.getPaymentKey(), cancelEx);
            }
            throw new FinalProjectException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "결제 처리 중 오류가 발생하여 결제가 취소되었습니다. 잠시 후 다시 시도해주세요.");
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
                .pointAmt(order.getPointAmt())
                .build();
    }

    // 토스 일시 형식: 2024-02-13T12:18:14+09:00
    private LocalDateTime toLocalDateTime(String isoDateTime) {
        return isoDateTime == null ? null : OffsetDateTime.parse(isoDateTime).toLocalDateTime();
    }
}
//@formatter:on