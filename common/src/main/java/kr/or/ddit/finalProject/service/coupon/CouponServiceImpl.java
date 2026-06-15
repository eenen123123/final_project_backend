package kr.or.ddit.finalProject.service.coupon;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.ddit.finalProject.dto.coupon.CouponDto;
import kr.or.ddit.finalProject.dto.coupon.CouponUseStatus;
import kr.or.ddit.finalProject.dto.coupon.DiscType;
import kr.or.ddit.finalProject.dto.coupon.UserCouponDto;
import kr.or.ddit.finalProject.exception.ErrorCode;
import kr.or.ddit.finalProject.exception.FinalProjectException;
import kr.or.ddit.finalProject.mapper.coupon.CouponMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private static final String ACTIVE = "Y";

    private final CouponMapper couponMapper;

    @Override
    @Transactional
    public CouponDto createCoupon(CouponDto couponDto, String adminId) {
        if (couponDto.getCouponNm() == null || couponDto.getCouponNm().isBlank()) {
            throw new FinalProjectException(ErrorCode.BAD_REQUEST);
        }
        if (couponDto.getDiscType() == null) {
            throw new FinalProjectException(ErrorCode.BAD_REQUEST);
        }
        if (couponDto.getValidDays() == null || couponDto.getValidDays() <= 0) {
            throw new FinalProjectException(ErrorCode.BAD_REQUEST);
        }

        // discType에 따라 할인값 서버 검증 (클라이언트 값 신뢰 X)
        if (couponDto.getDiscType() == DiscType.FIXED) {
            if (couponDto.getDiscAmt() == null || couponDto.getDiscAmt() <= 0) {
                throw new FinalProjectException(ErrorCode.COUPON_INVALID_DISCOUNT);
            }
            couponDto.setDiscRate(0); // 정액이면 rate는 0으로 강제
        } else if (couponDto.getDiscType() == DiscType.RATE) {
            if (couponDto.getDiscRate() == null || couponDto.getDiscRate() < 1 || couponDto.getDiscRate() > 100) {
                throw new FinalProjectException(ErrorCode.COUPON_INVALID_DISCOUNT);
            }
            couponDto.setDiscAmt(0L); // 정률이면 amt는 0으로 강제
        }

        // 클라이언트가 보낸 useYn, regUserId 는 무시하고 서버에서 설정
        couponDto.setUseYn(ACTIVE);
        couponDto.setRegUserId(adminId);

        couponMapper.insertCoupon(couponDto);
        log.info("쿠폰 생성: couponSn={}, couponNm={}, adminId={}", couponDto.getCouponSn(), couponDto.getCouponNm(), adminId);
        return couponDto;
    }

    @Override
    public List<CouponDto> getAllCoupons() {
        return couponMapper.selectAllCoupons();
    }

    @Override
    @Transactional
    public UserCouponDto issueCoupon(Long couponSn, String userId, String adminId) {
        // 발급 대상 userId 검증
        if (userId == null || userId.isBlank()) {
            throw new FinalProjectException(ErrorCode.COUPON_ISSUE_TARGET_REQUIRED);
        }

        // 쿠폰 존재 여부 및 활성 여부 서버에서 재조회 (클라이언트 값 사용 X)
        CouponDto coupon = couponMapper.selectCouponBySn(couponSn);
        if (coupon == null) {
            throw new FinalProjectException(ErrorCode.COUPON_NOT_FOUND);
        }
        if (!ACTIVE.equals(coupon.getUseYn())) {
            throw new FinalProjectException(ErrorCode.COUPON_INACTIVE);
        }

        // 만료일은 서버에서 계산 (클라이언트 값 사용 X)
        LocalDate expiryDt = LocalDate.now().plusDays(coupon.getValidDays());

        UserCouponDto userCoupon = UserCouponDto.builder()
                .userId(userId)
                .couponSn(couponSn)
                .couponNm(coupon.getCouponNm())  // 발급 시점 스냅샷
                .expiryDt(expiryDt)
                .useYn(CouponUseStatus.N)
                .issuedBy(adminId)
                .build();

        couponMapper.insertUserCoupon(userCoupon);
        log.info("쿠폰 발급: userCouponSn={}, userId={}, couponNm={}, expiryDt={}",
                userCoupon.getUserCouponSn(), userId, coupon.getCouponNm(), expiryDt);
        return userCoupon;
    }

    @Override
    public List<UserCouponDto> getMyCoupons(String userId) {
        return couponMapper.selectUserCouponsByUserId(userId);
    }

    @Override
    public List<UserCouponDto> getExpiringCoupons(String userId) {
        return couponMapper.selectExpiringCoupons(userId);
    }

    @Override
    @Transactional
    public CouponDto updateCoupon(CouponDto couponDto) {
        if (couponDto.getCouponSn() == null) {
            throw new FinalProjectException(ErrorCode.BAD_REQUEST);
        }
        CouponDto existing = couponMapper.selectCouponBySn(couponDto.getCouponSn());
        if (existing == null) {
            throw new FinalProjectException(ErrorCode.COUPON_NOT_FOUND);
        }
        if (couponDto.getCouponNm() == null || couponDto.getCouponNm().isBlank()) {
            throw new FinalProjectException(ErrorCode.BAD_REQUEST);
        }
        if (couponDto.getDiscType() == null) {
            throw new FinalProjectException(ErrorCode.BAD_REQUEST);
        }
        if (couponDto.getValidDays() == null || couponDto.getValidDays() <= 0) {
            throw new FinalProjectException(ErrorCode.BAD_REQUEST);
        }
        if (couponDto.getDiscType() == DiscType.FIXED) {
            if (couponDto.getDiscAmt() == null || couponDto.getDiscAmt() <= 0) {
                throw new FinalProjectException(ErrorCode.COUPON_INVALID_DISCOUNT);
            }
            couponDto.setDiscRate(0);
        } else if (couponDto.getDiscType() == DiscType.RATE) {
            if (couponDto.getDiscRate() == null || couponDto.getDiscRate() < 1 || couponDto.getDiscRate() > 100) {
                throw new FinalProjectException(ErrorCode.COUPON_INVALID_DISCOUNT);
            }
            couponDto.setDiscAmt(0L);
        }
        // useYn은 클라이언트 값 그대로 허용 (관리자가 활성/비활성 변경 가능)
        if (couponDto.getUseYn() == null) {
            couponDto.setUseYn(existing.getUseYn());
        }
        couponMapper.updateCoupon(couponDto);
        log.info("쿠폰 수정: couponSn={}", couponDto.getCouponSn());
        return couponMapper.selectCouponBySn(couponDto.getCouponSn());
    }

    @Override
    @Transactional
    public void deleteCoupon(Long couponSn) {
        CouponDto existing = couponMapper.selectCouponBySn(couponSn);
        if (existing == null) {
            throw new FinalProjectException(ErrorCode.COUPON_NOT_FOUND);
        }
        int deleted = couponMapper.deleteCoupon(couponSn);
        if (deleted == 0) {
            throw new FinalProjectException(ErrorCode.COUPON_DELETE_FAILED);
        }
        log.info("쿠폰 삭제: couponSn={}", couponSn);
    }

    @Override
    @Transactional
    public List<UserCouponDto> bulkIssueCoupon(Long couponSn, List<String> userIds, String adminId) {
        if (userIds == null || userIds.isEmpty()) {
            throw new FinalProjectException(ErrorCode.COUPON_ISSUE_TARGET_REQUIRED);
        }
        CouponDto coupon = couponMapper.selectCouponBySn(couponSn);
        if (coupon == null) {
            throw new FinalProjectException(ErrorCode.COUPON_NOT_FOUND);
        }
        if (!ACTIVE.equals(coupon.getUseYn())) {
            throw new FinalProjectException(ErrorCode.COUPON_INACTIVE);
        }
        LocalDate expiryDt = LocalDate.now().plusDays(coupon.getValidDays());
        List<UserCouponDto> issued = new ArrayList<>();
        for (String userId : userIds) {
            if (userId == null || userId.isBlank()) continue;
            UserCouponDto userCoupon = UserCouponDto.builder()
                    .userId(userId)
                    .couponSn(couponSn)
                    .couponNm(coupon.getCouponNm())
                    .expiryDt(expiryDt)
                    .useYn(CouponUseStatus.N)
                    .issuedBy(adminId)
                    .build();
            couponMapper.insertUserCoupon(userCoupon);
            issued.add(userCoupon);
        }
        log.info("쿠폰 일괄 발급: couponSn={}, 대상 {}명, adminId={}", couponSn, issued.size(), adminId);
        return issued;
    }

}
