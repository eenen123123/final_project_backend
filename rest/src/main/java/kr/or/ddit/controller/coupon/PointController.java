package kr.or.ddit.controller.coupon;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kr.or.ddit.finalProject.dto.coupon.AssetType;
import kr.or.ddit.finalProject.dto.coupon.MemberCouponPointDto;
import kr.or.ddit.finalProject.dto.coupon.PointHistDto;
import kr.or.ddit.finalProject.service.coupon.PointService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    // GET /api/points/balance?assetType=HM_POINT - 포인트 잔액 조회
    @GetMapping("/balance")
    public ResponseEntity<Long> getBalance(Authentication authentication,
            @RequestParam AssetType assetType) {
        long balance = pointService.getPointBalance(authentication.getName(), assetType);
        return ResponseEntity.ok(balance);
    }

    // GET /api/points/history - 포인트 이력 조회
    @GetMapping("/history")
    public ResponseEntity<List<PointHistDto>> getHistory(Authentication authentication) {
        return ResponseEntity.ok(pointService.getPointHistory(authentication.getName()));
    }

    // GET /api/points/expiring - 소멸 예정 포인트 조회
    @GetMapping("/expiring")
    public ResponseEntity<List<MemberCouponPointDto>> getExpiring(Authentication authentication) {
        return ResponseEntity.ok(pointService.getExpiringPoints(authentication.getName()));
    }

}
