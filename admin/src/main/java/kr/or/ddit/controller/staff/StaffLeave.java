package kr.or.ddit.controller.staff;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.dto.leave.AnnualLeaveHistoryDto;
import kr.or.ddit.finalProject.dto.leave.LeaveBalanceDto;
import kr.or.ddit.finalProject.paging.PaginationInfo;
import kr.or.ddit.finalProject.service.staff.StaffService;
import lombok.extern.slf4j.Slf4j;

/**
 * 휴가 관리 (행정)
 *
 * ✔ 이 화면은 "조회 전용" 행정 대시보드다.
 *   - 휴가 신청 / 직권 등록 등 쓰기 동작은 전부 전자결재로 처리한다.
 *   - 여기서는 전자결재로 최종 승인되어 적재된 휴가 사용 현황과,
 *     직급 기반 잔여 연차 현황만 조회한다.
 * ✔ 페이징·검색 필터는 전부 컨트롤러(서버) 단에서 처리한다.
 */
@Slf4j
@Controller
@RequestMapping("/admin")
public class StaffLeave {

    @Autowired
    StaffService staffService;

    private static final int BLOCK_SIZE = 5;

    /**
     * 휴가 관리 메인 화면
     */
    @GetMapping("/hr/leave")
    public String getHrLeave(Model model) {
        log.info("getHrLeave()");

        // 상단 요약 카드 (오늘 휴가자 / 이번달 / 올해 / 예정 건수)
        model.addAttribute("leaveSummary", staffService.getLeaveSummary());
        // 필터용 부서 목록
        model.addAttribute("departmentlist", staffService.retrieveDepartmentList());

        return "admin:/staff/hr_leave";
    }

    /**
     * 휴가 현황 동적 검색 + 서버 페이징 (AJAX)
     */
    @GetMapping("/hr/leave/search")
    @ResponseBody
    public ResponseEntity<PageResponse<AnnualLeaveHistoryDto>> searchLeaveHistory(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String year,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) String deptCd,
            @RequestParam(defaultValue = "1")  int page,
            @RequestParam(defaultValue = "10") int screenSize,
            @RequestParam(required = false) String orderBy,
            @RequestParam(required = false) String orderDirection) {

        Map<String, Object> cond = new HashMap<>();
        if (keyword != null && !keyword.isBlank()) cond.put("keyword", keyword.trim());
        if (type    != null && !type.isBlank())    cond.put("type",    type.trim());
        if (year    != null && !year.isBlank())     cond.put("year",    year.trim());
        if (month   != null && !month.isBlank())    cond.put("month",   month.replace("-", "").trim());
        if (deptCd  != null && !deptCd.isBlank())   cond.put("deptCd",  deptCd.trim());

        String safeDir = "ASC".equalsIgnoreCase(orderDirection) ? "ASC" : "DESC";
        PaginationInfo<Map<String, Object>> paging =
            new PaginationInfo<>(screenSize, BLOCK_SIZE, page, orderBy, safeDir);
        paging.setDetailCondition(cond);

        return ResponseEntity.ok(staffService.searchLeaveHistory(paging));
    }

    /**
     * 잔여 연차 현황 동적 검색 + 서버 페이징 (AJAX)
     */
    @GetMapping("/hr/leave/balance")
    @ResponseBody
    public ResponseEntity<PageResponse<LeaveBalanceDto>> searchLeaveBalance(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String deptCd,
            @RequestParam(required = false) String year,
            @RequestParam(defaultValue = "1")  int page,
            @RequestParam(defaultValue = "10") int screenSize,
            @RequestParam(required = false) String orderBy,
            @RequestParam(required = false) String orderDirection) {

        Map<String, Object> cond = new HashMap<>();
        if (keyword != null && !keyword.isBlank()) cond.put("keyword", keyword.trim());
        if (deptCd  != null && !deptCd.isBlank())   cond.put("deptCd",  deptCd.trim());
        if (year    != null && !year.isBlank())     cond.put("year",    year.trim());

        String safeDir = "DESC".equalsIgnoreCase(orderDirection) ? "DESC" : "ASC";
        PaginationInfo<Map<String, Object>> paging =
            new PaginationInfo<>(screenSize, BLOCK_SIZE, page, orderBy, safeDir);
        paging.setDetailCondition(cond);

        return ResponseEntity.ok(staffService.searchLeaveBalance(paging));
    }
}
