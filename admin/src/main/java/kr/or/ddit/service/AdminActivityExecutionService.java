package kr.or.ddit.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.or.ddit.finalProject.dto.approval.ApprovalMasterDto;
import kr.or.ddit.finalProject.dto.employee.DepartmentDto;
import kr.or.ddit.finalProject.dto.employee.EmployeeInfoDto;
import kr.or.ddit.finalProject.dto.employee.EmployeeSalaryDto;
import kr.or.ddit.finalProject.dto.employee.JobGradeDto;
import kr.or.ddit.finalProject.dto.member.MemberCreateLogDto;
import kr.or.ddit.finalProject.dto.member.MemberDto;
import kr.or.ddit.finalProject.service.staff.StaffService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminActivityExecutionService {

    private final ApprovalService approvalService;
    private final StaffService staffService;
    private final ObjectMapper objectMapper;

    @Transactional
    public void execute(Long aprvlDocSn) {
        try {
            ApprovalMasterDto master = approvalService.getApprovalDetail(aprvlDocSn);
            String content = master.getAprvlDocCn();
            if (content == null || !content.trim().startsWith("{")) return;

            Map<String, Object> envelope = objectMapper.readValue(content, new TypeReference<>() {});
            if (!Boolean.TRUE.equals(envelope.get("systemPayload"))) return;

            String actionType = (String) envelope.get("actionType");
            String actorUserId = (String) envelope.get("actorUserId");
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) envelope.get("data");

            switch (actionType) {
                case "EMPLOYEE_REGISTER" -> executeEmployeeRegister(data, actorUserId);
                case "EMPLOYEE_UPDATE"   -> executeEmployeeUpdate(data, actorUserId);
                case "EMPLOYEE_RETIRE"   -> executeEmployeeRetire(data, actorUserId);
                case "STUDENT_REGISTER"  -> executeStudentRegister(data, actorUserId);
                case "STUDENT_UPDATE"    -> executeStudentUpdate(data, actorUserId);
                case "STUDENT_RETIRE"    -> executeStudentRetire(data, actorUserId);
                case "DEPT_CREATE"       -> executeDeptCreate(data, actorUserId);
                case "DEPT_UPDATE"       -> executeDeptUpdate(data, actorUserId);
                case "DEPT_TOGGLE"       -> executeDeptToggle(data, actorUserId);
                case "GRADE_CREATE"      -> executeGradeCreate(data, actorUserId);
                case "GRADE_UPDATE"      -> executeGradeUpdate(data, actorUserId);
                case "GRADE_TOGGLE"      -> executeGradeToggle(data, actorUserId);
                case "MNT_MAPPING"       -> executeMntMapping(data, actorUserId);
                default -> log.warn("[AdminExecution] 알 수 없는 actionType: {}", actionType);
            }
            log.info("[AdminExecution] 결재 승인 후 실행 완료: docSn={}, type={}", aprvlDocSn, actionType);

        } catch (Exception e) {
            log.error("[AdminExecution] 실행 실패: docSn={}, reason={}", aprvlDocSn, e.getMessage(), e);
        }
    }

    private void executeEmployeeRegister(Map<String, Object> data, String actorUserId) {
        MemberDto memberDto = objectMapper.convertValue(data.get("memberDto"), MemberDto.class);
        EmployeeInfoDto employeeInfoDto = objectMapper.convertValue(data.get("employeeInfoDto"), EmployeeInfoDto.class);
        EmployeeSalaryDto employeeSalaryDto = objectMapper.convertValue(data.get("employeeSalaryDto"), EmployeeSalaryDto.class);
        String profileUrl = (String) data.get("profileUrl");
        staffService.registerEmployee(memberDto, employeeInfoDto, employeeSalaryDto, profileUrl, actorUserId);
    }

    @SuppressWarnings("unchecked")
    private void executeEmployeeUpdate(Map<String, Object> data, String actorUserId) {
        // before/after 구조로 저장된 경우 after에서 실제 데이터 추출
        Map<String, Object> effectiveData = data.containsKey("after")
            ? (Map<String, Object>) data.get("after")
            : data;
        MemberDto memberDto = objectMapper.convertValue(effectiveData.get("memberDto"), MemberDto.class);
        EmployeeInfoDto employeeInfoDto = objectMapper.convertValue(effectiveData.get("employeeInfoDto"), EmployeeInfoDto.class);
        EmployeeSalaryDto employeeSalaryDto = objectMapper.convertValue(effectiveData.get("employeeSalaryDto"), EmployeeSalaryDto.class);
        staffService.updateEmployee(memberDto, employeeInfoDto, employeeSalaryDto, actorUserId);
    }

    private void executeEmployeeRetire(Map<String, Object> data, String actorUserId) {
        String userId = (String) data.get("userId");
        String retmtRsn = (String) data.get("retmtRsn");
        staffService.retireEmployee(userId, retmtRsn, actorUserId);
    }

    private void executeStudentRegister(Map<String, Object> data, String actorUserId) {
        MemberDto memberDto = objectMapper.convertValue(data.get("memberDto"), MemberDto.class);
        MemberCreateLogDto memberCreateLog = objectMapper.convertValue(data.get("memberCreateLog"), MemberCreateLogDto.class);
        String profileUrl = (String) data.get("profileUrl");
        staffService.registerStudent(memberDto, memberCreateLog, profileUrl, actorUserId);
    }

    private void executeStudentUpdate(Map<String, Object> data, String actorUserId) {
        MemberDto memberDto = objectMapper.convertValue(data.get("memberDto"), MemberDto.class);
        staffService.updateStudent(memberDto, actorUserId);
    }

    private void executeStudentRetire(Map<String, Object> data, String actorUserId) {
        String userId = (String) data.get("userId");
        String withdrawRsn = (String) data.get("withdrawRsn");
        staffService.retireStudent(userId, withdrawRsn, actorUserId);
    }

    /* ── 조직 관리 ── */

    private void executeDeptCreate(Map<String, Object> data, String actorUserId) {
        DepartmentDto dept = objectMapper.convertValue(data.get("dept"), DepartmentDto.class);
        dept.setRgtrId(actorUserId);
        staffService.addDepartment(dept);
    }

    private void executeDeptUpdate(Map<String, Object> data, String actorUserId) {
        DepartmentDto dept = objectMapper.convertValue(data.get("dept"), DepartmentDto.class);
        dept.setLastMdfrId(actorUserId);
        staffService.modifyDepartment(dept);
    }

    private void executeDeptToggle(Map<String, Object> data, String actorUserId) {
        String deptCd = (String) data.get("deptCd");
        String useYn  = (String) data.get("useYn");
        staffService.toggleDeptUseYn(deptCd, useYn, actorUserId);
    }

    private void executeGradeCreate(Map<String, Object> data, String actorUserId) {
        JobGradeDto jbgr = objectMapper.convertValue(data.get("jbgr"), JobGradeDto.class);
        jbgr.setRgtrId(actorUserId);
        staffService.addJobGrade(jbgr);
    }

    private void executeGradeUpdate(Map<String, Object> data, String actorUserId) {
        JobGradeDto jbgr = objectMapper.convertValue(data.get("jbgr"), JobGradeDto.class);
        jbgr.setLastMdfrId(actorUserId);
        staffService.modifyJobGrade(jbgr);
    }

    private void executeGradeToggle(Map<String, Object> data, String actorUserId) {
        String jbgrCd = (String) data.get("jbgrCd");
        String useYn  = (String) data.get("useYn");
        staffService.toggleJbgrUseYn(jbgrCd, useYn, actorUserId);
    }

    private void executeMntMapping(Map<String, Object> data, String actorUserId) {
        String userId    = (String) data.get("userId");
        String mntUserId = (String) data.get("mntUserId");
        staffService.assignMntUserId(userId, mntUserId != null && !mntUserId.isBlank() ? mntUserId : null, actorUserId);
    }
}
