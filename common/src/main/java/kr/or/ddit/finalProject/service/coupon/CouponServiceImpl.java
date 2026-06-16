package kr.or.ddit.finalProject.service.coupon;

import java.security.SecureRandom;
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
    private static final String CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 15;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private String generateCouponCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CODE_CHARS.charAt(SECURE_RANDOM.nextInt(CODE_CHARS.length())));
        }
        return sb.toString();
    }

    private final CouponMapper couponMapper;

    @Override
    @Transactional
    public CouponDto createCoupon(CouponDto couponDto, String adminId) {
        // 필수 입력값 누락 체크
        if (couponDto.getCouponNm() == null || couponDto.getCouponNm().isBlank()) {
            throw new FinalProjectException(ErrorCode.BAD_REQUEST);
        }
        if (couponDto.getDiscType() == null) {
            throw new FinalProjectException(ErrorCode.BAD_REQUEST);
        }
        if (couponDto.getValidDays() == null || couponDto.getValidDays() <= 0) {
            throw new FinalProjectException(ErrorCode.BAD_REQUEST);
        }

        // 할인 방식에 맞는 값이 들어왔는지 서버에서 직접 검증
        // 정액: 할인 금액이 반드시 양수여야 함, 정률 필드는 0으로 초기화
        // 정률: 할인율이 1~100 사이여야 함, 정액 필드는 0으로 초기화
        // (클라이언트에서 두 필드를 동시에 보내도 서버가 강제로 정리)
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

        // 클라이언트가 보낸 useYn, regUserId는 무시하고 서버에서 덮어씀
        // (프론트에서 임의로 비활성/타인 ID를 넣어도 무효)
        couponDto.setUseYn(ACTIVE);
        couponDto.setRegUserId(adminId);
        couponDto.setCouponCode(generateCouponCode());

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
    public List<UserCouponDto> getAllIssuedCoupons() {
        return couponMapper.selectAllUserCoupons();
    }

    @Override
    @Transactional
    public UserCouponDto redeemCoupon(String couponCode, String userId) {
        // 코드로 쿠폰 조회
        CouponDto coupon = couponMapper.selectCouponByCode(couponCode);
        if (coupon == null) {
            throw new FinalProjectException(ErrorCode.COUPON_CODE_NOT_FOUND);
        }
        // 비활성 쿠폰 체크
        if (!ACTIVE.equals(coupon.getUseYn())) {
            throw new FinalProjectException(ErrorCode.COUPON_INACTIVE);
        }
        // 중복 발급 체크
        if (couponMapper.existsUserCoupon(userId, coupon.getCouponSn()) > 0) {
            throw new FinalProjectException(ErrorCode.COUPON_ALREADY_ISSUED);
        }

        LocalDate expiryDt = LocalDate.now().plusDays(coupon.getValidDays());
        UserCouponDto userCoupon = UserCouponDto.builder()
                .userId(userId)
                .couponSn(coupon.getCouponSn())
                .couponNm(coupon.getCouponNm())
                .expiryDt(expiryDt)
                .useYn(CouponUseStatus.N)
                .build();

        couponMapper.insertUserCoupon(userCoupon);
        log.info("쿠폰 코드 등록: userId={}, couponCode={}, couponNm={}", userId, couponCode, coupon.getCouponNm());
        return userCoupon;
    }

    @Override
    @Transactional
    public CouponDto updateCoupon(CouponDto couponDto) {
        // PK 누락 여부
        if (couponDto.getCouponSn() == null) {
            throw new FinalProjectException(ErrorCode.BAD_REQUEST);
        }
        // 수정 대상 쿠폰이 DB에 실제로 존재하는지
        CouponDto existing = couponMapper.selectCouponBySn(couponDto.getCouponSn());
        if (existing == null) {
            throw new FinalProjectException(ErrorCode.COUPON_NOT_FOUND);
        }
        // 필수 입력값 누락
        if (couponDto.getCouponNm() == null || couponDto.getCouponNm().isBlank()) {
            throw new FinalProjectException(ErrorCode.BAD_REQUEST);
        }
        if (couponDto.getDiscType() == null) {
            throw new FinalProjectException(ErrorCode.BAD_REQUEST);
        }
        if (couponDto.getValidDays() == null || couponDto.getValidDays() <= 0) {
            throw new FinalProjectException(ErrorCode.BAD_REQUEST);
        }
        // 할인 방식별 값 검증 + 반대편 필드 0 초기화 (createCoupon과 동일 규칙)
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
        // useYn 미전송 시 기존 값 유지 (관리자가 명시적으로 보낸 경우만 변경)
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
        // 삭제 대상이 존재하는지
        CouponDto existing = couponMapper.selectCouponBySn(couponSn);
        if (existing == null) {
            throw new FinalProjectException(ErrorCode.COUPON_NOT_FOUND);
        }
        // deleted == 0 이면 USER_COUPON 발급 이력 있는 것 -> 삭제 불가
        int deleted = couponMapper.deleteCoupon(couponSn);
        if (deleted == 0) {
            throw new FinalProjectException(ErrorCode.COUPON_DELETE_FAILED);
        }
        log.info("쿠폰 삭제: couponSn={}", couponSn);
    }

    @Override
    @Transactional
    public List<UserCouponDto> bulkIssueCoupon(Long couponSn, List<String> userIds, String adminId) {
        // 발급 대상 목록이 비어있는지
        if (userIds == null || userIds.isEmpty()) {
            throw new FinalProjectException(ErrorCode.COUPON_ISSUE_TARGET_REQUIRED);
        }
        // 쿠폰 존재 여부 + 활성 여부 (DB 재조회, 클라이언트 값 신뢰 X)
        CouponDto coupon = couponMapper.selectCouponBySn(couponSn);
        if (coupon == null) {
            throw new FinalProjectException(ErrorCode.COUPON_NOT_FOUND);
        }
        if (!ACTIVE.equals(coupon.getUseYn())) {
            throw new FinalProjectException(ErrorCode.COUPON_INACTIVE);
        }
        // 만료일 서버 계산, 루프마다 빈 userId 스킵
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
