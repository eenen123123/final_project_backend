package kr.or.ddit.controller.principal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.dto.log.AdminAuditLogDto;
import kr.or.ddit.finalProject.dto.log.LoginLogDto;
import kr.or.ddit.finalProject.dto.log.MemberActivityLogDto;
import kr.or.ddit.finalProject.dto.log.MemberLoginLogDto;
import kr.or.ddit.finalProject.dto.log.SystemErrorLogDto;
import kr.or.ddit.finalProject.paging.PaginationInfo;
import kr.or.ddit.service.PrincipalSystemMonitoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * PrincipalSystemMonitoringController
 *
 * ✔ 로그 분석 및 시스템 모니터링 컨트롤러
 *
 * ✔ 역할 요약
 * ---------------------------------------------------------------------
 * - 모니터링 메인 대시보드 뷰로의 초기 진입 제어 및 요약 데이터 바인딩
 * - 관리자/사용자의 접속 이력 및 활동 감사 로그 조회 API 제공
 * - 시스템 내부 오류 로그 추적 API 제공
 * - 대량 로그 데이터의 안정적인 조회를 위한 페이덤 변수 가공 및 유효성 검증
 *
 * ✔ 설계 목적
 * ---------------------------------------------------------------------
 * 1. 보안 및 오류 모니터링 화면 로딩 시 UI 렌더링 속도 최적화를 위해 메인 뷰와 데이터 API를 분리
 * 2. 각 로그 유형별 동적 검색 조건을 하나의 페이징 공통 아키텍처(PaginationInfo)로 통일 및 정제
 * 3. 예외 상황 발생 시 완전한 시스템 크래시를 방지하기 위한 예외 방어 프레임 구축
 *
 * ✔ 아키텍처 위치 (Controller Layer - Dashboard / REST API Mixin)
 * ---------------------------------------------------------------------
 * [Browser / UI Component]
 * │
 * ├── 1. GET /admin/system/monitoring ➡️ (Forward View) ➡️ Thymeleaf HTML
 * │
 * └── 2. GET /admin/system/monitoring/* ➡️ (JSON REST API)
 * │
 * ▼
 * [PrincipalSystemMonitoringController] 🌟 (현재 위치)
 * │
 * ▼
 * [PrincipalSystemMonitoringService] ➡️ [Database (Log Tables)]
 * ---------------------------------------------------------------------
 */
@Slf4j
@Controller
@RequestMapping("/admin/system/monitoring")
@RequiredArgsConstructor
public class PrincipalSystemMonitoringController {

    private final PrincipalSystemMonitoringService monitoringService;

    private static final int BLOCK_SIZE = 5;

    /**
     * 모니터링 메인 화면 이동 및 대시보드 요약 통계 적재
     * ✔ 설계 포인트: 대량의 로그 목록을 첫 화면부터 가져오면 부하가 크므로, 초기 로딩 시에는 상단 요약 카드 통계 데이터만 바인딩하여 뷰를 반환한다.
     * @param model Thymeleaf로 대시보드 스냅샷 통계를 넘기기 위한 UI 모델 객체
     * @return 대시보드 메인 뷰 템플릿 경로
     */
    @GetMapping
    public String getSystemMonitoring(Model model) {
        // 1. 서비스로부터 실시간 대시보드 카운트 및 현황 통계 데이터를 조회하여 모델에 바인딩한다.
        model.addAttribute("summaryStats", monitoringService.getSummaryStats());

        // 2. 고유 레이아웃이 적용된 시스템 모니터링 메인 화면 HTML 경로를 반환한다.
        return "admin:/principal/principal_system_monitoring";
    }


    /**
     * 1. 관리자 접속 이력 비동기 조회 (REST API)
     * ✔ 사용 시나리오: 부정 접근 방지 및 인사 관리자들이 시스템 로그인 히스토리를 필터링하여 감시할 때 사용한다.
     * @param userId        검색하고자 하는 관리자 고유ID
     * @param fromDt        검색 시작일 자격 조건
     * @param toDt          검색 종료일 자격 조건
     * @param page          현재 요청된 페이지 번호
     * @param screenSize    한 페이지에 노출할 행(Row)의 개수
     * @return              JSON 포맷으로 패키징된 페이징 아이템 리스트 및 메타데이터
     */
    @GetMapping("/admin-access")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> searchAdminLoginLog(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String fromDt,
            @RequestParam(required = false) String toDt,
            @RequestParam(defaultValue = "1")  int page,
            @RequestParam(defaultValue = "10") int screenSize) {
        
        // 1. 요청 파라미터의 유효성을 검증하고 쿼리 맵에 적재한다.
        Map<String, Object> params = new HashMap<>();
        if (hasText(userId)) params.put("userId", userId.trim());
        if (hasText(fromDt)) params.put("fromDt", fromDt.trim());
        if (hasText(toDt))   params.put("toDt",   toDt.trim());

        // 2. 화면 크기, 페이징 블록 크기, 요청 페이지 정보를 기반으로 페이징 엔티티를 빌드한다.
        PaginationInfo<Map<String, Object>> paging =
                new PaginationInfo<>(screenSize, BLOCK_SIZE, page);
        paging.setDetailCondition(params);

        // 3. 서비스 계층에 페이징 스펙을 전달하여 최종 결과가 바인딩된 PageResponse 객체를 수령한다.
        PageResponse<LoginLogDto> resp = monitoringService.searchAdminLoginLog(paging);

        // 4. 표준 응답 구조 맵으로 변환하여 HTTP 200 OK 상태코드와 함께 JSON으로 반환한다.
        return ResponseEntity.ok(toResponse(resp));
    }

    /**
     * 2. 관리자 활동 감사 비동기 조회 (REST API)
     * ✔ 사용 시나리오: 관리자 권한을 가진 유저가 시스템 내부에서 어떤 API를 호출하고 어떤 상태 코드를 받았는지(CUD 행위 감사) 추적한다.
     */
    @GetMapping("/admin-audit")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> searchAdminAuditLog(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String httpMethod,
            @RequestParam(required = false) String statusCode,
            @RequestParam(defaultValue = "1")  int page,
            @RequestParam(defaultValue = "10") int screenSize) {

        Map<String, Object> params = new HashMap<>();
        if (hasText(keyword))    params.put("keyword",    keyword.trim());
        if (hasText(httpMethod)) params.put("httpMethod", httpMethod.trim());
        if (hasText(statusCode)) params.put("statusCode", statusCode.trim());

        PaginationInfo<Map<String, Object>> paging =
                new PaginationInfo<>(screenSize, BLOCK_SIZE, page);
        paging.setDetailCondition(params);

        // 비즈니스 로직을 구동하여 해당 필터에 부합하는 감사 로그 리스트를 확보한다.
        PageResponse<AdminAuditLogDto> resp = monitoringService.searchAdminAuditLog(paging);
        return ResponseEntity.ok(toResponse(resp));
    }

    /**
     * 3. 사용자 접속 이력 비동기 조회 (REST API)
     * ✔ 사용 시나리오: 일반 회원의 시스템 로그인 시도 성공 및 실패 여부를 감시한다.
     * ✔ 설계 포인트: 사용자 접속 이력은 트래픽이 많아 에러 발생 확률이 높으므로 예외 처리 블록을 통해 안정성을 확보한다.
     */
    @GetMapping("/member-access")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> searchMemberLoginLog(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String loginSuccessYn,
            @RequestParam(required = false) String fromDt,
            @RequestParam(defaultValue = "1")  int page,
            @RequestParam(defaultValue = "10") int screenSize) {

        Map<String, Object> params = new HashMap<>();
        if (hasText(userId))         params.put("userId",         userId.trim());
        if (hasText(loginSuccessYn)) params.put("loginSuccessYn", loginSuccessYn.trim());
        if (hasText(fromDt))         params.put("fromDt",         fromDt.trim());

        PaginationInfo<Map<String, Object>> paging =
                new PaginationInfo<>(screenSize, BLOCK_SIZE, page);
        paging.setDetailCondition(params);

        try {
            // 1. 일반 사용자 로그인 이력 조회 로직을 수행하고 결과를 반환한다.
            PageResponse<MemberLoginLogDto> resp = monitoringService.searchMemberLoginLog(paging);
            return ResponseEntity.ok(toResponse(resp));
        } catch (Exception e) {
            // 2. 조회 실패 시 에러 로그를 남기고 시스템 다운 대신 빈 결과 셋(Empty JSON)을 반환하여 UI 스크립트 에러를 방지한다.
            log.warn("사용자 접속 이력 조회 실패: {}", e.getMessage());
            return ResponseEntity.ok(emptyResponse());
        }
    }

    /**
     * 4. 사용자 활동 이력 비동기 조회 (REST API)
     * ✔ 사용 시나리오: 회원들이 게시판, 학습 콘텐츠, 시험 모듈 등에서 구체적으로 어떤 비즈니스 활동 유형을 기록했는지 추적한다.
     */
    @GetMapping("/member-activity")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> searchMemberActivityLog(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String activityType,
            @RequestParam(required = false) String fromDt,
            @RequestParam(defaultValue = "1")  int page,
            @RequestParam(defaultValue = "10") int screenSize) {

        Map<String, Object> params = new HashMap<>();
        if (hasText(userId))       params.put("userId",       userId.trim());
        if (hasText(activityType)) params.put("activityType", activityType.trim());
        if (hasText(fromDt))       params.put("fromDt",       fromDt.trim());

        PaginationInfo<Map<String, Object>> paging =
                new PaginationInfo<>(screenSize, BLOCK_SIZE, page);
        paging.setDetailCondition(params);

        try {
            // 1. 영속성 계층에 접근하여 멤버들의 개별 활동 히스토리를 스캔한다.
            PageResponse<MemberActivityLogDto> resp = monitoringService.searchMemberActivityLog(paging);
            return ResponseEntity.ok(toResponse(resp));
        } catch (Exception e) {
            // 2. 예외 발생 시 워닝 로그를 식별하고 안정적인 빈 구조 응답 페이로드를 전달한다.
            log.warn("사용자 활동 이력 조회 실패: {}", e.getMessage());
            return ResponseEntity.ok(emptyResponse());
        }
    }

    /**
     * 5. 시스템 에러 로그 비동기 조회 (REST API)
     * ✔ 사용 시나리오: 서버 내부에서 던져진 StackTrace 및 예외 경고 메시지들을 날짜와 키워드별로 스크리닝하여 시스템 안정도를 확인한다.
     */
    @GetMapping("/sys-error")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> searchSystemErrorLog(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String fromDt,
            @RequestParam(defaultValue = "1")  int page,
            @RequestParam(defaultValue = "10") int screenSize) {

        Map<String, Object> params = new HashMap<>();
        if (hasText(keyword)) params.put("keyword", keyword.trim());
        if (hasText(fromDt))  params.put("fromDt",  fromDt.trim());

        PaginationInfo<Map<String, Object>> paging =
                new PaginationInfo<>(screenSize, BLOCK_SIZE, page);
        paging.setDetailCondition(params);

        // 1. 수집된 시스템 내부 예외 발생 로그 전체 내역을 아우르는 리스트를 반환한다.
        PageResponse<SystemErrorLogDto> resp = monitoringService.searchSystemErrorLog(paging);
        return ResponseEntity.ok(toResponse(resp));
    }

    // ─────────────────────────────────────────────────────────────────
    // [Helpers] 공통 내부 도우미 함수군
    // ─────────────────────────────────────────────────────────────────

    /**
     * 전달된 문자열 객체의 텍스트 유효성 검사
     * @param s 검증 대상 문자열
     * @return  널이 아니고 공백 문자를 제외한 텍스트가 존재할 시 true 반환
     */
    private boolean hasText(String s) {
        return s != null && !s.isBlank();
    }

    /**
     * 공통 PageResponse 명세를 UI 컴포넌트(Datatable 등) 프로토콜 포맷에 맞춰 맵으로 래핑
     * @param resp  rest 서비스에서 반환된 원시 데이터 래퍼 객체
     * @return      items와 totalCount 키를 가지는 가공된 데이터 구조 맵
     */
    private <T> Map<String, Object> toResponse(PageResponse<T> resp) {
        Map<String, Object> result = new HashMap<>();
        result.put("items",      resp.getItems());
        result.put("totalCount", resp.getTotalCount());
        return result;
    }

    /**
     * 예외 처리 블록 작동 시 데이터 통신 규격을 맞추기 위한 빈 값 응답 처리
     * @return  빈 아이템 리스트와 카운트 0을 가진 디폴트 맵
     */
    private Map<String, Object> emptyResponse() {
        Map<String, Object> result = new HashMap<>();
        result.put("items",      Collections.emptyList());
        result.put("totalCount", 0);
        return result;
    }
}
