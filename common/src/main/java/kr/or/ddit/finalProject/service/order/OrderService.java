package kr.or.ddit.finalProject.service.order;

import java.time.LocalDateTime;
import java.util.List;

import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.dto.coupon.AssetType;
import kr.or.ddit.finalProject.dto.order.OrderDto;
import kr.or.ddit.finalProject.dto.order.OrderItemDto;
import kr.or.ddit.finalProject.dto.order.OrderSearchCondition;
import kr.or.ddit.finalProject.dto.order.OrderShippingDto;
import kr.or.ddit.finalProject.paging.PaginationInfo;

public interface OrderService {

    OrderDto createOrder(String userId, List<OrderItemDto> items, long pointAmt, AssetType pointType, OrderShippingDto shipping, boolean saveToAddressBook);

    PageResponse<OrderDto> getOrdersByUserId(String userId, int page, LocalDateTime from, LocalDateTime to);

    OrderDto getOrderByOrderSn(Long ordSn, String userId);

    // 전체 주문 조회 (관리자)
    PageResponse<OrderDto> getAllOrders(PaginationInfo<OrderSearchCondition> paginationInfo);

    // 전체 주문 건수 (관리자 카드용)
    int getTotalOrderCount();

    // 취소 요청 목록 조회 (관리자)
    List<OrderDto> getCancelRequestList();

    // 취소 상세 조회 (관리자 - 모달용, 아이템 포함)
    OrderDto getCancelDetailByOrdSn(Long ordSn);
}
