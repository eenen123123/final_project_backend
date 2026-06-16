package kr.or.ddit.controller.coupon;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.or.ddit.finalProject.dto.coupon.CouponDto;
import kr.or.ddit.finalProject.dto.coupon.UserCouponDto;
import kr.or.ddit.finalProject.dto.member.MemberDto;
import kr.or.ddit.finalProject.mapper.MemberMapper;
import kr.or.ddit.finalProject.service.coupon.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/admin/coupon")
@RequiredArgsConstructor
public class AdminCouponController {

    private final CouponService couponService;
    private final MemberMapper memberMapper;
    private final ObjectMapper objectMapper;

    // GET /admin/coupon - 쿠폰 목록 페이지
    @GetMapping
    public String couponList(Model model) throws Exception {
        List<UserCouponDto> issuedCoupons = couponService.getAllIssuedCoupons();
        model.addAttribute("coupons", couponService.getAllCoupons());
        model.addAttribute("issuedCoupons", issuedCoupons);

        List<Map<String, Object>> issuedForJs = issuedCoupons.stream().map(uc -> {
            Map<String, Object> m = new HashMap<>();
            m.put("couponSn", uc.getCouponSn());
            m.put("couponNm", uc.getCouponNm());
            m.put("userId", uc.getUserId());
            m.put("userName", uc.getUserName() != null ? uc.getUserName() : "");
            m.put("useYn", uc.getUseYn() != null ? uc.getUseYn().name() : "N");
            m.put("expiryDt", uc.getExpiryDt() != null ? uc.getExpiryDt().toString() : "");
            return m;
        }).collect(Collectors.toList());
        model.addAttribute("issuedCouponsJson", objectMapper.writeValueAsString(issuedForJs));

        return "admin:/coupon/coupon_list";
    }

    // GET /admin/coupon/insert - 쿠폰 등록 페이지
    @GetMapping("/insert")
    public String couponInsertForm() {
        return "admin:/coupon/coupon_insert";
    }

    // POST /admin/coupon/insert - 쿠폰 등록 (AJAX)
    @PostMapping("/insert")
    @ResponseBody
    public ResponseEntity<CouponDto> insertCoupon(@RequestBody CouponDto couponDto,
            Authentication authentication) {
        CouponDto created = couponService.createCoupon(couponDto, authentication.getName());
        return ResponseEntity.ok(created);
    }

    // POST /admin/coupon/{couponSn}/issue - 유저에게 쿠폰 발급 (AJAX)
    @PostMapping("/{couponSn}/issue")
    @ResponseBody
    public ResponseEntity<UserCouponDto> issueCoupon(@PathVariable Long couponSn,
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        String userId = body.get("userId");
        UserCouponDto issued = couponService.issueCoupon(couponSn, userId, authentication.getName());
        return ResponseEntity.ok(issued);
    }

    // POST /admin/coupon/{couponSn}/issue/bulk - 여러 유저에게 쿠폰 일괄 발급
    @PostMapping("/{couponSn}/issue/bulk")
    @ResponseBody
    public ResponseEntity<Map<String, Integer>> bulkIssueCoupon(@PathVariable Long couponSn,
            @RequestBody Map<String, List<String>> body,
            Authentication authentication) {
        List<String> userIds = body.get("userIds");
        List<UserCouponDto> issued = couponService.bulkIssueCoupon(couponSn, userIds, authentication.getName());
        return ResponseEntity.ok(Map.of("count", issued.size()));
    }

    // PUT /admin/coupon/{couponSn} - 쿠폰 수정
    @PutMapping("/{couponSn}")
    @ResponseBody
    public ResponseEntity<CouponDto> updateCoupon(@PathVariable Long couponSn,
            @RequestBody CouponDto couponDto) {
        couponDto.setCouponSn(couponSn);
        CouponDto updated = couponService.updateCoupon(couponDto);
        return ResponseEntity.ok(updated);
    }

    // DELETE /admin/coupon/{couponSn} - 쿠폰 삭제
    @DeleteMapping("/{couponSn}")
    @ResponseBody
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCoupon(@PathVariable Long couponSn) {
        couponService.deleteCoupon(couponSn);
    }

    // GET /admin/coupon/popup/users - 유저 선택 팝업 페이지
    @GetMapping("/popup/users")
    public String userSelectPopup(@RequestParam Long couponSn, @RequestParam String couponNm, Model model) {
        model.addAttribute("couponSn", couponSn);
        model.addAttribute("couponNm", couponNm);
        return "coupon/coupon_user_popup";
    }

    // GET /admin/coupon/popup/users/search - 학생/일반회원 검색 (AJAX)
    @GetMapping("/popup/users/search")
    @ResponseBody
    public ResponseEntity<List<MemberDto>> searchStudents(
            @RequestParam(defaultValue = "") String q) {
        return ResponseEntity.ok(memberMapper.searchStudentsForCoupon(q));
    }

}
