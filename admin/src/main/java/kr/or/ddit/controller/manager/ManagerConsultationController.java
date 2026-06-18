package kr.or.ddit.controller.manager;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.dto.consultation.ConsultationDto;
import kr.or.ddit.finalProject.dto.consultation.ConsultationStudentDto;
import kr.or.ddit.finalProject.paging.PaginationInfo;
import kr.or.ddit.service.ConsultationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 학부모/학생 상담 관리 데이터 API
 *  · 화면(GET /admin/consultation)은 {@link ManagerController}가 담당
 *  · 검색·페이징은 서버사이드 처리, select 옵션은 공통코드(211/212)로 관리
 *
 * - GET  /admin/consultation/summary          : 상단 요약 카드
 * - GET  /admin/consultation/list             : 전체 상담 이력 (서버사이드 페이징)
 * - GET  /admin/consultation/students         : 학생 모달 검색 (서버사이드 페이징)
 * - GET  /admin/consultation/students/{id}    : 학생 1인 정보 + 학부모 자동 매칭
 * - GET  /admin/consultation/student/{id}     : 학생별 상담 이력 (타임라인)
 * - GET  /admin/consultation/detail/{cnslSn}  : 상담 상세 (보기 모달)
 * - POST /admin/consultation/save             : 상담 기록 저장
 */
@Slf4j
@Controller
@RequestMapping("/admin/consultation")
@RequiredArgsConstructor
public class ManagerConsultationController {

    private final ConsultationService consultationService;

    private static final int BLOCK_SIZE = 5;

    /** 상단 요약 카드 */
    @GetMapping("/summary")
    @ResponseBody
    public ResponseEntity<?> summary() {
        return ResponseEntity.ok(consultationService.getSummary());
    }

    /** 전체 상담 이력 (검색·페이징) */
    @GetMapping("/list")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String cnslTypeCd,
            @RequestParam(required = false) String cnslStatCd,
            @RequestParam(required = false) String stdUserId,
            @RequestParam(defaultValue = "1")  int page,
            @RequestParam(defaultValue = "10") int screenSize) {

        Map<String, Object> cond = new HashMap<>();
        putIfText(cond, "keyword",    keyword);
        putIfText(cond, "cnslTypeCd", cnslTypeCd);
        putIfText(cond, "cnslStatCd", cnslStatCd);
        putIfText(cond, "stdUserId",  stdUserId);

        PaginationInfo<Map<String, Object>> paging = new PaginationInfo<>(screenSize, BLOCK_SIZE, page);
        paging.setDetailCondition(cond);

        PageResponse<ConsultationDto> resp = consultationService.searchConsultations(paging);
        return ResponseEntity.ok(toResponse(resp, paging));
    }

    /** 학생 모달 검색 (검색·페이징) */
    @GetMapping("/students")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> students(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1")  int page,
            @RequestParam(defaultValue = "8")  int screenSize) {

        Map<String, Object> cond = new HashMap<>();
        putIfText(cond, "keyword", keyword);

        PaginationInfo<Map<String, Object>> paging = new PaginationInfo<>(screenSize, BLOCK_SIZE, page);
        paging.setDetailCondition(cond);

        PageResponse<ConsultationStudentDto> resp = consultationService.searchStudents(paging);
        return ResponseEntity.ok(toResponse(resp, paging));
    }

    /** 학생 1인 정보 + 학부모 자동 매칭 */
    @GetMapping("/students/{stdUserId}")
    @ResponseBody
    public ResponseEntity<ConsultationStudentDto> studentInfo(@PathVariable String stdUserId) {
        ConsultationStudentDto info = consultationService.getStudentInfo(stdUserId);
        return info != null ? ResponseEntity.ok(info) : ResponseEntity.notFound().build();
    }

    /** 학생별 상담 이력 (타임라인) */
    @GetMapping("/student/{stdUserId}")
    @ResponseBody
    public ResponseEntity<?> studentHistory(@PathVariable String stdUserId) {
        return ResponseEntity.ok(consultationService.getByStudent(stdUserId));
    }

    /** 캘린더 표시용 (기간 조회 · FullCalendar 이벤트 피드) */
    @GetMapping("/calendar")
    @ResponseBody
    public ResponseEntity<?> calendar(@RequestParam String start, @RequestParam String end) {
        return ResponseEntity.ok(consultationService.getForCalendar(start, end));
    }

    /** 학생이 수강 중인 강좌 (상담 상세 모달) */
    @GetMapping("/student/{stdUserId}/courses")
    @ResponseBody
    public ResponseEntity<?> studentCourses(@PathVariable String stdUserId) {
        return ResponseEntity.ok(consultationService.getStudentCourses(stdUserId));
    }

    /** 상담 상세 (보기 모달) */
    @GetMapping("/detail/{cnslSn}")
    @ResponseBody
    public ResponseEntity<ConsultationDto> detail(@PathVariable Long cnslSn) {
        ConsultationDto dto = consultationService.getDetail(cnslSn);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    /** 상담 기록 저장 */
    @PostMapping("/save")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> save(
            @RequestParam(required = false) String stdUserId,
            @RequestParam(required = false) String cnslNm,
            @RequestParam(required = false) String cnslTelno,
            @RequestParam String cnslDt,
            @RequestParam(required = false) String cnslTypeCd,
            @RequestParam(required = false) String cnslStatCd,
            @RequestParam(required = false) String cnslCn,
            @RequestParam(required = false) String cnslSmry,
            @RequestParam(required = false) String fllwUpCn,
            Principal principal) {

        Map<String, Object> result = new HashMap<>();
        boolean isMember = stdUserId != null && !stdUserId.isBlank();
        // 재원생(stdUserId) 또는 신규 문의자(이름) 중 하나는 필수
        if (!isMember && (cnslNm == null || cnslNm.isBlank())) {
            result.put("error", "재원생을 선택하거나 신규 문의자 이름을 입력해 주세요.");
            return ResponseEntity.badRequest().body(result);
        }
        if (cnslDt == null || cnslDt.isBlank()) {
            result.put("error", "상담 일시를 입력해 주세요.");
            return ResponseEntity.badRequest().body(result);
        }
        if (cnslCn == null || cnslCn.isBlank()) {
            result.put("error", "상담 내용을 입력해 주세요.");
            return ResponseEntity.badRequest().body(result);
        }

        String loginId = principal != null ? principal.getName() : "SYSTEM";

        ConsultationDto dto = new ConsultationDto();
        dto.setStdUserId(isMember ? stdUserId.trim() : null);
        // 신규 문의자(비회원)는 이름/연락처를 직접 저장, 재원생은 MEMBER에서 조회되므로 null
        dto.setCnslNm(isMember ? null : blankToNull(cnslNm));
        dto.setCnslTelno(isMember ? null : blankToNull(cnslTelno));
        dto.setChrgUserId(loginId);                       // 담당 강사 = 작성한 로그인 사용자
        dto.setCnslTypeCd(blankToNull(cnslTypeCd));
        dto.setCnslStatCd(orDefault(cnslStatCd, "02"));   // 미지정 시 완료
        dto.setCnslDt(parseDateTime(cnslDt));
        dto.setCnslCn(cnslCn.trim());
        dto.setCnslSmry(blankToNull(cnslSmry));
        dto.setFllwUpCn(blankToNull(fllwUpCn));
        dto.setRgtrId(loginId);

        consultationService.saveConsultation(dto);
        result.put("success", true);
        return ResponseEntity.ok(result);
    }

    // ───────────────────────────── helpers ─────────────────────────────

    /** datetime-local("yyyy-MM-dd'T'HH:mm") 또는 "yyyy-MM-dd HH:mm" 파싱 */
    private LocalDateTime parseDateTime(String raw) {
        String v = raw.trim().replace(' ', 'T');
        if (v.length() == 16) v = v + ":00";   // 초 보정
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

    /** items + totalCount + 현재 페이지 응답 래핑 */
    private <T> Map<String, Object> toResponse(PageResponse<T> resp, PaginationInfo<?> paging) {
        Map<String, Object> result = new HashMap<>();
        result.put("items",      resp.getItems());
        result.put("totalCount", resp.getTotalCount());
        result.put("page",       paging.getPage());
        result.put("totalPage",  (resp.getTotalCount() + paging.getScreenSize() - 1) / paging.getScreenSize());
        return result;
    }
}
