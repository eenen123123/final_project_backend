package kr.or.ddit.controller.coupon;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.or.ddit.finalProject.dto.coupon.AssetType;
import kr.or.ddit.finalProject.dto.coupon.MemberCouponPointDto;
import kr.or.ddit.finalProject.dto.coupon.PointHistDto;
import kr.or.ddit.finalProject.mapper.coupon.PointMapper;
import kr.or.ddit.finalProject.service.coupon.PointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/admin/point")
@RequiredArgsConstructor
public class AdminPointController {

    private final PointService pointService;
    private final PointMapper pointMapper;

    // GET /admin/point/study/search?q= - 스터디포인트 지급 내역 검색
    @GetMapping("/study/search")
    @ResponseBody
    public ResponseEntity<List<MemberCouponPointDto>> searchStudyGrants(
            @RequestParam(defaultValue = "") String q) {
        return ResponseEntity.ok(pointService.searchStudyGrants(q));
    }

    // GET /admin/point/user?userId=&assetType= - 특정 유저 잔액 + 이력 조회
    @GetMapping("/user")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUserPointInfo(
            @RequestParam String userId,
            @RequestParam AssetType assetType) {
        long balance = pointService.getPointBalance(userId, assetType);
        List<PointHistDto> history = pointService.getPointHistoryByType(userId, assetType);
        String userName = pointService.getUserName(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("userName", userName);
        result.put("balance", balance);
        result.put("history", history);
        return ResponseEntity.ok(result);
    }

    // GET /admin/point/users?q=&assetType=&role= - 유저 목록 + 잔액 조회
    @GetMapping("/users")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> searchUsersWithBalance(
            @RequestParam(defaultValue = "") String q,
            @RequestParam AssetType assetType,
            @RequestParam(defaultValue = "all") String role) {
        return ResponseEntity.ok(pointMapper.searchUsersWithBalance(q, assetType, role));
    }

    // GET /admin/point/user/history?userId=&assetType= - 특정 유저 이력만 조회
    @GetMapping("/user/history")
    @ResponseBody
    public ResponseEntity<List<PointHistDto>> getUserPointHistory(
            @RequestParam String userId,
            @RequestParam AssetType assetType) {
        return ResponseEntity.ok(pointService.getPointHistoryByType(userId, assetType));
    }

    // POST /admin/point/grant - 스터디포인트 수동 지급
    @PostMapping("/grant")
    @ResponseBody
    public ResponseEntity<Void> grantStudyPoint(@RequestBody Map<String, Object> body,
            Authentication authentication) {
        String userId = (String) body.get("userId");
        long amount = Long.parseLong(body.get("amount").toString());
        String memo = (String) body.get("memo");
        pointService.grantStudyPoint(userId, amount, authentication.getName(), memo);
        return ResponseEntity.ok().build();
    }

}
