package kr.or.ddit.finalProject.service.pay;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.ddit.finalProject.dto.coupon.AssetType;
import kr.or.ddit.finalProject.dto.order.CancelReason;
import kr.or.ddit.finalProject.dto.order.OrderDto;
import kr.or.ddit.finalProject.dto.order.OrderStatus;
import kr.or.ddit.finalProject.dto.pay.PayHistDto;
import kr.or.ddit.finalProject.dto.order.ShippingStatus;
import kr.or.ddit.finalProject.exception.ErrorCode;
import kr.or.ddit.finalProject.exception.FinalProjectException;
import kr.or.ddit.finalProject.mapper.coupon.CouponMapper;
import kr.or.ddit.finalProject.mapper.order.OrderMapper;
import kr.or.ddit.finalProject.service.enrollment.CourseEnrollmentService;
import kr.or.ddit.finalProject.mapper.order.OrderShippingMapper;
import kr.or.ddit.finalProject.mapper.pay.PayHistMapper;
import kr.or.ddit.finalProject.service.coupon.PointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TossPayCancelService {

    private final TossPayService tossPayService;
    private final OrderMapper orderMapper;
    private final OrderShippingMapper orderShippingMapper;
    private final PayHistMapper payHistMapper;
    private final PointService pointService;
    private final CouponMapper couponMapper;
    private final CourseEnrollmentService enrollmentService;

    /**
     * 취소/환불 요청 (사용자) PAID → CANCEL_REQUESTED
     */
    @Transactional
    public void requestCancel(Long ordSn, String userId, CancelReason cancelRsnCd, String cancelRsnDtl) {
        OrderDto order = orderMapper.selectOrderByOrdSn(ordSn, userId);
        if (order == null) {
            throw new FinalProjectException(ErrorCode.ORDER_NOT_FOUND);
        }
        if (order.getOrdStatCd() != OrderStatus.PAID) {
            throw new FinalProjectException(ErrorCode.ORDER_CANNOT_CANCEL);
        }

        int result = orderMapper.requestCancel(ordSn, userId, cancelRsnCd, cancelRsnDtl);
        if (result == 0) {
            throw new FinalProjectException(ErrorCode.ORDER_NOT_FOUND);
        }
        log.info("취소 요청 완료 - ordSn: {}, userId: {}, reason: {}", ordSn, userId, cancelRsnCd);
    }

    /**
     * 취소 승인 (관리자) CANCEL_REQUESTED → CANCELED
     * 1) 토스 취소 API 호출
     * 2) 주문 CANCELED 처리
     * 3) 배송 CANCELED 처리 (교재 포함 시)
     * 4) 포인트 복원 (사용한 경우)
     */
    @Transactional
    public void approveCancel(Long ordSn, String adminId) {
        // 중복 승인 방지: 반드시 CANCEL_REQUESTED 상태여야 함
        OrderDto orderCheck = orderMapper.selectOrderByOrdSnForAdmin(ordSn);
        if (orderCheck == null) {
            throw new FinalProjectException(ErrorCode.ORDER_NOT_FOUND);
        }
        if (orderCheck.getOrdStatCd() != OrderStatus.CANCEL_REQUESTED) {
            throw new FinalProjectException(ErrorCode.ORDER_CANCEL_ALREADY_APPROVED);
        }

        // PAY_HIST에서 paymentKey 조회
        PayHistDto payHist = payHistMapper.selectPayHistByOrdSn(ordSn);
        if (payHist == null || payHist.getTossPaymentKey() == null) {
            throw new FinalProjectException(ErrorCode.ORDER_NOT_FOUND);
        }

        // 토스 취소 API 호출
        tossPayService.cancel(payHist.getTossPaymentKey(), "관리자 취소 승인");

        // 주문 CANCELED 처리
        int result = orderMapper.approveCancel(ordSn);
        if (result == 0) {
            throw new FinalProjectException(ErrorCode.ORDER_NOT_FOUND);
        }

        // 배송 CANCELED 처리 (교재 포함 주문)
        try {
            orderShippingMapper.updateDeliveryStatus(ordSn, ShippingStatus.CANCELED.name(), null, adminId);
        } catch (Exception e) {
            log.debug("배송 정보 없음 (강좌 전용 주문) - ordSn: {}", ordSn);
        }

        // 수강 회수 (강좌 포함 주문 취소 시 REVOKED 처리)
        enrollmentService.revokeByOrdSn(ordSn);

        // 쿠폰 복원 (사용된 쿠폰 USE_YN = 'N' 초기화)
        couponMapper.restoreCoupons(ordSn);

        // 포인트 복원 및 적립 포인트 회수
        OrderDto order = orderMapper.selectOrderByOrdId(payHist.getPtnrOrdId());
        if (order != null) {
            // 사용 포인트 복원
            if (order.getPointAmt() != null && order.getPointAmt() > 0) {
                pointService.earnPoint(
                    order.getUserId(),
                    order.getPointType(),
                    order.getPointAmt(),
                    ordSn,
                    "취소/환불 포인트 복원 - " + order.getOrdNm()
                );
                log.info("포인트 복원 - userId: {}, amt: {}", order.getUserId(), order.getPointAmt());
            }

            // 결제 완료 시 1% 적립된 HM포인트 회수
            long earnAmt = payHist.getTotAmt() / 100;
            if (earnAmt > 0) {
                long balance = pointService.getPointBalance(order.getUserId(), AssetType.HM_POINT);
                long reclaimAmt = Math.min(earnAmt, balance);
                if (reclaimAmt > 0) {
                    pointService.usePoint(order.getUserId(), AssetType.HM_POINT, reclaimAmt, ordSn,
                            "취소/환불 적립 포인트 회수 - " + order.getOrdNm());
                    log.info("적립 포인트 회수 - userId: {}, 회수={}p", order.getUserId(), reclaimAmt);
                }
                if (reclaimAmt < earnAmt) {
                    log.warn("적립 포인트 일부 회수 불가 (잔액 부족): userId={}, 회수대상={}p, 실제회수={}p",
                            order.getUserId(), earnAmt, reclaimAmt);
                }
            }
        }

        log.info("취소 승인 완료 - ordSn: {}, adminId: {}", ordSn, adminId);
    }
}
