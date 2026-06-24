package kr.or.ddit.finalProject.mapper.order;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.order.OrderShippingDto;
import kr.or.ddit.finalProject.paging.PaginationInfo;

@Mapper
public interface OrderShippingMapper {

    List<OrderShippingDto> selectShippingList(PaginationInfo<OrderShippingDto> paginationInfo);

    int countShippingList(PaginationInfo<OrderShippingDto> paginationInfo);

    Map<String, Object> selectShippingStatusSummary();

    int insertOrderShipping(OrderShippingDto orderShippingDto);

    OrderShippingDto selectOrderShippingByOrdSn(Long ordSn);

    OrderShippingDto selectMyOrderShipping(@Param("ordSn") Long ordSn, @Param("userId") String userId);

    int updateOrderShipping(OrderShippingDto orderShippingDto);

    /**
     * 배송 상태 및 송장번호 업데이트 (관리자 기능)
     * 
     * @param ordSn 주문 일련번호
     * @param dlvryStatCd 변경할 배송 상태 (READY, SHIPPING, DELIVERED)
     * @param invoiceNo 송장번호
     * @param lastMdfrId 수정자(관리자) ID
     */
    int updateDeliveryStatus(
        @Param("ordSn") Long ordSn,
        @Param("dlvryStatCd") String dlvryStatCd,
        @Param("invoiceNo") String invoiceNo,
        @Param("lastMdfrId") String lastMdfrId
    );

}
