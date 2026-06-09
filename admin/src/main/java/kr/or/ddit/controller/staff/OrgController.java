package kr.or.ddit.controller.staff;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import kr.or.ddit.finalProject.dto.employee.DepartmentDto;
import kr.or.ddit.finalProject.dto.employee.EmployeeDetailDto;
import kr.or.ddit.finalProject.dto.employee.JobGradeDto;
import kr.or.ddit.finalProject.dto.staff.AdminActivityType;
import kr.or.ddit.finalProject.service.staff.StaffService;
import kr.or.ddit.service.AdminActivityApprovalService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/admin/org")
public class OrgController {

    @Autowired
    StaffService staffService;

    @Autowired
    AdminActivityApprovalService activityApprovalService;

    /* ─── 메인 페이지 ─── */
    @GetMapping
    public String getOrgPage(Model model) {
        model.addAttribute("deptList",      staffService.retrieveDepartmentList());
        model.addAttribute("jobGradeList",  staffService.retrieveAllJobGradeList());
        return "admin:/staff/org";
    }

    /* ─── 직급 목록 AJAX (DB 페이징+필터) ─── */
    @GetMapping("/grade/list")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getGradeList(
            @RequestParam(defaultValue = "") String deptCd,
            @RequestParam(defaultValue = "") String useYn,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(staffService.searchJobGradeList(deptCd, useYn, page, size));
    }

    /* ─── 사수 배정 칸반용 직원 목록 AJAX ─── */
    @GetMapping("/mapping/employees")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getMappingEmployees(
            @RequestParam(defaultValue = "") String deptCd) {
        List<EmployeeDetailDto> all = staffService.retrieveEmployeeList();
        Stream<EmployeeDetailDto> stream = all.stream();
        if (!deptCd.isBlank()) {
            stream = stream.filter(e -> deptCd.equals(e.getDeptCd()));
        }
        List<Map<String, Object>> result = stream.map(e -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("userId",      e.getUserId());
            m.put("userName",    e.getMember() != null ? e.getMember().getUserName() : "");
            m.put("deptCd",      e.getDeptCd());
            m.put("deptNm",      e.getDeptNm());
            m.put("jbgrNm",      e.getJbgrNm());
            m.put("emplStatCd",  e.getEmplStatCd());
            m.put("userProfile", e.getMember() != null ? e.getMember().getUserProfile() : null);
            m.put("mntUserId",   e.getEmployeeInfo() != null ? e.getEmployeeInfo().getMntUserId() : null);
            return m;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    /* ═══════════ 부서 CRUD ═══════════ */
    @PostMapping("/dept")
    @ResponseBody
    public ResponseEntity<Map<String, String>> addDept(@RequestBody DepartmentDto dept, Principal p) {
        String actorId = p != null ? p.getName() : "SYSTEM";
        try {
            activityApprovalService.submitForApproval(actorId, AdminActivityType.DEPT_CREATE,
                dept.getDeptNm() + " (" + dept.getDeptCd() + ")",
                Map.of("dept", dept));
            return ResponseEntity.ok(Map.of("result", "success",
                "message", "결재 요청이 완료되었습니다. 승인 후 처리됩니다."));
        } catch (Exception e) {
            log.error("[OrgController] 부서 등록 결재 요청 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("result", "error", "message", e.getMessage()));
        }
    }

    @PutMapping("/dept/{deptCd}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> editDept(@PathVariable String deptCd,
                                                         @RequestBody DepartmentDto dept, Principal p) {
        String actorId = p != null ? p.getName() : "SYSTEM";
        try {
            dept.setDeptCd(deptCd);
            activityApprovalService.submitForApproval(actorId, AdminActivityType.DEPT_UPDATE,
                dept.getDeptNm() + " (" + deptCd + ")",
                Map.of("dept", dept));
            return ResponseEntity.ok(Map.of("result", "success",
                "message", "결재 요청이 완료되었습니다. 승인 후 처리됩니다."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("result", "error", "message", e.getMessage()));
        }
    }

    @PutMapping("/dept/{deptCd}/toggle")
    @ResponseBody
    public ResponseEntity<Map<String, String>> toggleDept(@PathVariable String deptCd,
                                                           @RequestBody Map<String, String> body, Principal p) {
        String actorId = p != null ? p.getName() : "SYSTEM";
        try {
            String useYn = body.get("useYn");
            activityApprovalService.submitForApproval(actorId, AdminActivityType.DEPT_TOGGLE,
                deptCd + " → " + ("Y".equals(useYn) ? "활성화" : "비활성화"),
                Map.of("deptCd", deptCd, "useYn", String.valueOf(useYn)));
            return ResponseEntity.ok(Map.of("result", "success",
                "message", "결재 요청이 완료되었습니다. 승인 후 처리됩니다."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("result", "error", "message", e.getMessage()));
        }
    }

    /* ═══════════ 직급 CRUD ═══════════ */

    @PostMapping("/grade")
    @ResponseBody
    public ResponseEntity<Map<String, String>> addGrade(@RequestBody JobGradeDto jbgr, Principal p) {
        String actorId = p != null ? p.getName() : "SYSTEM";
        try {
            activityApprovalService.submitForApproval(actorId, AdminActivityType.GRADE_CREATE,
                jbgr.getJbgrNm() + " (" + jbgr.getJbgrCd() + ")",
                Map.of("jbgr", jbgr));
            return ResponseEntity.ok(Map.of("result", "success",
                "message", "결재 요청이 완료되었습니다. 승인 후 처리됩니다."));
        } catch (Exception e) {
            log.error("[OrgController] 직급 등록 결재 요청 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("result", "error", "message", e.getMessage()));
        }
    }

    @PutMapping("/grade/{jbgrCd}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> editGrade(@PathVariable String jbgrCd,
                                                          @RequestBody JobGradeDto jbgr, Principal p) {
        String actorId = p != null ? p.getName() : "SYSTEM";
        try {
            jbgr.setJbgrCd(jbgrCd);
            activityApprovalService.submitForApproval(actorId, AdminActivityType.GRADE_UPDATE,
                jbgr.getJbgrNm() + " (" + jbgrCd + ")",
                Map.of("jbgr", jbgr));
            return ResponseEntity.ok(Map.of("result", "success",
                "message", "결재 요청이 완료되었습니다. 승인 후 처리됩니다."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("result", "error", "message", e.getMessage()));
        }
    }

    @PutMapping("/grade/{jbgrCd}/toggle")
    @ResponseBody
    public ResponseEntity<Map<String, String>> toggleGrade(@PathVariable String jbgrCd,
                                                            @RequestBody Map<String, String> body, Principal p) {
        String actorId = p != null ? p.getName() : "SYSTEM";
        try {
            String useYn = body.get("useYn");
            activityApprovalService.submitForApproval(actorId, AdminActivityType.GRADE_TOGGLE,
                jbgrCd + " → " + ("Y".equals(useYn) ? "활성화" : "비활성화"),
                Map.of("jbgrCd", jbgrCd, "useYn", String.valueOf(useYn)));
            return ResponseEntity.ok(Map.of("result", "success",
                "message", "결재 요청이 완료되었습니다. 승인 후 처리됩니다."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("result", "error", "message", e.getMessage()));
        }
    }

    /* ═══════════ 사수 매핑 ═══════════ */

    @PostMapping("/mapping/batch")
    @ResponseBody
    public ResponseEntity<Map<String, String>> batchAssignMnt(
            @RequestBody List<Map<String, String>> assignments, Principal p) {
        String actorId = p != null ? p.getName() : "SYSTEM";
        try {
            for (Map<String, String> entry : assignments) {
                String userId    = entry.get("userId");
                String mntUserId = entry.get("mntUserId");
                if (userId == null || userId.isBlank()) continue;
                String summary = mntUserId != null && !mntUserId.isBlank()
                    ? userId + " → 사수: " + mntUserId
                    : userId + " → 배정 해제";
                activityApprovalService.submitForApproval(actorId, AdminActivityType.MNT_MAPPING,
                    summary, Map.of("userId", userId, "mntUserId", mntUserId != null ? mntUserId : ""));
            }
            return ResponseEntity.ok(Map.of("result", "success",
                "message", "결재에 등록되었습니다."));
        } catch (Exception e) {
            log.error("[OrgController] 사수 배정 일괄 결재 요청 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("result", "error", "message", e.getMessage()));
        }
    }
}
