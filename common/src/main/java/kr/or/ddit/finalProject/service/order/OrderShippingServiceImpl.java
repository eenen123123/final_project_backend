package kr.or.ddit.finalProject.service.order;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.dto.order.OrderShippingDto;
import kr.or.ddit.finalProject.dto.order.ShippingStatus;
import kr.or.ddit.finalProject.exception.ErrorCode;
import kr.or.ddit.finalProject.exception.FinalProjectException;
import kr.or.ddit.finalProject.mapper.order.OrderShippingMapper;
import kr.or.ddit.finalProject.paging.PaginationInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderShippingServiceImpl implements OrderShippingService {

    private final OrderShippingMapper orderShippingMapper;

    @Override
    public Map<String, Object> getShippingStatusSummary() {
        return orderShippingMapper.selectShippingStatusSummary();
    }

    @Override
    public PageResponse<OrderShippingDto> getShippingList(PaginationInfo<OrderShippingDto> paginationInfo) {
        List<OrderShippingDto> list = orderShippingMapper.selectShippingList(paginationInfo);
        int totalCount = orderShippingMapper.countShippingList(paginationInfo);
        return new PageResponse<>(list, totalCount);
    }

    @Override
    @Transactional
    public void registerOrderShipping(OrderShippingDto dto) {
        if (dto.getOrdSn() == null) {
            throw new FinalProjectException(ErrorCode.SHIPPING_ORD_SN_REQUIRED);
        }
        if (dto.getBuyerNm() == null || dto.getBuyerNm().isBlank()) {
            throw new FinalProjectException(ErrorCode.SHIPPING_BUYER_NAME_REQUIRED);
        }
        if (dto.getBuyerTel() == null || dto.getBuyerTel().isBlank()) {
            throw new FinalProjectException(ErrorCode.SHIPPING_BUYER_TEL_REQUIRED);
        }
        orderShippingMapper.insertOrderShipping(dto);
        log.info("배송 정보 등록 완료 - ordSn: {}", dto.getOrdSn());
    }

    @Override
    public OrderShippingDto getOrderShippingByOrdSn(Long ordSn) {
        // 관리자용 - 강좌 전용 주문은 배송 정보 없을 수 있으므로 null 반환 허용
        return orderShippingMapper.selectOrderShippingByOrdSn(ordSn);
    }

    @Override
    public OrderShippingDto getMyOrderShipping(Long ordSn, String userId) {
        OrderShippingDto dto = orderShippingMapper.selectMyOrderShipping(ordSn, userId);
        if (dto == null) {
            throw new FinalProjectException(ErrorCode.SHIPPING_NOT_FOUND);
        }
        return dto;
    }

    @Override
    @Transactional
    public void modifyOrderShipping(OrderShippingDto dto) {
        if (dto.getOrdSn() == null) {
            throw new FinalProjectException(ErrorCode.SHIPPING_ORD_SN_REQUIRED);
        }
        // lastMdfrId = 요청한 사용자 ID (컨트롤러에서 인증 정보로 세팅)
        // 본인 주문인지 먼저 확인 (없거나 다른 사람 주문이면 SHIPPING_NOT_FOUND)
        OrderShippingDto existing = orderShippingMapper.selectMyOrderShipping(dto.getOrdSn(), dto.getLastMdfrId());
        if (existing == null) {
            throw new FinalProjectException(ErrorCode.SHIPPING_NOT_FOUND);
        }

        int result = orderShippingMapper.updateOrderShipping(dto);
        if (result == 0) {
            log.warn("배송지 수정 실패 (배송 시작됨) - ordSn: {}", dto.getOrdSn());
            throw new FinalProjectException(ErrorCode.SHIPPING_ALREADY_IN_PROGRESS);
        }
        log.info("배송지 수정 완료 - ordSn: {}", dto.getOrdSn());
    }

    @Override
    @Transactional
    public void changeDeliveryStatus(Long ordSn, ShippingStatus dlvryStatCd, String invoiceNo, String lastMdfrId) {
        if (dlvryStatCd == null) {
            throw new FinalProjectException(ErrorCode.SHIPPING_STATUS_REQUIRED);
        }
        if (dlvryStatCd == ShippingStatus.SHIPPING && (invoiceNo == null || invoiceNo.isBlank())) {
            throw new FinalProjectException(ErrorCode.SHIPPING_INVOICE_REQUIRED);
        }

        int result = orderShippingMapper.updateDeliveryStatus(ordSn, dlvryStatCd.name(), invoiceNo, lastMdfrId);
        if (result == 0) {
            log.warn("배송 상태 변경 실패 - ordSn: {}", ordSn);
            throw new FinalProjectException(ErrorCode.SHIPPING_NOT_FOUND);
        }
        log.info("배송 상태 변경 완료 - ordSn: {}, status: {}", ordSn, dlvryStatCd);
    }
}
