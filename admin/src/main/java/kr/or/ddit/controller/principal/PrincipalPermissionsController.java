package kr.or.ddit.controller.principal;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.dto.employee.EmployeeDetailDto;
import kr.or.ddit.finalProject.dto.log.LoginLogDto;
import kr.or.ddit.finalProject.paging.PaginationInfo;
import kr.or.ddit.finalProject.service.log.LoginLogService;
import kr.or.ddit.finalProject.service.staff.StaffService;
import kr.or.ddit.mapper.PrincipalSystemMonitoringMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class PrincipalPermissionsController {

    private final StaffService staffService;
    private final LoginLogService loginLogService;
    private final PrincipalSystemMonitoringMapper monitoringMapper;

    private static final int PERM_SCREEN_SIZE = 10;
    private static final int PERM_BLOCK_SIZE  = 5;
    private static final DateTimeFormatter LOGIN_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * 관리자 권한 설정 페이지로 이동
     * @param model
     * @return
     */
    @GetMapping("/settings/permissions")
    public String getPermissions(Model model) {
        log.info("getPermissions");

        List<EmployeeDetailDto> employeeList = staffService.retrieveActiveEmployeeList();

        Map<String, LoginLogDto> lastLoginMap = loginLogService.getLastLoginPerUser()
                .stream()
                .collect(Collectors.toMap(LoginLogDto::getUserId, dto -> dto));

        long onlineCount = lastLoginMap.values().stream()
                .filter(dto -> dto.getLogoutDt() == null)
                .count();

        LocalDateTime threshold = LocalDateTime.now().minusDays(7);
        long inactiveCount = employeeList.stream()
                .filter(emp -> {
                    LoginLogDto last = lastLoginMap.get(emp.getUserId());
                    return last == null || (last.getLoginDt() != null && last.getLoginDt().isBefore(threshold));
                })
                .count();

        model.addAttribute("employeeList", employeeList);
        model.addAttribute("lastLoginMap", lastLoginMap);
        model.addAttribute("onlineCount", onlineCount);
        model.addAttribute("inactiveCount", inactiveCount);
        model.addAttribute("departmentList", staffService.retrieveDepartmentList());
        model.addAttribute("jobGradeList", staffService.retrieveJobGradeList());
        return "admin:/principal/permission_management";
    }

    /**
     * 재직 직원 동적 검색 + 서버 페이징 (권한 설정 탭 AJAX)
     * 응답: { items, totalCount, lastLoginMap }
     */
    @GetMapping("/settings/permissions/search")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> searchPermissions(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String deptCd,
            @RequestParam(required = false) String jbgrNm,
            @RequestParam(required = false) String online,
            @RequestParam(defaultValue = "1")  int page,
            @RequestParam(defaultValue = "10") int screenSize,
            @RequestParam(required = false) String orderBy,
            @RequestParam(required = false) String orderDirection) {

        Map<String, Object> params = new HashMap<>();
        if (keyword != null && !keyword.isBlank()) params.put("keyword", keyword.trim());
        if (deptCd  != null && !deptCd.isBlank())  params.put("deptCd",  deptCd.trim());
        if (jbgrNm  != null && !jbgrNm.isBlank())  params.put("jbgrNm",  jbgrNm.trim());
        if (online  != null && !online.isBlank())   params.put("online",  online.trim());

        String safeDir = "DESC".equalsIgnoreCase(orderDirection) ? "DESC" : "ASC";
        PaginationInfo<Map<String, Object>> paging =
            new PaginationInfo<>(screenSize, PERM_BLOCK_SIZE, page, orderBy, safeDir);
        paging.setDetailCondition(params);

        PageResponse<EmployeeDetailDto> pageResp = staffService.searchActiveEmployeeList(paging);

        // 현재 페이지 직원 ID 목록
        Set<String> pageIds = pageResp.getItems().stream()
            .map(EmployeeDetailDto::getUserId)
            .collect(Collectors.toSet());

        // 마지막 로그인 정보 (현재 페이지 직원만)
        Map<String, Map<String, Object>> lastLoginMap = loginLogService.getLastLoginPerUser()
            .stream()
            .filter(dto -> pageIds.contains(dto.getUserId()))
            .collect(Collectors.toMap(
                LoginLogDto::getUserId,
                dto -> {
                    Map<String, Object> info = new HashMap<>();
                    info.put("online",  dto.getLogoutDt() == null);
                    info.put("loginDt", dto.getLoginDt() != null ? dto.getLoginDt().format(LOGIN_FMT) : null);
                    return info;
                }
            ));

        Map<String, Object> response = new HashMap<>();
        response.put("items",        pageResp.getItems());
        response.put("totalCount",   pageResp.getTotalCount());
        response.put("lastLoginMap", lastLoginMap);
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 직원의 접속 이력 + 활동 감사 로그 조회 (모달용 AJAX)
     * 응답: { loginLogs: [...], auditLogs: [...] }
     */
    @GetMapping("/settings/permissions/login-history/{userId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getLoginHistory(@PathVariable String userId) {
        // 1. 로그인/로그아웃 이력 (ADMINLOGIN_LOG)
        List<Map<String, Object>> loginLogs = new ArrayList<>();
        for (LoginLogDto log : loginLogService.getLoginLogsByUserId(userId)) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("loginDt",  log.getLoginDt()  != null ? log.getLoginDt().format(LOGIN_FMT)  : null);
            entry.put("logoutDt", log.getLogoutDt() != null ? log.getLogoutDt().format(LOGIN_FMT) : null);
            entry.put("loginIp",  log.getLoginIp());
            loginLogs.add(entry);
        }

        // 2. 최근 URL 접근 감사 로그 (HERMES_ADMIN_AUDIT_LOG)
        List<Map<String, Object>> auditLogs = new ArrayList<>();
        monitoringMapper.findRecentAdminAudits(userId).forEach(a -> {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("dt",         a.getCreatedAt());
            entry.put("method",     a.getHttpMethod());
            entry.put("uri",        a.getRequestUri());
            entry.put("ip",         a.getMemberIp());
            entry.put("statusCode", a.getStatusCode());
            auditLogs.add(entry);
        });

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("loginLogs", loginLogs);
        result.put("auditLogs", auditLogs);
        return ResponseEntity.ok(result);
    }
}
