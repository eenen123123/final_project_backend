package kr.or.ddit.finalProject.service.coupon;

import java.util.List;

import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.dto.coupon.CouponDto;
import kr.or.ddit.finalProject.dto.coupon.MemberCouponPointDto;

public interface CouponService {

    // 쿠폰 생성 (관리자)
    CouponDto createCoupon(CouponDto couponDto, String adminId);

    // 쿠폰 목록 조회 (관리자)
    List<CouponDto> getAllCoupons();

    // 유저에게 쿠폰 발급 (관리자)
    MemberCouponPointDto issueCoupon(Long couponSn, String userId, String adminId);

    // 내 쿠폰 목록 조회 (사용자, 날짜 범위, 페이징)
    PageResponse<MemberCouponPointDto> getMyCoupons(String userId, String startDate, String endDate, int page);

    // 소멸 예정 쿠폰 조회 - 다음달 만료 (사용자)
    List<MemberCouponPointDto> getExpiringCoupons(String userId);

    // 결제 페이지용 사용 가능한 쿠폰 목록 (미사용 + 미만료 + 할인정보 포함)
    List<MemberCouponPointDto> getAvailableCouponsForCheckout(String userId);

    // 여러 유저에게 쿠폰 일괄 발급 (관리자)
    List<MemberCouponPointDto> bulkIssueCoupon(Long couponSn, List<String> userIds, String adminId);

    // 쿠폰 수정 (관리자)
    CouponDto updateCoupon(CouponDto couponDto);

    // 쿠폰 삭제 (관리자) - 발급 이력 없을 때만 가능
    void deleteCoupon(Long couponSn);

    // 전체 발급내역 조회 (관리자)
    List<MemberCouponPointDto> getAllIssuedCoupons();

    // 쿠폰 코드 입력으로 발급 (사용자)
    MemberCouponPointDto redeemCoupon(String couponCode, String userId);

    // 스터디포인트 정의 등록 (관리자)
    CouponDto createStudyPointDef(CouponDto couponDto, String adminId);

    // 스터디포인트 정의 목록 조회 (관리자)
    List<CouponDto> getAllStudyPointDefs();

    // 스터디포인트 정의 수정 (관리자)
    CouponDto updateStudyPointDef(CouponDto couponDto);

    // 스터디포인트 정의 삭제 (관리자)
    void deleteStudyPointDef(Long couponSn);

    // 스터디포인트 발급 (관리자)
    MemberCouponPointDto issueStudyPoint(Long couponSn, String userId, String adminId);

    // 스터디포인트 일괄 발급 (관리자)
    List<MemberCouponPointDto> bulkIssueStudyPoint(Long couponSn, List<String> userIds, String adminId);

    // 스터디포인트 발급 내역 조회 (관리자)
    List<MemberCouponPointDto> getAllStudyPointGrants(Long couponSn);

}
