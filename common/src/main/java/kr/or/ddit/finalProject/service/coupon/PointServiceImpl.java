package kr.or.ddit.finalProject.service.coupon;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.dto.coupon.AssetType;
import kr.or.ddit.finalProject.dto.coupon.CouponUseStatus;
import kr.or.ddit.finalProject.dto.coupon.MemberCouponPointDto;
import kr.or.ddit.finalProject.dto.coupon.PointHistDto;
import kr.or.ddit.finalProject.dto.coupon.PointHistType;
import kr.or.ddit.finalProject.paging.PaginationInfo;
import kr.or.ddit.finalProject.exception.ErrorCode;
import kr.or.ddit.finalProject.exception.FinalProjectException;
import kr.or.ddit.finalProject.mapper.MemberMapper;
import kr.or.ddit.finalProject.mapper.coupon.PointMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointServiceImpl implements PointService {

    private static final long MIN_USE_AMOUNT = 1_000L;

    private final PointMapper pointMapper;
    private final MemberMapper memberMapper;

    @Override
    @Transactional
    public void earnPoint(String userId, AssetType assetType, long amount, Long ordSn, String memo) {
        validatePointType(assetType);
        validateAmount(amount);
        if (userId == null || userId.isBlank()) {
            throw new FinalProjectException(ErrorCode.BAD_REQUEST);
        }

        MemberCouponPointDto grant = MemberCouponPointDto.builder()
                .userId(userId)
                .assetType(assetType)
                .pointAmt(amount)
                .expiryDt(LocalDate.now().plusYears(1))
                .useYn(CouponUseStatus.N)
                .build();
        pointMapper.insertPointGrant(grant);

        pointMapper.insertPointHist(PointHistDto.builder()
                .mcpntSn(grant.getMcpntSn())
                .userId(userId)
                .histType(PointHistType.EARN)
                .changeAmt(amount)
                .ordSn(ordSn)
                .memo(memo)
                .build());

        log.info("포인트 적립: userId={}, assetType={}, amount={}", userId, assetType, amount);
    }

    @Override
    @Transactional
    public void usePoint(String userId, AssetType assetType, long amount, Long ordSn, String memo) {
        validatePointType(assetType);
        validateAmount(amount);
        if (userId == null || userId.isBlank()) {
            throw new FinalProjectException(ErrorCode.BAD_REQUEST);
        }
        // 1,000p 미만 사용 불가
        if (amount < MIN_USE_AMOUNT) {
            throw new FinalProjectException(ErrorCode.POINT_MINIMUM_USAGE);
        }

        // 잔액 체크 + 차감을 단일 SQL로 원자적 처리 (동시성 보호)
        // INSERT 결과가 0이면 잔액 부족 (다른 트랜잭션이 먼저 차감한 경우 포함)
        int result = pointMapper.insertPointHistIfSufficient(
                PointHistDto.builder()
                        .userId(userId)
                        .histType(PointHistType.USE)
                        .changeAmt(-amount)
                        .ordSn(ordSn)
                        .memo(memo)
                        .assetType(assetType)
                        .build(),
                assetType);
        if (result == 0) {
            throw new FinalProjectException(ErrorCode.POINT_INSUFFICIENT_BALANCE);
        }

        log.info("포인트 사용: userId={}, assetType={}, amount={}", userId, assetType, amount);
    }

    @Override
    public long getPointBalance(String userId, AssetType assetType) {
        Long balance = pointMapper.selectPointBalance(userId, assetType);
        return balance != null ? balance : 0L;
    }

    @Override
    public List<PointHistDto> getPointHistory(String userId) {
        return pointMapper.selectPointHistByUserId(userId);
    }

    @Override
    public List<MemberCouponPointDto> getExpiringPoints(String userId) {
        return pointMapper.selectExpiringPoints(userId);
    }

    @Override
    @Transactional
    public void grantStudyPoint(String userId, long amount, String adminId, String memo) {
        if (userId == null || userId.isBlank()) {
            throw new FinalProjectException(ErrorCode.BAD_REQUEST);
        }
        if (adminId == null || adminId.isBlank()) {
            throw new FinalProjectException(ErrorCode.BAD_REQUEST);
        }
        // 대상 회원 존재 여부 확인
        memberMapper.findByUserId(userId)
                .orElseThrow(() -> new FinalProjectException(ErrorCode.POINT_ISSUE_USER_NOT_FOUND));

        if (amount <= 0) {
            throw new FinalProjectException(ErrorCode.POINT_INVALID_AMOUNT);
        }

        earnPoint(userId, AssetType.STUDY_POINT, amount, null,
                memo != null && !memo.isBlank() ? memo : "관리자 지급 (" + adminId + ")");
        log.info("스터디포인트 수동 지급: userId={}, amount={}, adminId={}", userId, amount, adminId);
    }

    @Override
    public List<MemberCouponPointDto> getAllPointGrants() {
        return pointMapper.selectAllPointGrants();
    }

    @Override
    public List<MemberCouponPointDto> searchStudyGrants(String q) {
        return pointMapper.searchStudyGrants(q);
    }

    @Override
    public List<PointHistDto> getAllPointHistoryByType(String userId, AssetType assetType) {
        return pointMapper.selectAllPointHistByUserAndType(userId, assetType);
    }

    @Override
    public PageResponse<PointHistDto> getPointHistoryByType(String userId, AssetType assetType, String startDate, String endDate, int page) {
        PaginationInfo<?> paginationInfo = new PaginationInfo<>(10, page);
        List<PointHistDto> items = pointMapper.selectPointHistByUserAndType(userId, assetType, startDate, endDate, paginationInfo);
        int totalCount = pointMapper.selectPointHistCountByUserAndType(userId, assetType, startDate, endDate);
        return new PageResponse<>(items, totalCount);
    }

    @Override
    public String getUserName(String userId) {
        return pointMapper.selectUserNameById(userId);
    }

    // ── 공통 검증 ──────────────────────────────────────────

    private void validatePointType(AssetType assetType) {
        if (assetType == null || assetType == AssetType.COUPON) {
            throw new FinalProjectException(ErrorCode.POINT_INVALID_TYPE);
        }
    }

    private void validateAmount(long amount) {
        if (amount <= 0) {
            throw new FinalProjectException(ErrorCode.POINT_INVALID_AMOUNT);
        }
    }
}
