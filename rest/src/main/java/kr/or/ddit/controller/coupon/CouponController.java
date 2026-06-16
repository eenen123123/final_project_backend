package kr.or.ddit.controller.coupon;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kr.or.ddit.finalProject.dto.coupon.UserCouponDto;
import kr.or.ddit.finalProject.service.coupon.CouponService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    // GET /api/coupons/my - 내 쿠폰 목록
    @GetMapping("/my")
    public ResponseEntity<List<UserCouponDto>> getMyCoupons(Authentication authentication) {
        return ResponseEntity.ok(couponService.getMyCoupons(authentication.getName()));
    }

    // GET /api/coupons/my/expiring - 소멸 예정 쿠폰 (다음달 만료)
    @GetMapping("/my/expiring")
    public ResponseEntity<List<UserCouponDto>> getExpiringCoupons(Authentication authentication) {
        return ResponseEntity.ok(couponService.getExpiringCoupons(authentication.getName()));
    }

    // POST /api/coupons/redeem - 쿠폰 코드 입력으로 발급
    @PostMapping("/redeem")
    public ResponseEntity<UserCouponDto> redeemCoupon(Authentication authentication,
            @RequestBody Map<String, String> body) {
        UserCouponDto issued = couponService.redeemCoupon(body.get("couponCode"), authentication.getName());
        return ResponseEntity.ok(issued);
    }

}
