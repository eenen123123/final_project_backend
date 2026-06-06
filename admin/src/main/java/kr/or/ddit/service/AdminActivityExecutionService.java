package kr.or.ddit.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.or.ddit.finalProject.dto.approval.ApprovalMasterDto;
import kr.or.ddit.finalProject.dto.employee.EmployeeInfoDto;
import kr.or.ddit.finalProject.dto.employee.EmployeeSalaryDto;
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

    private void executeEmployeeUpdate(Map<String, Object> data, String actorUserId) {
        MemberDto memberDto = objectMapper.convertValue(data.get("memberDto"), MemberDto.class);
        EmployeeInfoDto employeeInfoDto = objectMapper.convertValue(data.get("employeeInfoDto"), EmployeeInfoDto.class);
        EmployeeSalaryDto employeeSalaryDto = objectMapper.convertValue(data.get("employeeSalaryDto"), EmployeeSalaryDto.class);
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
}
