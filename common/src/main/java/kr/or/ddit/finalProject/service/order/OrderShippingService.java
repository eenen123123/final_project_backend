package kr.or.ddit.finalProject.service.order;

import java.util.Map;

import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.dto.order.OrderShippingDto;
import kr.or.ddit.finalProject.dto.order.ShippingStatus;
import kr.or.ddit.finalProject.paging.PaginationInfo;

public interface OrderShippingService {

    // 배송 목록 조회 (관리자)
    PageResponse<OrderShippingDto> getShippingList(PaginationInfo<OrderShippingDto> paginationInfo);

    // 상태별 건수 요약
    Map<String, Object> getShippingStatusSummary();

    // 배송 정보 등록 (결제 완료 시 호출)
    void registerOrderShipping(OrderShippingDto orderShippingDto);

    // 배송 정보 조회 - 관리자용 (소유자 검증 없음)
    OrderShippingDto getOrderShippingByOrdSn(Long ordSn);

    // 배송 정보 조회 - 사용자용 (소유자 검증 포함)
    OrderShippingDto getMyOrderShipping(Long ordSn, String userId);

    // 배송지 수정 (READY 상태일 때만 가능)
    void modifyOrderShipping(OrderShippingDto orderShippingDto);

    // 배송 상태 및 송장번호 변경 (관리자 전용)
    void changeDeliveryStatus(Long ordSn, ShippingStatus dlvryStatCd, String invoiceNo, String lastMdfrId);
}
