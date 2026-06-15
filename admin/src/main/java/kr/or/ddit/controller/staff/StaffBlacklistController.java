package kr.or.ddit.controller.staff;

import java.security.Principal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.dto.student.StudentBlackListDto;
import kr.or.ddit.finalProject.paging.PaginationInfo;
import kr.or.ddit.finalProject.service.staff.BlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 주의 학생(블랙리스트) 관리 — AJAX 엔드포인트.
 *
 * 페이지 진입 라우트(/admin/blacklist)는 StaffController 가 담당하고,
 * 이 컨트롤러는 목록(동적검색+서버페이징)·요약·상세·등록/수정/해제를 처리한다.
 */
@Slf4j
@Controller
@RequestMapping("/admin/blacklist")
@RequiredArgsConstructor
public class StaffBlacklistController {

    private final BlacklistService blacklistService;

    private static final int BLOCK_SIZE = 5;

    /** 목록 동적 검색 + 서버 페이징 */
    @GetMapping("/search")
    @ResponseBody
    public ResponseEntity<PageResponse<StudentBlackListDto>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1")  int page,
            @RequestParam(defaultValue = "10") int screenSize,
            @RequestParam(required = false) String orderBy,
            @RequestParam(required = false) String orderDirection) {

        Map<String, Object> cond = new HashMap<>();
        if (keyword  != null && !keyword.isBlank())  cond.put("keyword",  keyword.trim());
        if (level    != null && !level.isBlank())    cond.put("level",    level.trim());
        if (category != null && !category.isBlank()) cond.put("category", category.trim());
        if (status   != null && !status.isBlank())   cond.put("status",   status.trim());

        String safeDir = "ASC".equalsIgnoreCase(orderDirection) ? "ASC" : "DESC";
        PaginationInfo<Map<String, Object>> paging =
                new PaginationInfo<>(screenSize, BLOCK_SIZE, page, orderBy, safeDir);
        paging.setDetailCondition(cond);

        return ResponseEntity.ok(blacklistService.searchBlacklist(paging));
    }

    /** 상단 요약 카드 */
    @GetMapping("/summary")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> summary() {
        return ResponseEntity.ok(blacklistService.getSummary());
    }

    /** 학생 수강중 클래스룸명 (등록 picker 표시용) */
    @GetMapping("/classroom")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> classroom(@RequestParam String userId) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("classNm", blacklistService.getStudentClassNames(userId));
        return ResponseEntity.ok(body);
    }

    /** 단일 상세 (현재 상태 + 변경 이력) */
    @GetMapping("/detail")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> detail(@RequestParam String stdUserId) {
        StudentBlackListDto info = blacklistService.getDetail(stdUserId);
        if (info == null) return ResponseEntity.notFound().build();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("info", info);
        body.put("logs", blacklistService.getHistory(stdUserId));
        return ResponseEntity.ok(body);
    }

    /** 위반 등록 (누적 횟수 기반 자동 에스컬레이션) */
    @PostMapping
    @ResponseBody
    public ResponseEntity<Map<String, Object>> register(@RequestBody StudentBlackListDto dto, Principal principal) {
        return ResponseEntity.ok(blacklistService.registerBlacklist(dto, loginId(principal)));
    }

    /** 수정 */
    @PutMapping("/{stdUserId}")
    @ResponseBody
    public ResponseEntity<Void> update(@PathVariable String stdUserId,
                                       @RequestBody StudentBlackListDto dto,
                                       Principal principal) {
        dto.setStdUserId(stdUserId);
        blacklistService.updateBlacklist(dto, loginId(principal));
        return ResponseEntity.ok().build();
    }

    /** 해제 */
    @PutMapping("/{stdUserId}/resolve")
    @ResponseBody
    public ResponseEntity<Void> resolve(@PathVariable String stdUserId, Principal principal) {
        blacklistService.resolveBlacklist(stdUserId, loginId(principal));
        return ResponseEntity.ok().build();
    }

    private String loginId(Principal principal) {
        return principal != null ? principal.getName() : "SYSTEM";
    }
}
