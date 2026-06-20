package kr.or.ddit.finalProject.service.order;

import java.time.LocalDateTime;
import java.util.List;

import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.dto.coupon.AssetType;
import kr.or.ddit.finalProject.dto.order.OrderDto;
import kr.or.ddit.finalProject.dto.order.OrderItemDto;
import kr.or.ddit.finalProject.dto.order.OrderShippingDto;

public interface OrderService {

    OrderDto createOrder(String userId, List<OrderItemDto> items, long pointAmt, AssetType pointType, OrderShippingDto shipping);

    PageResponse<OrderDto> getOrdersByUserId(String userId, int page, LocalDateTime from, LocalDateTime to);

    OrderDto getOrderByOrderSn(Long ordSn, String userId);
}
