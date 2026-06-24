package kr.or.ddit.finalProject.dto.order;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderShippingDto {
    private Long shippingSn; // PK
    private Long ordSn;    // FK -> ORDERS.ORD_SN
    private String ordId;  // ORDERS.ORD_ID (실제 주문번호)
    private String ordNm;  // ORDERS.ORD_NM (주문명)
    private String buyerNm; // 구매자 이름
    private String buyerTel; // 구매자 연락처
    private String buyerEmail; // 구매자 이메일
    private String receiverNm; // 수령인 이름
    private String receiverTel; // 수령인 연락처 (주)
    private String receiverTel2; // 수령인 연락처 (부)
    private String zipCd; // 우편번호
    private String addrRoad; // 도로명 주소
    private String addrJibun; // 지번 주소
    private String addrDtl; // 상세주소
    private String deliveryMsg; // 배송 요청사항 (배송 메모)

    private ShippingStatus dlvryStatCd; // 배송 상태 Enum
    private String invoiceNo; // 송장번호 (관리자 입력)

    private String rgtrId; // 등록자 ID
    private LocalDateTime regDt; // 등록 일시
    private String lastMdfrId; // 최종 수정자 ID
    private LocalDateTime mdfcnDt; // 수정 일시

    // 검색 조건 (관리자 목록 조회용)
    private String keyword;       
    private LocalDateTime from;    
    private LocalDateTime to;      
}
