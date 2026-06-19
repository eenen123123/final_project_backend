package kr.or.ddit.controller.manager;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.dto.retention.RetentionAttendanceDto;
import kr.or.ddit.finalProject.dto.retention.RetentionProcessDto;
import kr.or.ddit.finalProject.paging.PaginationInfo;
import kr.or.ddit.service.RetentionService;
import kr.or.ddit.service.impl.RetentionAttendanceUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 퇴원 방어 및 유지 관리 데이터 API
 *  · 화면(GET /admin/retention)은 {@link ManagerController}가 담당
 *  · 대상: 오프라인 학생(ROLE_STUDENT), 검색·페이징 서버사이드, select 옵션은 공통코드
 *  · 학생 검색/수강 강좌(보기)는 상담관리 엔드포인트(/admin/consultation/*) 재사용
 *
 * - GET  /admin/retention/summary             : 상단 요약 카드
 * - GET  /admin/retention/anomalies           : 근태 특이사항 집계 (페이징)
 * - GET  /admin/retention/processes           : 상담 프로세스 이력 (페이징)
 * - GET  /admin/retention/calendar            : 상담 프로세스 캘린더 (기간)
 * - POST /admin/retention/processes           : 상담 프로세스 저장
 * - POST /admin/retention/attendance/upload   : 근태 Excel 업로드
 */
@Slf4j
@Controller
@RequestMapping("/admin/retention")
@RequiredArgsConstructor
public class ManagerRetentionController {

    private final RetentionService retentionService;
    private final RetentionAttendanceUploadService uploadService;

    private static final int BLOCK_SIZE = 5;

    /** 상단 요약 카드 */
    @GetMapping("/summary")
    @ResponseBody
    public ResponseEntity<?> summary() {
        return ResponseEntity.ok(retentionService.getSummary());
    }

    /** 근태 특이사항 집계 (검색·페이징) */
    @GetMapping("/anomalies")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> anomalies(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String atndTypeCd,
            @RequestParam(required = false) String riskLevel,
            @RequestParam(defaultValue = "1")  int page,
            @RequestParam(defaultValue = "10") int screenSize) {

        Map<String, Object> cond = new HashMap<>();
        putIfText(cond, "keyword",    keyword);
        putIfText(cond, "atndTypeCd", atndTypeCd);
        putIfText(cond, "riskLevel",  riskLevel);

        PaginationInfo<Map<String, Object>> paging = new PaginationInfo<>(screenSize, BLOCK_SIZE, page);
        paging.setDetailCondition(cond);

        PageResponse<RetentionAttendanceDto> resp = retentionService.searchAnomalies(paging);
        return ResponseEntity.ok(toResponse(resp, paging));
    }

    /** 상담 프로세스 이력 (검색·페이징) */
    @GetMapping("/processes")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> processes(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String result,
            @RequestParam(defaultValue = "1")  int page,
            @RequestParam(defaultValue = "10") int screenSize) {

        Map<String, Object> cond = new HashMap<>();
        putIfText(cond, "keyword", keyword);
        putIfText(cond, "result",  result);

        PaginationInfo<Map<String, Object>> paging = new PaginationInfo<>(screenSize, BLOCK_SIZE, page);
        paging.setDetailCondition(cond);

        PageResponse<RetentionProcessDto> resp = retentionService.searchProcesses(paging);
        return ResponseEntity.ok(toResponse(resp, paging));
    }

    /** 상담 프로세스 캘린더 (기간 조회) */
    @GetMapping("/calendar")
    @ResponseBody
    public ResponseEntity<?> calendar(@RequestParam String start, @RequestParam String end) {
        return ResponseEntity.ok(retentionService.getProcessCalendar(start, end));
    }

    /** 상담 프로세스 저장 */
    @PostMapping("/processes")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> saveProcess(
            @RequestParam String stdUserId,
            @RequestParam(required = false) String wdrwRsnCd,
            @RequestParam(required = false) String rtnpCn,
            @RequestParam(required = false) String rtnpRsltCd,
            @RequestParam(required = false) String rtnpDt,
            Principal principal) {

        Map<String, Object> result = new HashMap<>();
        if (stdUserId == null || stdUserId.isBlank()) {
            result.put("error", "학생을 선택해 주세요.");
            return ResponseEntity.badRequest().body(result);
        }
        if (rtnpCn == null || rtnpCn.isBlank()) {
            result.put("error", "상담 내용을 입력해 주세요.");
            return ResponseEntity.badRequest().body(result);
        }

        String loginId = principal != null ? principal.getName() : "SYSTEM";

        RetentionProcessDto dto = new RetentionProcessDto();
        dto.setStdUserId(stdUserId.trim());
        dto.setWdrwRsnCd(blankToNull(wdrwRsnCd));
        dto.setRtnpCn(rtnpCn.trim());
        dto.setRtnpRsltCd(orDefault(rtnpRsltCd, "01")); // 미지정 시 진행중
        dto.setRtnpDt(parseDateTime(rtnpDt));           // 미지정 시 null → 매퍼에서 현재시각
        dto.setChrgUserId(loginId);
        dto.setRgtrId(loginId);

        retentionService.saveProcess(dto);
        result.put("success", true);
        return ResponseEntity.ok(result);
    }

    /** 근태 Excel 업로드 적재 */
    @PostMapping("/attendance/upload")
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

    // ───────────────────────────── helpers ─────────────────────────────

    /** datetime-local("yyyy-MM-dd'T'HH:mm") 또는 "yyyy-MM-dd" 파싱, 없으면 null */
    private LocalDateTime parseDateTime(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String v = raw.trim().replace(' ', 'T');
        if (v.length() == 10) v = v + "T00:00:00"; // 날짜만 입력 시
        else if (v.length() == 16) v = v + ":00";  // 초 보정
        return LocalDateTime.parse(v);
    }

    private String blankToNull(String v) {
        return (v == null || v.isBlank()) ? null : v.trim();
    }

    private String orDefault(String v, String def) {
        return (v == null || v.isBlank()) ? def : v.trim();
    }

    private void putIfText(Map<String, Object> map, String key, String val) {
        if (val != null && !val.isBlank()) map.put(key, val.trim());
    }

    private <T> Map<String, Object> toResponse(PageResponse<T> resp, PaginationInfo<?> paging) {
        Map<String, Object> result = new HashMap<>();
        result.put("items",      resp.getItems());
        result.put("totalCount", resp.getTotalCount());
        result.put("page",       paging.getPage());
        result.put("totalPage",  (resp.getTotalCount() + paging.getScreenSize() - 1) / paging.getScreenSize());
        return result;
    }
}
