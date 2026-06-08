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
import kr.or.ddit.finalProject.service.staff.StaffService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/admin/org")
public class OrgController {

    @Autowired
    StaffService staffService;

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
        try {
            dept.setRgtrId(p != null ? p.getName() : "SYSTEM");
            staffService.addDepartment(dept);
            return ResponseEntity.ok(Map.of("result", "success"));
        } catch (Exception e) {
            log.error("[OrgController] 부서 등록 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("result", "error", "message", e.getMessage()));
        }
    }

    @PutMapping("/dept/{deptCd}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> editDept(@PathVariable String deptCd,
                                                         @RequestBody DepartmentDto dept, Principal p) {
        try {
            dept.setDeptCd(deptCd);
            dept.setLastMdfrId(p != null ? p.getName() : "SYSTEM");
            staffService.modifyDepartment(dept);
            return ResponseEntity.ok(Map.of("result", "success"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("result", "error", "message", e.getMessage()));
        }
    }

    @PutMapping("/dept/{deptCd}/toggle")
    @ResponseBody
    public ResponseEntity<Map<String, String>> toggleDept(@PathVariable String deptCd,
                                                           @RequestBody Map<String, String> body, Principal p) {
        try {
            staffService.toggleDeptUseYn(deptCd, body.get("useYn"), p != null ? p.getName() : "SYSTEM");
            return ResponseEntity.ok(Map.of("result", "success"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("result", "error", "message", e.getMessage()));
        }
    }

    /* ═══════════ 직급 CRUD ═══════════ */

    @PostMapping("/grade")
    @ResponseBody
    public ResponseEntity<Map<String, String>> addGrade(@RequestBody JobGradeDto jbgr, Principal p) {
        try {
            jbgr.setRgtrId(p != null ? p.getName() : "SYSTEM");
            staffService.addJobGrade(jbgr);
            return ResponseEntity.ok(Map.of("result", "success"));
        } catch (Exception e) {
            log.error("[OrgController] 직급 등록 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("result", "error", "message", e.getMessage()));
        }
    }

    @PutMapping("/grade/{jbgrCd}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> editGrade(@PathVariable String jbgrCd,
                                                          @RequestBody JobGradeDto jbgr, Principal p) {
        try {
            jbgr.setJbgrCd(jbgrCd);
            jbgr.setLastMdfrId(p != null ? p.getName() : "SYSTEM");
            staffService.modifyJobGrade(jbgr);
            return ResponseEntity.ok(Map.of("result", "success"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("result", "error", "message", e.getMessage()));
        }
    }

    @PutMapping("/grade/{jbgrCd}/toggle")
    @ResponseBody
    public ResponseEntity<Map<String, String>> toggleGrade(@PathVariable String jbgrCd,
                                                            @RequestBody Map<String, String> body, Principal p) {
        try {
            staffService.toggleJbgrUseYn(jbgrCd, body.get("useYn"), p != null ? p.getName() : "SYSTEM");
            return ResponseEntity.ok(Map.of("result", "success"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("result", "error", "message", e.getMessage()));
        }
    }

    /* ═══════════ 사수 매핑 ═══════════ */

    @PutMapping("/mapping/{userId}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> assignMnt(@PathVariable String userId,
                                                          @RequestBody Map<String, String> body, Principal p) {
        try {
            String mntUserId = body.getOrDefault("mntUserId", null);
            staffService.assignMntUserId(userId, mntUserId, p != null ? p.getName() : "SYSTEM");
            return ResponseEntity.ok(Map.of("result", "success"));
        } catch (Exception e) {
            log.error("[OrgController] 사수 배정 실패: userId={}, {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("result", "error", "message", e.getMessage()));
        }
    }
}
