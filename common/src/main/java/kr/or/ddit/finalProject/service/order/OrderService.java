package kr.or.ddit.finalProject.service.order;

import java.time.LocalDateTime;
import java.util.List;

import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.dto.order.OrderDto;
import kr.or.ddit.finalProject.dto.order.OrderItemDto;

public interface OrderService {

    /**
     * 결제 전 주문 선생성. 상품명/가격은 서버가 DB에서 재조회해 채운다 (금액 위변조 방지).
     *
     * @param userId 주문자
     * @param items  prodDivCd, prodSn, itemQty만 사용
     * @return 생성된 주문 (ordId, totAmt를 토스 결제창 호출에 사용)
     */
    OrderDto createOrder(String userId, List<OrderItemDto> items);

    PageResponse<OrderDto> getOrdersByUserId(String userId, int page, LocalDateTime from, LocalDateTime to);

    OrderDto getOrderByOrderSn(Long ordSn, String userId);
}
