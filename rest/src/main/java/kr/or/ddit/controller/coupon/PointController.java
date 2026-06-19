package kr.or.ddit.controller.coupon;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kr.or.ddit.finalProject.dto.common.PageResponse;
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

    // GET /api/points/history?assetType=HM_POINT&startDate=&endDate=&page=1
    @GetMapping("/history")
    public ResponseEntity<PageResponse<PointHistDto>> getHistory(Authentication authentication,
            @RequestParam AssetType assetType,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") int page) {
        return ResponseEntity.ok(pointService.getPointHistoryByType(authentication.getName(), assetType, startDate, endDate, page));
    }

    // GET /api/points/expiring?assetType=HM_POINT - 소멸 예정 포인트 잔액 합계
    @GetMapping("/expiring")
    public ResponseEntity<Long> getExpiring(Authentication authentication,
            @RequestParam AssetType assetType) {
        List<MemberCouponPointDto> expiring = pointService.getExpiringPoints(authentication.getName());
        long total = expiring.stream()
                .filter(p -> p.getAssetType() == assetType && p.getPointAmt() != null)
                .mapToLong(MemberCouponPointDto::getPointAmt)
                .sum();
        return ResponseEntity.ok(total);
    }

    // GET /api/points/expiring/items?assetType=HM_POINT - 소멸 예정 포인트 목록 (모달용)
    @GetMapping("/expiring/items")
    public ResponseEntity<List<MemberCouponPointDto>> getExpiringItems(Authentication authentication,
            @RequestParam AssetType assetType) {
        List<MemberCouponPointDto> expiring = pointService.getExpiringPoints(authentication.getName());
        List<MemberCouponPointDto> filtered = expiring.stream()
                .filter(p -> p.getAssetType() == assetType)
                .toList();
        return ResponseEntity.ok(filtered);
    }

}
