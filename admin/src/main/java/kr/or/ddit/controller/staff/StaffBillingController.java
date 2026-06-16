package kr.or.ddit.controller.staff;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import kr.or.ddit.finalProject.dto.common.CommonCodeDto;
import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.dto.tuition.TuitionPaymentDto;
import kr.or.ddit.finalProject.dto.tuition.TuitionUnpaidDto;
import kr.or.ddit.finalProject.paging.PaginationInfo;
import kr.or.ddit.service.CommonCodeService;
import kr.or.ddit.service.StaffBillingService;
import kr.or.ddit.service.impl.StaffBillingUploadService;
import kr.or.ddit.service.impl.TuitionBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 원비 및 수납 관리 컨트롤러
 * - GET /admin/billing            : 화면 진입 (공통코드 select·오늘 요약 적재)
 * - GET /admin/billing/summary    : 오늘 수납 현황 카드 (JSON)
 * - GET /admin/billing/receipts   : 일일 수납 내역 (JSON · 서버사이드 페이징)
 * - GET /admin/billing/history    : 수납 이력 조회 (JSON · 서버사이드 페이징)
 * - GET /admin/billing/unpaid     : 미납자 관리 (JSON · 서버사이드 페이징)
 *
 * ※ 모든 select box 옵션은 공통코드(COM_CD)로 관리, 검색·페이징은 서버사이드 처리
 */
@Slf4j
@Controller
@RequestMapping("/admin/billing")
@RequiredArgsConstructor
public class StaffBillingController {

    private final StaffBillingService billingService;
    private final CommonCodeService commonCodeService;
    private final StaffBillingUploadService uploadService;
    private final TuitionBatchService batchService;

    private static final int BLOCK_SIZE = 5;

    // 공통코드 분류
    private static final String CL_ITEM   = "222"; // 원비 항목
    private static final String CL_METHOD = "223"; // 결제수단
    private static final String CL_PSTAT  = "225"; // 결제 상태

    /**
     * 원비 및 수납 관리 화면 진입
     * 공통코드 기반 select 옵션과 오늘 수납 현황 요약을 모델에 적재한다.
     */
    @GetMapping
    public String billingPage(Model model) {
        String today = LocalDate.now().toString();
        model.addAttribute("itemCodes",    activeCodes(CL_ITEM));
        model.addAttribute("methodCodes",  activeCodes(CL_METHOD));
        model.addAttribute("payStatCodes", activeCodes(CL_PSTAT));
        model.addAttribute("summary",      billingService.getSummary(today));
        model.addAttribute("today",        today);
        return "admin:/staff/billing";
    }

    /** 오늘 수납 현황 카드 요약 */
    @GetMapping("/summary")
    @ResponseBody
    public ResponseEntity<?> summary(@RequestParam(required = false) String baseDate) {
        return ResponseEntity.ok(billingService.getSummary(orToday(baseDate)));
    }

    /** 일일 수납 내역 (특정 일자) */
    @GetMapping("/receipts")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> receipts(
            @RequestParam(required = false) String baseDate,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String payMthdCd,
            @RequestParam(required = false) String payStatCd,
            @RequestParam(defaultValue = "1")  int page,
            @RequestParam(defaultValue = "10") int screenSize) {

        Map<String, Object> cond = new HashMap<>();
        cond.put("baseDate", orToday(baseDate));
        putIfText(cond, "keyword",   keyword);
        putIfText(cond, "payMthdCd", payMthdCd);
        putIfText(cond, "payStatCd", payStatCd);

        PaginationInfo<Map<String, Object>> paging = new PaginationInfo<>(screenSize, BLOCK_SIZE, page);
        paging.setDetailCondition(cond);

        PageResponse<TuitionPaymentDto> resp = billingService.searchDailyReceipts(paging);
        return ResponseEntity.ok(toResponse(resp, paging));
    }

    /** 수납 이력 조회 (기간/검색) */
    @GetMapping("/history")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> history(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String payMthdCd,
            @RequestParam(required = false) String payItemCd,
            @RequestParam(required = false) String fromDt,
            @RequestParam(required = false) String toDt,
            @RequestParam(defaultValue = "1")  int page,
            @RequestParam(defaultValue = "10") int screenSize) {

        Map<String, Object> cond = new HashMap<>();
        putIfText(cond, "keyword",   keyword);
        putIfText(cond, "payMthdCd", payMthdCd);
        putIfText(cond, "payItemCd", payItemCd);
        putIfText(cond, "fromDt",    fromDt);
        putIfText(cond, "toDt",      toDt);

        PaginationInfo<Map<String, Object>> paging = new PaginationInfo<>(screenSize, BLOCK_SIZE, page);
        paging.setDetailCondition(cond);

        PageResponse<TuitionPaymentDto> resp = billingService.searchPaymentHistory(paging);
        return ResponseEntity.ok(toResponse(resp, paging));
    }

    /** 미납자 관리 */
    @GetMapping("/unpaid")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> unpaid(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String minOverdue,
            @RequestParam(defaultValue = "1")  int page,
            @RequestParam(defaultValue = "10") int screenSize) {

        Map<String, Object> cond = new HashMap<>();
        putIfText(cond, "keyword",    keyword);
        putIfText(cond, "minOverdue", minOverdue);

        PaginationInfo<Map<String, Object>> paging = new PaginationInfo<>(screenSize, BLOCK_SIZE, page);
        paging.setDetailCondition(cond);

        PageResponse<TuitionUnpaidDto> resp = billingService.searchUnpaid(paging);
        return ResponseEntity.ok(toResponse(resp, paging));
    }

    /** 외부 급여지급 프로그램 수납 Excel 업로드 적재 */
    @PostMapping("/upload")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) {
        try {
            return ResponseEntity.ok(uploadService.upload(file));
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    /** 원비 청구 배치 수동 실행 (테스트용) · 매일 01:00 자동 실행과 동일 */
    @GetMapping("/batch/run")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> runBatch() {
        return ResponseEntity.ok(batchService.runDailyBatch());
    }

    // ───────────────────────────── helpers ─────────────────────────────

    /** 사용중(Y) 공통코드만 반환 */
    private List<CommonCodeDto> activeCodes(String clCode) {
        return commonCodeService.getAllCodes(clCode).stream()
                .filter(c -> "Y".equals(c.getUseYn() == null ? "Y" : c.getUseYn().trim()))
                .collect(Collectors.toList());
    }

    private String orToday(String date) {
        return (date != null && !date.isBlank()) ? date.trim() : LocalDate.now().toString();
    }

    private void putIfText(Map<String, Object> map, String key, String val) {
        if (val != null && !val.isBlank()) map.put(key, val.trim());
    }

    /** items + totalCount + 페이징 메타(현재/총페이지·블록) 응답 래핑 */
    private <T> Map<String, Object> toResponse(PageResponse<T> resp, PaginationInfo<?> paging) {
        paging.setDetailCondition(null); // 응답에 조건 노출 방지
        Map<String, Object> result = new HashMap<>();
        result.put("items",      resp.getItems());
        result.put("totalCount", resp.getTotalCount());
        result.put("page",       paging.getPage());
        return result;
    }
}
