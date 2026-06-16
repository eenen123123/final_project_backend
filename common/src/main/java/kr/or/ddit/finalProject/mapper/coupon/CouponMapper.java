package kr.or.ddit.finalProject.mapper.coupon;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.coupon.CouponDto;
import kr.or.ddit.finalProject.dto.coupon.UserCouponDto;

@Mapper
public interface CouponMapper {

    // 쿠폰 정의 등록 (관리자)
    int insertCoupon(CouponDto couponDto);

    // 쿠폰 목록 조회 (관리자)
    List<CouponDto> selectAllCoupons();

    // 쿠폰 단건 조회
    CouponDto selectCouponBySn(@Param("couponSn") Long couponSn);

    // 유저에게 쿠폰 발급 (관리자)
    int insertUserCoupon(UserCouponDto userCouponDto);

    // 내 쿠폰 목록 조회 (사용자)
    List<UserCouponDto> selectUserCouponsByUserId(@Param("userId") String userId);

    // 소멸 예정 쿠폰 조회 - 다음달 만료 (사용자)
    List<UserCouponDto> selectExpiringCoupons(@Param("userId") String userId);

    // 쿠폰 수정 (관리자)
    int updateCoupon(CouponDto couponDto);

    // 쿠폰 삭제 (관리자) - 발급 이력 없을 때만
    int deleteCoupon(@Param("couponSn") Long couponSn);

    // 전체 발급내역 조회 (관리자)
    List<UserCouponDto> selectAllUserCoupons();

    // 쿠폰 코드로 쿠폰 조회 (사용자 등록용)
    CouponDto selectCouponByCode(@Param("couponCode") String couponCode);

    // 사용자가 해당 쿠폰을 이미 발급받았는지 확인
    int existsUserCoupon(@Param("userId") String userId, @Param("couponSn") Long couponSn);

}
