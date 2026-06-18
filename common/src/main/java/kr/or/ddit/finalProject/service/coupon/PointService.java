package kr.or.ddit.finalProject.service.coupon;

import java.util.List;

import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.dto.coupon.AssetType;
import kr.or.ddit.finalProject.dto.coupon.MemberCouponPointDto;
import kr.or.ddit.finalProject.dto.coupon.PointHistDto;

public interface PointService {

    // 포인트 적립 (지급건 생성 + 이력 기록)
    void earnPoint(String userId, AssetType assetType, long amount, Long ordSn, String memo);

    // 포인트 사용 (이력 기록)
    void usePoint(String userId, AssetType assetType, long amount, Long ordSn, String memo);

    // 포인트 잔액 조회
    long getPointBalance(String userId, AssetType assetType);

    // 포인트 이력 조회 (사용자)
    List<PointHistDto> getPointHistory(String userId);

    // 소멸 예정 포인트 조회 - 다음달 만료분
    List<MemberCouponPointDto> getExpiringPoints(String userId);

    // 스터디포인트 수동 지급 (관리자)
    void grantStudyPoint(String userId, long amount, String adminId, String memo);

    // 전체 포인트 지급 내역 조회 (관리자)
    List<MemberCouponPointDto> getAllPointGrants();

    // 스터디포인트 지급 내역 검색
    List<MemberCouponPointDto> searchStudyGrants(String q);

    // 특정 유저의 포인트 이력 조회 (타입별, 날짜 범위, 페이징)
    PageResponse<PointHistDto> getPointHistoryByType(String userId, AssetType assetType, String startDate, String endDate, int page);

    // 유저 이름 조회
    String getUserName(String userId);

}
