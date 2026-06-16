package kr.or.ddit.finalProject.service.coupon;

import java.util.List;

import kr.or.ddit.finalProject.dto.coupon.CouponDto;
import kr.or.ddit.finalProject.dto.coupon.UserCouponDto;

public interface CouponService {

    // 쿠폰 생성 (관리자)
    CouponDto createCoupon(CouponDto couponDto, String adminId);

    // 쿠폰 목록 조회 (관리자)
    List<CouponDto> getAllCoupons();

    // 유저에게 쿠폰 발급 (관리자)
    UserCouponDto issueCoupon(Long couponSn, String userId, String adminId);

    // 내 쿠폰 목록 조회 (사용자)
    List<UserCouponDto> getMyCoupons(String userId);

    // 소멸 예정 쿠폰 조회 - 다음달 만료 (사용자)
    List<UserCouponDto> getExpiringCoupons(String userId);

    // 여러 유저에게 쿠폰 일괄 발급 (관리자)
    List<UserCouponDto> bulkIssueCoupon(Long couponSn, List<String> userIds, String adminId);

    // 쿠폰 수정 (관리자)
    CouponDto updateCoupon(CouponDto couponDto);

    // 쿠폰 삭제 (관리자) - 발급 이력 없을 때만 가능
    void deleteCoupon(Long couponSn);

    // 전체 발급내역 조회 (관리자)
    List<UserCouponDto> getAllIssuedCoupons();

    // 쿠폰 코드 입력으로 발급 (사용자)
    UserCouponDto redeemCoupon(String couponCode, String userId);

}
