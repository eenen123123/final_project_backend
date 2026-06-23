package kr.or.ddit.controller.principal;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.dto.finance.FinanceTxnDto;
import kr.or.ddit.finalProject.paging.PaginationInfo;
import kr.or.ddit.service.FinanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 매출 및 재무 분석 (원장 전용 · 수입 중심)
 * - GET /admin/finance/summary      : 요약 카드 (이번 달/전월/증감률/카테고리)
 * - GET /admin/finance/monthly      : 월별 매출 추이 (12개월)
 * - GET /admin/finance/transactions : 수입 거래 내역 (서버사이드 페이징)
 *
 * ※ 원비 수납(TUITION_PAYMENT 완료) + 온라인 결제(ORDERS PAID) 합산
 */
@Slf4j
@Controller
@RequestMapping("/admin/finance")
@RequiredArgsConstructor
public class FinanceController {

    private final FinanceService financeService;

    private static final int BLOCK_SIZE = 5;

    /** 요약 카드 */
    @GetMapping("/summary")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> summary(@RequestParam(required = false) String ym) {
        return ResponseEntity.ok(financeService.getSummary(orThisMonth(ym)));
    }

    /** 월별 매출 추이 */
    @GetMapping("/monthly")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> monthly(@RequestParam(required = false) String year) {
        String y = (year != null && !year.isBlank()) ? year.trim()
                : String.valueOf(LocalDate.now().getYear());
        Map<String, Object> result = new HashMap<>();
        result.put("items", financeService.getMonthlySales(y));
        return ResponseEntity.ok(result);
    }

    /** 수입 거래 내역 */
    @GetMapping("/transactions")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> transactions(
            @RequestParam(required = false) String ym,
            @RequestParam(required = false) String payMthdCd,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int screenSize) {

        Map<String, Object> cond = new HashMap<>();
        cond.put("ym", orThisMonth(ym));
        putIfText(cond, "payMthdCd", payMthdCd);

        PaginationInfo<Map<String, Object>> paging = new PaginationInfo<>(screenSize, BLOCK_SIZE, page);
        paging.setDetailCondition(cond);

        PageResponse<FinanceTxnDto> resp = financeService.getTransactions(paging);

        paging.setDetailCondition(null);
        Map<String, Object> result = new HashMap<>();
        result.put("items", resp.getItems());
        result.put("totalCount", resp.getTotalCount());
        result.put("page", paging.getPage());
        return ResponseEntity.ok(result);
    }

    // ───────────────────────────── helpers ─────────────────────────────

    private String orThisMonth(String ym) {
        return (ym != null && !ym.isBlank()) ? ym.trim() : YearMonth.now().toString();
    }

    private void putIfText(Map<String, Object> map, String key, String val) {
        if (val != null && !val.isBlank()) map.put(key, val.trim());
    }
}
