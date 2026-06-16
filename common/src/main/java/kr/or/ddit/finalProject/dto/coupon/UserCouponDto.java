package kr.or.ddit.finalProject.dto.coupon;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCouponDto {

    private Long userCouponSn;      // PK
    private String userId;          // FK → MEMBER.USER_ID
    private Long couponSn;          // FK → COUPON.COUPON_SN
    private String couponNm;        // 발급 시점 쿠폰명 스냅샷
    private LocalDateTime issueDt;  // 발급일시
    private LocalDate expiryDt;     // 만료일
    private CouponUseStatus useYn;  // 사용 여부 (N:미사용 / Y:사용완료 / E:소멸)
    private LocalDateTime useDt;    // 사용일시
    private Long orderSn;           // 사용한 주문 SN (미사용 시 null)
    private String issuedBy;        // 발급한 관리자 ID (이벤트 자동 발급 시 null)
    private LocalDateTime regDt;    // 등록일시
    private String userName;        // 수신 유저 이름 (JOIN 조회 시)

}
