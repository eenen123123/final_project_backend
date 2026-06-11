package kr.or.ddit.finalProject.dto.cart;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartDto {

    private Long cartSn; // PK
    private String userId; // FK → MEMBER.USER_ID
    private ProductType prodDivCd; //
    private Long prodSn; // 교재SN or 강좌SN
    private String prodNm; // 상품명
    private Long prodPrice; // 상품 가격
    private Integer itemQty; // 수량 (기본 1)
    private LocalDateTime regDt; // 등록일시

}
