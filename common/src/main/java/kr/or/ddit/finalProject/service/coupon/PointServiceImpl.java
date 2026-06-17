package kr.or.ddit.finalProject.service.coupon;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.ddit.finalProject.dto.coupon.AssetType;
import kr.or.ddit.finalProject.dto.coupon.CouponUseStatus;
import kr.or.ddit.finalProject.dto.coupon.MemberCouponPointDto;
import kr.or.ddit.finalProject.dto.coupon.PointHistDto;
import kr.or.ddit.finalProject.dto.coupon.PointHistType;
import kr.or.ddit.finalProject.exception.ErrorCode;
import kr.or.ddit.finalProject.exception.FinalProjectException;
import kr.or.ddit.finalProject.mapper.coupon.PointMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointServiceImpl implements PointService {

    private final PointMapper pointMapper;

    @Override
    @Transactional
    public void earnPoint(String userId, AssetType assetType, long amount, Long ordSn, String memo) {
        MemberCouponPointDto grant = MemberCouponPointDto.builder()
                .userId(userId)
                .assetType(assetType)
                .pointAmt(amount)
                .expiryDt(LocalDate.now().plusYears(1))
                .useYn(CouponUseStatus.N)
                .build();
        pointMapper.insertPointGrant(grant);

        PointHistDto hist = PointHistDto.builder()
                .mcpntSn(grant.getMcpntSn())
                .userId(userId)
                .histType(PointHistType.EARN)
                .changeAmt(amount)
                .ordSn(ordSn)
                .memo(memo)
                .build();
        pointMapper.insertPointHist(hist);

        log.info("포인트 적립: userId={}, assetType={}, amount={}", userId, assetType, amount);
    }

    @Override
    @Transactional
    public void usePoint(String userId, AssetType assetType, long amount, Long ordSn, String memo) {
        long balance = getPointBalance(userId, assetType);
        if (balance < amount) {
            throw new FinalProjectException(ErrorCode.BAD_REQUEST);
        }

        PointHistDto hist = PointHistDto.builder()
                .userId(userId)
                .histType(PointHistType.USE)
                .changeAmt(-amount)
                .ordSn(ordSn)
                .memo(memo)
                .build();
        pointMapper.insertPointHist(hist);

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
        if (amount <= 0) {
            throw new FinalProjectException(ErrorCode.BAD_REQUEST);
        }
        earnPoint(userId, AssetType.STUDY_POINT, amount, null, memo != null ? memo : "관리자 지급 (" + adminId + ")");
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
    public List<PointHistDto> getPointHistoryByType(String userId, AssetType assetType) {
        return pointMapper.selectPointHistByUserAndType(userId, assetType);
    }

    @Override
    public String getUserName(String userId) {
        return pointMapper.selectUserNameById(userId);
    }

}
