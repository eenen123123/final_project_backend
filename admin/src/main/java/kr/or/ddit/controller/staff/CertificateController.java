package kr.or.ddit.controller.staff;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.or.ddit.finalProject.dto.certificate.CertificateIssueDto;
import kr.or.ddit.finalProject.dto.common.CommonCodeDto;
import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.paging.PaginationInfo;
import kr.or.ddit.service.CertificateService;
import kr.or.ddit.service.CommonCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 증명서 발급 컨트롤러
 * - 직원 셀프서비스 (공통업무) : /admin/certificate          → 전 직원 접근
 * - 행정직원 모니터링         : /admin/certificates/list      → 행정(D100) 전용
 *
 * ※ select box 옵션은 공통코드(228), 검색·페이징은 서버사이드 처리
 *   자동승인 셀프서비스 구조: 신청 즉시 자동발급, 본인이 직접 1회 출력
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateService certificateService;
    private final CommonCodeService commonCodeService;

    private static final int BLOCK_SIZE = 5;
    private static final String CL_CERT_TYPE = "228"; // 증명서 종류

    // ─────────────────────────── 직원 셀프서비스 ───────────────────────────

    /** 증명서 발급 화면 진입 (공통업무) */
    @GetMapping("/admin/certificate")
    public String certificatePage(Model model, Authentication authentication) {
        log.info("certificatePage() user={}", authentication.getName());
        model.addAttribute("certTypes", activeCodes(CL_CERT_TYPE));
        return "admin:/staff/certificate_issue";
    }

    /** 본인 발급 이력 (JSON · 서버사이드 페이징) */
    @GetMapping("/admin/certificate/list")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> myList(
            @RequestParam(required = false) String certTyCd,
            @RequestParam(required = false) String prnYn,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int screenSize,
            Authentication authentication) {

        Map<String, Object> cond = new HashMap<>();
        cond.put("userId", authentication.getName());
        putIfText(cond, "certTyCd", certTyCd);
        putIfText(cond, "prnYn", prnYn);

        PaginationInfo<Map<String, Object>> paging = new PaginationInfo<>(screenSize, BLOCK_SIZE, page);
        paging.setDetailCondition(cond);

        return ResponseEntity.ok(toResponse(certificateService.searchMyList(paging), paging));
    }

    /** 증명서 신청 → 즉시 자동발급 */
    @PostMapping("/admin/certificate/issue")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> issue(
            @RequestParam String certTyCd,
            @RequestParam(required = false) String issueRsn,
            @RequestParam(required = false) String issuePurps,
            Authentication authentication) {

        if (certTyCd == null || certTyCd.isBlank()) {
            return badRequest("증명서 종류를 선택하세요.");
        }

        CertificateIssueDto dto = new CertificateIssueDto();
        dto.setUserId(authentication.getName());
        dto.setCertTyCd(certTyCd.trim());
        dto.setIssueRsn(trimOrNull(issueRsn));
        dto.setIssuePurps(trimOrNull(issuePurps));

        Long certSn = certificateService.issue(dto);

        Map<String, Object> result = new HashMap<>();
        result.put("certSn", certSn);
        return ResponseEntity.ok(result);
    }

    /** 출력 처리 (1회 제한) → 출력용 증명서 데이터 반환 */
    @PostMapping("/admin/certificate/{certSn}/print")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> print(@PathVariable Long certSn,
            Authentication authentication) {
        try {
            CertificateIssueDto cert = certificateService.print(certSn, authentication.getName());
            Map<String, Object> result = new HashMap<>();
            result.put("cert", cert);
            return ResponseEntity.ok(result);
        } catch (IllegalStateException e) {
            return badRequest(e.getMessage());
        }
    }

    // ─────────────────────────── 행정직원 모니터링 ───────────────────────────

    /** 전체 발급 이력 (JSON · 서버사이드 페이징) */
    @GetMapping("/admin/certificates/list")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> allList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String certTyCd,
            @RequestParam(required = false) String prnYn,
            @RequestParam(required = false) String fromDt,
            @RequestParam(required = false) String toDt,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int screenSize) {

        Map<String, Object> cond = new HashMap<>();
        putIfText(cond, "keyword", keyword);
        putIfText(cond, "certTyCd", certTyCd);
        putIfText(cond, "prnYn", prnYn);
        putIfText(cond, "fromDt", fromDt);
        putIfText(cond, "toDt", toDt);

        PaginationInfo<Map<String, Object>> paging = new PaginationInfo<>(screenSize, BLOCK_SIZE, page);
        paging.setDetailCondition(cond);

        return ResponseEntity.ok(toResponse(certificateService.searchAllList(paging), paging));
    }

    // ───────────────────────────── helpers ─────────────────────────────

    private List<CommonCodeDto> activeCodes(String clCode) {
        return commonCodeService.getAllCodes(clCode).stream()
                .filter(c -> "Y".equals(c.getUseYn() == null ? "Y" : c.getUseYn().trim()))
                .collect(Collectors.toList());
    }

    private void putIfText(Map<String, Object> map, String key, String val) {
        if (val != null && !val.isBlank()) map.put(key, val.trim());
    }

    private String trimOrNull(String val) {
        return (val != null && !val.isBlank()) ? val.trim() : null;
    }

    private ResponseEntity<Map<String, Object>> badRequest(String message) {
        Map<String, Object> err = new HashMap<>();
        err.put("error", message);
        return ResponseEntity.badRequest().body(err);
    }

    private <T> Map<String, Object> toResponse(PageResponse<T> resp, PaginationInfo<?> paging) {
        paging.setDetailCondition(null);
        Map<String, Object> result = new HashMap<>();
        result.put("items", resp.getItems());
        result.put("totalCount", resp.getTotalCount());
        result.put("page", paging.getPage());
        return result;
    }
}
