package kr.or.ddit.finalProject.mapper.order;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.order.OrderDto;
import kr.or.ddit.finalProject.dto.order.OrderItemDto;
import kr.or.ddit.finalProject.dto.order.OrderSearchCondition;
import kr.or.ddit.finalProject.dto.order.OrderStatus;
import kr.or.ddit.finalProject.paging.PaginationInfo;

@Mapper
public interface OrderMapper {

    int insertOrder(OrderDto orderDto);

    int insertOrderItem(OrderItemDto orderItemDto);

    OrderDto selectOrderByOrdId(@Param("ordId") String ordId);

    List<OrderItemDto> selectOrderItemsByOrdSn(@Param("ordSn") Long ordSn);

    int updateOrderStatus(@Param("ordSn") Long ordSn, @Param("ordStatCd") OrderStatus ordStatCd);

    // 새 주문 생성 시, 같은 회원의 결제 안 된 PENDING 주문을 EXPIRED로 정리
    int expirePendingOrders(@Param("userId") String userId);

    // 주문 생성 시 서버 기준 상품명/가격 조회 (프론트가 보낸 금액은 신뢰하지 않음)
    OrderItemDto selectCourseForOrder(@Param("prodSn") Long prodSn);

    OrderItemDto selectTextbookForOrder(@Param("prodSn") Long prodSn);

    List<OrderDto> selectOrdersByUserId(@Param("userId") String userId,
            @Param("paginationInfo") PaginationInfo<OrderSearchCondition> paginationInfo);

    int selectOrderTotalCountByUserId(@Param("userId") String userId,
            @Param("paginationInfo") PaginationInfo<OrderSearchCondition> paginationInfo);

    List<OrderItemDto> selectOrderItemsByOrderSn(@Param("ordSn") Long ordSn, @Param("userId") String userId);

    OrderDto selectOrderByOrdSn(@Param("ordSn") Long ordSn, @Param("userId") String userId);
}
