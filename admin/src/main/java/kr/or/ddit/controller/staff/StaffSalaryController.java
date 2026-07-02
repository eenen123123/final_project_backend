package kr.or.ddit.controller.staff;

import java.io.PrintWriter;
import java.security.Principal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpServletResponse;
import kr.or.ddit.finalProject.dto.common.CommonCodeDto;
import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.dto.employee.DepartmentDto;
import kr.or.ddit.finalProject.dto.employee.SalaryAccountRowDto;
import kr.or.ddit.finalProject.dto.staff.AdminActivityType;
import kr.or.ddit.finalProject.paging.PaginationInfo;
import kr.or.ddit.finalProject.service.staff.StaffService;
import kr.or.ddit.service.AdminActivityApprovalService;
import kr.or.ddit.service.CommonCodeService;
import kr.or.ddit.service.SalaryAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 급여 + 계좌 관리 컨트롤러.
 *
 * - 급여(기본급) 조회·변경, 급여 계좌(은행/계좌/예금주) 관리 화면 제공
 * - 변경 저장은 직원 관리와 동일하게 전자결재 워크플로우를 거친다 (승인 후 반영)
 * - 외부 결제 프로그램 연동을 위한 CSV(UTF-8 BOM) 내보내기 제공
 */
@Slf4j
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class StaffSalaryController {

    private static final String BANK_CL_CODE = "702";
    private static final int SALARY_BLOCK_SIZE = 5;

    private final SalaryAccountService salaryAccountService;
    private final StaffService staffService;
    private final CommonCodeService commonCodeService;
    private final AdminActivityApprovalService activityApprovalService;

    /** 급여/계좌 관리 메인 화면 */
    @GetMapping("/salary")
    public String salaryPage(Model model) {
        log.info("salaryPage()");
        List<DepartmentDto> departmentlist = staffService.retrieveDepartmentList();
        List<CommonCodeDto> bankList = commonCodeService.getAllCodes(BANK_CL_CODE);

        model.addAttribute("departmentlist", departmentlist);
        model.addAttribute("bankList", bankList);
        return "admin:/staff/salary";
    }

    /** 급여+계좌 목록 동적 검색 + 서버 페이징 (AJAX) */
    @GetMapping("/salary/list")
    @ResponseBody
    public ResponseEntity<PageResponse<SalaryAccountRowDto>> salaryList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String deptCd,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1")  int page,
            @RequestParam(defaultValue = "10") int screenSize) {

        Map<String, Object> searchParams = new HashMap<>();
        if (keyword != null && !keyword.isBlank()) searchParams.put("keyword", keyword.trim());
        if (deptCd != null && !deptCd.isBlank())   searchParams.put("deptCd", deptCd.trim());
        if (status != null && !status.isBlank())   searchParams.put("status", status.trim());

        PaginationInfo<Map<String, Object>> paging =
            new PaginationInfo<>(screenSize, SALARY_BLOCK_SIZE, page);
        paging.setDetailCondition(searchParams);

        return ResponseEntity.ok(salaryAccountService.searchSalaryAccountList(paging));
    }

    /** 급여·계좌 변경 → 결재 요청 (승인 후 반영) */
    @PutMapping("/salary/update")
    @ResponseBody
    public ResponseEntity<Map<String, String>> updateSalaryAccount(
            @RequestParam String userId,
            @RequestParam(required = false) Integer baseSalary,
            @RequestParam(required = false) String bankCd,
            @RequestParam(required = false) String acntNo,
            @RequestParam(required = false) String depositorNm,
            Principal principal) {

        String loginAdminId = principal != null ? principal.getName() : "SYSTEM";

        try {
            Map<String, Object> after = new LinkedHashMap<>();
            after.put("userId", userId);
            after.put("baseSalary", baseSalary);
            after.put("bankCd", bankCd);
            after.put("acntNo", acntNo);
            after.put("depositorNm", depositorNm);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("after", after);

            activityApprovalService.submitForApproval(
                loginAdminId, AdminActivityType.SALARY_ACCOUNT_UPDATE, userId, data);

            return ResponseEntity.ok(Map.of("result", "success",
                "message", "결재 요청이 완료되었습니다. 승인 후 반영됩니다."));
        } catch (Exception e) {
            log.error("[updateSalaryAccount] 결재 요청 실패. userId={}, cause={}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("result", "error", "message", e.getMessage()));
        }
    }

    /** 급여+계좌 목록 CSV 내보내기 (UTF-8 BOM, 외부 급여 프로그램 연동용) */
    @GetMapping("/salary/export/csv")
    public void exportCsv(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String deptCd,
            @RequestParam(required = false) String status,
            HttpServletResponse response) throws Exception {

        Map<String, Object> params = new HashMap<>();
        if (keyword != null && !keyword.isBlank()) params.put("keyword", keyword.trim());
        if (deptCd != null && !deptCd.isBlank())   params.put("deptCd", deptCd.trim());
        if (status != null && !status.isBlank())   params.put("status", status.trim());

        List<SalaryAccountRowDto> rows = salaryAccountService.getSalaryAccountList(params);

        String fileName = "salary_" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ".csv";
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

        PrintWriter writer = response.getWriter();
        writer.write('﻿'); // Excel 한글 깨짐 방지 BOM
        writer.println("직원ID,직원명,부서,직급,기본급,은행,계좌번호,예금주");
        for (SalaryAccountRowDto r : rows) {
            writer.println(String.join(",",
                csv(r.getUserId()),
                csv(r.getUserName()),
                csv(r.getDeptNm()),
                csv(r.getJbgrNm()),
                csv(r.getBaseSalary() != null ? String.valueOf(r.getBaseSalary()) : ""),
                csv(r.getBankNm()),
                csv(r.getAcntNo()),
                csv(r.getDepositorNm())));
        }
        writer.flush();
    }

    /** CSV 셀 이스케이프 (콤마·큰따옴표·줄바꿈 포함 시 큰따옴표로 감쌈) */
    private String csv(String v) {
        if (v == null) return "";
        if (v.contains(",") || v.contains("\"") || v.contains("\n")) {
            return "\"" + v.replace("\"", "\"\"") + "\"";
        }
        return v;
    }
}
