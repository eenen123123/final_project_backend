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
public class MemberAddressDto {
    private Long addressSn; // PK
    private String userId; // FK -> MEMBER.USER_ID
    private String addressNm; // 배송지 별칭(예: 집, 회사)
    private String receiverNm; // 수령인 이름
    private String receiverTel; // 수령인 연락처 (주)
    private String receiverTel2; // 수령인 연락처 (부)
    private String zipCd; // 우편번호
    private String addrRoad; // 도로명 주소
    private String addrJibun; // 지번 주소
    private String addrDtl; // 상세주소
    private String deliveryMsg; // 배송 요청사항
    private String defaultYn; // 기본 배송지 여부 (Y/N)
    private LocalDateTime regDt;
    private LocalDateTime mdfcnDt;
}
