package kr.or.ddit.finalProject.dto.order;

import java.time.LocalDateTime;

import kr.or.ddit.finalProject.dto.cart.ProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDto {

    private Long ordItemSn; // PK
    private Long ordSn; // FK → ORDERS.ORD_SN
    private ProductType prodDivCd; // TEXTBOOK / COURSE
    private Long prodSn; // 교재SN or 강좌SN
    private String prodNm; // 주문 시점 상품명 스냅샷
    private Long prodPrice; // 주문 시점 단가 스냅샷
    private Integer itemQty; // 수량 (기본 1)
    private LocalDateTime regDt; // 등록일시

    private String prodImg; // 상품 이미지

}
