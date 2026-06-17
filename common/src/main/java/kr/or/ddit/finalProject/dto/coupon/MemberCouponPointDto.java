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
public class MemberCouponPointDto {

    private Long mcpntSn;              // PK
    private String userId;             // FK → MEMBER.USER_ID
    private AssetType assetType;       // COUPON | HM_POINT | STUDY_POINT | HM_MONEY

    // 쿠폰일 때만 사용
    private Long couponSn;             // FK → COUPON.COUPON_SN
    private String couponNm;           // 발급 시점 쿠폰명 스냅샷

    // 포인트일 때만 사용
    private Long pointAmt;             // 지급 포인트량

    private LocalDateTime issueDt;     // 발급일시
    private LocalDate expiryDt;        // 만료일 (HM머니는 null)
    private CouponUseStatus useYn;     // N:미사용 / Y:사용완료 / E:소멸
    private LocalDateTime useDt;       // 사용일시
    private Long orderSn;              // 사용한 주문 SN
    private String issuedBy;           // 발급 관리자 ID
    private LocalDateTime regDt;       // 등록일시

    private String userName;           // JOIN 조회용
}
