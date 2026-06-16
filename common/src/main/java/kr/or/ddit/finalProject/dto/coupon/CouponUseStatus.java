package kr.or.ddit.finalProject.dto.coupon;

/*
    * 쿠폰 사용 상태 (N:미사용 / Y:사용완료 / E:소멸)
    * - N: 발급된 쿠폰이 아직 사용되지 않은 상태
    * - Y: 쿠폰이 사용되어 주문에 적용된 상태
    * - E: 쿠폰이 만료되었거나 소멸된 상태
*/
public enum CouponUseStatus {
    N, // 미사용
    Y, // 사용완료
    E; // 소멸
}
