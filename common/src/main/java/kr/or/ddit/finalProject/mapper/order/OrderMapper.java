package kr.or.ddit.finalProject.mapper.order;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.order.OrderDto;
import kr.or.ddit.finalProject.dto.order.OrderItemDto;
import kr.or.ddit.finalProject.dto.order.OrderStatus;

@Mapper
public interface OrderMapper {

    int insertOrder(OrderDto orderDto);

    int insertOrderItem(OrderItemDto orderItemDto);

    OrderDto selectOrderByOrdId(@Param("ordId") String ordId);

    List<OrderItemDto> selectOrderItemsByOrdSn(@Param("ordSn") Long ordSn);

    int updateOrderStatus(@Param("ordSn") Long ordSn, @Param("ordStatCd") OrderStatus ordStatCd);

    // 주문 생성 시 서버 기준 상품명/가격 조회 (프론트가 보낸 금액은 신뢰하지 않음)
    OrderItemDto selectCourseForOrder(@Param("prodSn") Long prodSn);

    OrderItemDto selectTextbookForOrder(@Param("prodSn") Long prodSn);
}
