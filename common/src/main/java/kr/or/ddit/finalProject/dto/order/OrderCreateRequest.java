package kr.or.ddit.finalProject.dto.order;

import java.util.List;

import kr.or.ddit.finalProject.dto.coupon.AssetType;
import lombok.Data;

/**
 * 주문 생성 요청 DTO
 * 클라이언트가 보내도 되는 값만 담는 입력 전용 DTO
 * [포인트 시스템] 기존 List<OrderItemDto> 단순 전달 방식에서
 * 포인트 사용 정보(pointAmt, pointType)를 함께 전달하기 위해 래퍼 DTO 추가
 */
@Data
public class OrderCreateRequest {

    private List<OrderItemDto> items;

    /** 사용할 포인트량 (0이면 미사용) */
    private long pointAmt = 0;

    /** 사용할 포인트 유형 (HM_POINT / STUDY_POINT, 미사용 시 null) */
    private AssetType pointType;

}
