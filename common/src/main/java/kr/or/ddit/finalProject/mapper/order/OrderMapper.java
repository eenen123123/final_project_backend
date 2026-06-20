package kr.or.ddit.finalProject.mapper.order;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.order.CancelReason;
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

    // 관리자용 전체 주문 조회
    List<OrderDto> selectAllOrders(@Param("paginationInfo") PaginationInfo<OrderSearchCondition> paginationInfo);

    int countAllOrders(@Param("paginationInfo") PaginationInfo<OrderSearchCondition> paginationInfo);

    int countTotalOrders();

    // 취소/환불 요청 (PAID → CANCEL_REQUESTED)
    int requestCancel(@Param("ordSn") Long ordSn,
                      @Param("userId") String userId,
                      @Param("cancelRsnCd") CancelReason cancelRsnCd,
                      @Param("cancelRsnDtl") String cancelRsnDtl);

    // 취소 승인 (CANCEL_REQUESTED → CANCELED) - 관리자
    int approveCancel(@Param("ordSn") Long ordSn);

    // 관리자용 주문 단건 조회 (userId 없음)
    OrderDto selectOrderByOrdSnForAdmin(@Param("ordSn") Long ordSn);

    // 취소 요청 목록 조회 - 관리자
    List<OrderDto> selectCancelRequestList();
}
