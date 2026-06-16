package kr.or.ddit.finalProject.dto.coupon;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponDto {

    private Long couponSn;       // PK
    private String couponNm;     // 쿠폰명
    private DiscType discType;   // 할인 방식 (FIXED:정액 / RATE:정률)
    private Long discAmt;        // 정액 할인금액(0,000원)
    private Integer discRate;    // 정률 할인율(%)
    private String useLimitCd;   // 사용 가능 상품 유형 (COURSE / TEXTBOOK / ALL)
    private Integer validDays;   // 발급 후 유효일수
    private String useYn;        // 활성 여부 (Y:활성 / N:비활성)
    private String regUserId;    // 등록한 관리자 ID
    private LocalDateTime regDt; // 등록일시
    private String couponCode;   // 사용자 직접 입력용 쿠폰 코드 (nullable, unique)

}
