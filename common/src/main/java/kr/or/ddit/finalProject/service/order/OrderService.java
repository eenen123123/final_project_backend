package kr.or.ddit.finalProject.service.order;

import java.time.LocalDateTime;
import java.util.List;

import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.dto.coupon.AssetType;
import kr.or.ddit.finalProject.dto.order.OrderDto;
import kr.or.ddit.finalProject.dto.order.OrderItemDto;

public interface OrderService {

    /**
     * 결제 전 주문 선생성. 상품명/가격은 서버가 DB에서 재조회해 채운다 (금액 위변조 방지).
     *
     * @param userId    주문자
     * @param items     prodDivCd, prodSn, itemQty만 사용
     * @param pointAmt  사용할 포인트량 (0이면 미사용) — [포인트 시스템 추가]
     * @param pointType 사용할 포인트 유형 (HM_POINT / STUDY_POINT, pointAmt=0이면 null) — [포인트 시스템 추가]
     * @return 생성된 주문 (ordId, totAmt를 토스 결제창 호출에 사용)
     */
    OrderDto createOrder(String userId, List<OrderItemDto> items, long pointAmt, AssetType pointType);

    PageResponse<OrderDto> getOrdersByUserId(String userId, int page, LocalDateTime from, LocalDateTime to);

    OrderDto getOrderByOrderSn(Long ordSn, String userId);
}
