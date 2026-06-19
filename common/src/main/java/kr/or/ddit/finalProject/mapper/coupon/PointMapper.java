package kr.or.ddit.finalProject.mapper.coupon;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.coupon.AssetType;
import kr.or.ddit.finalProject.dto.coupon.MemberCouponPointDto;
import kr.or.ddit.finalProject.dto.coupon.PointHistDto;
import kr.or.ddit.finalProject.paging.PaginationInfo;

@Mapper
public interface PointMapper {

    // MEMBER_COUPONPOINT에 포인트 지급건 삽입
    int insertPointGrant(MemberCouponPointDto dto);

    // POINT_HIST에 이력 삽입
    int insertPointHist(PointHistDto dto);

    // 포인트 잔액 조회 (POINT_HIST CHANGE_AMT 합산)
    Long selectPointBalance(@Param("userId") String userId, @Param("assetType") AssetType assetType);

    // 포인트 이력 조회 (사용자)
    List<PointHistDto> selectPointHistByUserId(@Param("userId") String userId);

    // 소멸 예정 포인트 조회 - 다음달 만료분
    List<MemberCouponPointDto> selectExpiringPoints(@Param("userId") String userId);

    // 전체 포인트 지급 내역 조회 (관리자)
    List<MemberCouponPointDto> selectAllPointGrants();

    // 스터디포인트 지급 내역 검색 (userId or userName)
    List<MemberCouponPointDto> searchStudyGrants(@Param("q") String q);

    // 특정 유저의 포인트 이력 조회 (타입별, 날짜 범위, 페이징)
    List<PointHistDto> selectPointHistByUserAndType(
            @Param("userId") String userId,
            @Param("assetType") AssetType assetType,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("paginationInfo") PaginationInfo<?> paginationInfo);

    // 특정 유저의 포인트 이력 전체 조회 (관리자용)
    List<PointHistDto> selectAllPointHistByUserAndType(
            @Param("userId") String userId,
            @Param("assetType") AssetType assetType);

    // 포인트 이력 전체 건수
    int selectPointHistCountByUserAndType(
            @Param("userId") String userId,
            @Param("assetType") AssetType assetType,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate);

    // 유저 ID로 회원 이름 조회
    String selectUserNameById(@Param("userId") String userId);

    // 유저 목록 + 포인트 잔액 조회 (관리자)
    List<java.util.Map<String, Object>> searchUsersWithBalance(
            @Param("q") String q,
            @Param("assetType") AssetType assetType,
            @Param("role") String role);

}
