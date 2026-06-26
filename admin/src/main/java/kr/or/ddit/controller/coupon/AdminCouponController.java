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
import kr.or.ddit.finalProject.dto.coupon.MemberCouponPointDto;
import kr.or.ddit.finalProject.dto.member.MemberDto;
import kr.or.ddit.finalProject.mapper.MemberMapper;
import kr.or.ddit.finalProject.service.coupon.CouponService;
import kr.or.ddit.finalProject.service.coupon.PointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/admin/coupon")
@RequiredArgsConstructor
public class AdminCouponController {

    private final CouponService couponService;
    private final PointService pointService;
    private final MemberMapper memberMapper;
    private final ObjectMapper objectMapper;

    // GET /admin/coupon - 쿠폰 목록 페이지
    @GetMapping
    public String couponList(Model model) throws Exception {
        List<MemberCouponPointDto> issuedCoupons = couponService.getAllIssuedCoupons();
        model.addAttribute("coupons", couponService.getAllCoupons());
        model.addAttribute("issuedCoupons", issuedCoupons);

        List<Map<String, Object>> issuedForJs = issuedCoupons.stream().map(uc -> {
            Map<String, Object> m = new HashMap<>();
            m.put("couponSn", uc.getCouponSn());
            m.put("couponNm", uc.getCouponNm());
            m.put("userId", uc.getUserId());
            m.put("userName", uc.getUserName() != null ? uc.getUserName() : "");
            m.put("useYn", uc.getUseYn() != null ? uc.getUseYn().name() : "N");
            m.put("issueDt", uc.getIssueDt() != null ? uc.getIssueDt().toString() : "");
            m.put("expiryDt", uc.getExpiryDt() != null ? uc.getExpiryDt().toString() : "");
            return m;
        }).collect(Collectors.toList());
        model.addAttribute("issuedCouponsJson", objectMapper.writeValueAsString(issuedForJs));

        List<MemberCouponPointDto> pointGrants = pointService.getAllPointGrants();
        model.addAttribute("pointGrants", pointGrants);
        model.addAttribute("pointGrantsJson", objectMapper.writeValueAsString(pointGrants));

        model.addAttribute("studyPointDefs", couponService.getAllStudyPointDefs());

        return "admin:/coupon/coupon_list";
    }

    // GET /admin/coupon/list - 쿠폰 목록 JSON (비동기 테이블용)
    @GetMapping("/list")
    @ResponseBody
    public ResponseEntity<List<CouponDto>> getCouponListJson() {
        return ResponseEntity.ok(couponService.getAllCoupons());
    }

    // GET /admin/coupon/insert - 쿠폰 등록 페이지 (레거시, 팝업으로 대체됨)
    @GetMapping("/insert")
    public String couponInsertForm() {
        return "admin:/coupon/coupon_insert";
    }

    // GET /admin/coupon/recipients-popup - 쿠폰 수신자 내역 팝업
    @GetMapping("/recipients-popup")
    public String couponRecipientsPopup() {
        return "coupon/coupon_recipients_popup";
    }

    // GET /admin/coupon/study-point/recipients-popup - 스터디포인트 수신자 내역 팝업
    @GetMapping("/study-point/recipients-popup")
    public String studyRecipientsPopup() {
        return "coupon/study_recipients_popup";
    }

    // GET /admin/coupon/insert-popup - 쿠폰 등록 팝업
    @GetMapping("/insert-popup")
    public String couponInsertPopup() {
        return "coupon/coupon_insert_popup";
    }

    // GET /admin/coupon/study-point/insert-popup - 스터디포인트 등록 팝업
    @GetMapping("/study-point/insert-popup")
    public String studyPointInsertPopup() {
        return "coupon/study_point_popup";
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
    public ResponseEntity<MemberCouponPointDto> issueCoupon(@PathVariable Long couponSn,
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        String userId = body.get("userId");
        MemberCouponPointDto issued = couponService.issueCoupon(couponSn, userId, authentication.getName());
        return ResponseEntity.ok(issued);
    }

    // POST /admin/coupon/{couponSn}/issue/bulk - 여러 유저에게 쿠폰 일괄 발급
    @PostMapping("/{couponSn}/issue/bulk")
    @ResponseBody
    public ResponseEntity<Map<String, Integer>> bulkIssueCoupon(@PathVariable Long couponSn,
            @RequestBody Map<String, List<String>> body,
            Authentication authentication) {
        List<String> userIds = body.get("userIds");
        List<MemberCouponPointDto> issued = couponService.bulkIssueCoupon(couponSn, userIds, authentication.getName());
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
    public String userSelectPopup(@RequestParam Long couponSn, @RequestParam String couponNm,
            @RequestParam(defaultValue = "coupon") String type, Model model) {
        model.addAttribute("couponSn", couponSn);
        model.addAttribute("couponNm", couponNm);
        model.addAttribute("issueType", type);
        return "coupon/coupon_issue_popup";
    }

    // GET /admin/coupon/popup/users/search - 학생/일반회원 검색 (AJAX)
    @GetMapping("/popup/users/search")
    @ResponseBody
    public ResponseEntity<List<MemberDto>> searchStudents(
            @RequestParam(defaultValue = "") String q) {
        return ResponseEntity.ok(memberMapper.searchStudentsForCoupon(q));
    }

    // ===================== 스터디포인트 정의 =====================

    // POST /admin/coupon/study-point - 스터디포인트 정의 등록
    @PostMapping("/study-point")
    @ResponseBody
    public ResponseEntity<CouponDto> insertStudyPointDef(@RequestBody CouponDto couponDto,
            Authentication authentication) {
        return ResponseEntity.ok(couponService.createStudyPointDef(couponDto, authentication.getName()));
    }

    // PUT /admin/coupon/study-point/{couponSn} - 스터디포인트 정의 수정
    @PutMapping("/study-point/{couponSn}")
    @ResponseBody
    public ResponseEntity<CouponDto> updateStudyPointDef(@PathVariable Long couponSn,
            @RequestBody CouponDto couponDto) {
        couponDto.setCouponSn(couponSn);
        return ResponseEntity.ok(couponService.updateStudyPointDef(couponDto));
    }

    // DELETE /admin/coupon/study-point/{couponSn} - 스터디포인트 정의 삭제
    @DeleteMapping("/study-point/{couponSn}")
    @ResponseBody
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteStudyPointDef(@PathVariable Long couponSn) {
        couponService.deleteStudyPointDef(couponSn);
    }

    // POST /admin/coupon/study-point/{couponSn}/issue - 스터디포인트 발급
    @PostMapping("/study-point/{couponSn}/issue")
    @ResponseBody
    public ResponseEntity<MemberCouponPointDto> issueStudyPoint(@PathVariable Long couponSn,
            @RequestBody Map<String, String> body, Authentication authentication) {
        return ResponseEntity.ok(couponService.issueStudyPoint(couponSn, body.get("userId"), authentication.getName()));
    }

    // POST /admin/coupon/study-point/{couponSn}/issue/bulk - 스터디포인트 일괄 발급
    @PostMapping("/study-point/{couponSn}/issue/bulk")
    @ResponseBody
    public ResponseEntity<Map<String, Integer>> bulkIssueStudyPoint(@PathVariable Long couponSn,
            @RequestBody Map<String, List<String>> body, Authentication authentication) {
        List<MemberCouponPointDto> issued = couponService.bulkIssueStudyPoint(couponSn, body.get("userIds"), authentication.getName());
        return ResponseEntity.ok(Map.of("count", issued.size()));
    }

    // GET /admin/coupon/study-point/grants?couponSn= - 발급 내역 조회 (couponSn 없으면 전체)
    @GetMapping("/study-point/grants")
    @ResponseBody
    public ResponseEntity<List<MemberCouponPointDto>> getStudyPointGrants(
            @RequestParam(required = false) Long couponSn) {
        return ResponseEntity.ok(couponService.getAllStudyPointGrants(couponSn));
    }

}
