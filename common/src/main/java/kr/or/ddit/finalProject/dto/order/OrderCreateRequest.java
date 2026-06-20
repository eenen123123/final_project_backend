package kr.or.ddit.finalProject.dto.order;

import java.util.List;

import kr.or.ddit.finalProject.dto.coupon.AssetType;
import lombok.Data;

@Data
public class OrderCreateRequest {

    private List<OrderItemDto> items;

    /** 사용할 포인트량 (0이면 미사용) */
    private long pointAmt = 0;

    /** 사용할 포인트 유형 (HM_POINT / STUDY_POINT, 미사용 시 null) */
    private AssetType pointType;

    /** 배송지 정보 (교재 포함 주문 시 필수 · 강좌만이면 null 허용) */
    private OrderShippingDto shipping;

    /** 배송지를 주소록에 저장 여부 (교재 포함 주문에만 유효) */
    private boolean saveToAddressBook = false;

}
