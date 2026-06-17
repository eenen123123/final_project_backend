package kr.or.ddit.finalProject.mapper.coupon;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.coupon.CouponDto;
import kr.or.ddit.finalProject.dto.coupon.MemberCouponPointDto;

@Mapper
public interface CouponMapper {

    // 쿠폰 정의 등록 (관리자)
    int insertCoupon(CouponDto couponDto);

    // 쿠폰 목록 조회 (관리자)
    List<CouponDto> selectAllCoupons();

    // 쿠폰 단건 조회
    CouponDto selectCouponBySn(@Param("couponSn") Long couponSn);

    // 유저에게 쿠폰 발급 (관리자)
    int insertUserCoupon(MemberCouponPointDto userCouponDto);

    // 내 쿠폰 목록 조회 (사용자)
    List<MemberCouponPointDto> selectUserCouponsByUserId(@Param("userId") String userId);

    // 소멸 예정 쿠폰 조회 - 다음달 만료 (사용자)
    List<MemberCouponPointDto> selectExpiringCoupons(@Param("userId") String userId);

    // 쿠폰 수정 (관리자)
    int updateCoupon(CouponDto couponDto);

    // 쿠폰 삭제 (관리자) - 발급 이력 없을 때만
    int deleteCoupon(@Param("couponSn") Long couponSn);

    // 전체 발급내역 조회 (관리자)
    List<MemberCouponPointDto> selectAllUserCoupons();

    // 쿠폰 코드로 쿠폰 조회 (사용자 등록용)
    CouponDto selectCouponByCode(@Param("couponCode") String couponCode);

    // 사용자가 해당 쿠폰을 이미 발급받았는지 확인
    int existsUserCoupon(@Param("userId") String userId, @Param("couponSn") Long couponSn);

    // 스터디포인트 정의 등록
    int insertStudyPointDef(CouponDto couponDto);

    // 스터디포인트 정의 목록 조회
    List<CouponDto> selectAllStudyPointDefs();

    // 스터디포인트 정의 단건 조회
    CouponDto selectStudyPointDefBySn(@Param("couponSn") Long couponSn);

    // 스터디포인트 정의 수정
    int updateStudyPointDef(CouponDto couponDto);

    // 스터디포인트 정의 삭제 (발급 이력 없을 때만)
    int deleteStudyPointDef(@Param("couponSn") Long couponSn);

    // 스터디포인트 발급 내역 조회 (관리자, couponSn null이면 전체)
    List<MemberCouponPointDto> selectAllStudyPointGrants(@Param("couponSn") Long couponSn);

}
