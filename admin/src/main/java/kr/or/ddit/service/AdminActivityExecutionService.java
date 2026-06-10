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
import kr.or.ddit.finalProject.service.file.CloudinaryUploadService;
import kr.or.ddit.finalProject.service.staff.StaffService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.Map;

/**
 * AdminActivityExecutionService
 *
 * ✔ 전자결재 승인 완료 후 시스템 반영을 자동화하는 행위 집행(Execution) 서비스
 *
 * ✔ 역할 요약
 * ---------------------------------------------------------------------
 * - 결재 문서 내용(JSON Payload) 파싱 및 비즈니스 데이터 추출
 * - 각 액션 타입(직원/학생/조직 관리 등)에 따른 분기 및 핸들러 메서드 매핑
 * - 임시 저장된 Base64 프로필 이미지 데이터를 Cloudinary 클라우드 스토리지에 업로드
 * - 최종 정제된 데이터를 StaffService 핵심 인사 비즈니스 로직에 전달하여 DB 최종 반영
 *
 * ✔ 설계 목적
 * ---------------------------------------------------------------------
 * 1. 결재 승인과 물리적인 데이터 변경(인사 등록 등) 간의 동기화 및 결합도 분리
 * 2. 결재 문서 본문에 포함된 비정형 데이터(JSON)를 강타입 DTO 객체로 안전하게 변환(역직렬화)
 * 3. 하나의 결재 완료 처리가 모든 하위 시스템 반영을 보장하도록 단일 트랜잭션 범위 지정
 *
 * ✔ 아키텍처 위치 (ServiceImpl Layer - Event Handler / Executor)
 * ---------------------------------------------------------------------
 * [ApprovalController] ➡️ [ApprovalService (최종 승인 처리)]
 * ↓
 * [AdminActivityExecutionService.execute()] 🌟 (현재 위치)
 * ↓ (JSON Payload Parsing & Switch)
 * +-----------------------+-----------------------+
 * ↓                       ↓                       ↓
 * [executeEmployee...]   [executeStudent...]     [executeDept...]
 * ↓                       ↓                       ↓
 * [CloudinaryService]    [StaffService]          [StaffService]
 * (이미지 업로드)        (인사 DB 반영)          (조직 DB 반영)
 * ---------------------------------------------------------------------
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminActivityExecutionService {

    private final ApprovalService approvalService;
    private final StaffService staffService;
    private final CloudinaryUploadService cloudinaryUploadService;
    private final ObjectMapper objectMapper;

    /**
     * 최종 승인된 결재 문서 스냅샷 분석 및 비즈니스 로직 연동 총괄
     *
     * ✔ 내부 동작 및 설계 포인트
     * ---------------------------------------------------------------------
     * 1. 결재 일련번호를 통해 문서 내용을 확보하고 시스템 자동 집행 대상 여부 확인
     * 2. JSON 포맷의 문서 본문에서 행위 유형(actionType)과 행위자(actorUserId) 추출
     * 3. Switch-Case 문을 통해 적절한 전용 처리 메서드로 제어권 위임
     *
     * @param aprvlDocSn 결재 마스터 테이블의 고유 식별 번호 (결재 문서 일련번호)
     */
    @Transactional
    public void execute(Long aprvlDocSn) {
        try {
            // 1. 전자결재 서비스로부터 해당 문서의 상세 마스터 정보를 조회한다.
            ApprovalMasterDto master = approvalService.getApprovalDetail(aprvlDocSn);
            String content = master.getAprvlDocCn();

            // 2. 문서 내용이 없거나 JSON 시작 문자({)로 구성되지 않은 일반 결재 문서는 처리 대상에서 제외한다.
            if (content == null || !content.trim().startsWith("{")) return;

            // 3. 결재 문서 본문 문자열을 엔벨로프(Envelope) 형태의 공통 Map 구조로 역직렬화한다.
            Map<String, Object> envelope = objectMapper.readValue(content, new TypeReference<>() {});

            // 4. 시스템 자동 실행 대상 데이터(systemPayload == true)가 아닐 경우 처리를 중단한다.
            if (!Boolean.TRUE.equals(envelope.get("systemPayload"))) return;

            // 5. 엔벨로프 데이터 내부에서 수행할 액션 코드와 등록 요청자를 추출한다.
            String actionType = (String) envelope.get("actionType");
            String actorUserId = (String) envelope.get("actorUserId");

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) envelope.get("data");
            
            // 6. 행위 유형(Action Type)에 따라 매핑된 세부 비즈니스 집행 로직으로 분기한다.
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
            // 7. 파싱 및 비즈니스 반영 중 오류 발생 시 트랜잭션을 롤백하고 에러 로그를 남긴다.
            log.error("[AdminExecution] 실행 실패: docSn={}, reason={}", aprvlDocSn, e.getMessage(), e);
        }
    }

    /**
     * 신규 직원 등록 집행
     * @param data 결재 본문에서 추출한 맵 형태의 원시 인사 데이터
     * @param actorUserId 처리를 승인/집행한 관리자 ID
     */
    private void executeEmployeeRegister(Map<String, Object> data, String actorUserId) {
        // 1. 맵 데이터를 각각의 회원 계정, 인사 정보, 급여 정보 DTO로 변환한다.
        MemberDto memberDto = objectMapper.convertValue(data.get("memberDto"), MemberDto.class);
        EmployeeInfoDto employeeInfoDto = objectMapper.convertValue(data.get("employeeInfoDto"), EmployeeInfoDto.class);
        EmployeeSalaryDto employeeSalaryDto = objectMapper.convertValue(data.get("employeeSalaryDto"), EmployeeSalaryDto.class);

        // 2. 대기 상태의 Base64 파일 데이터를 클라우드에 업로드하여 영구 URL을 발급받는다.
        String profileUrl = uploadPendingImage(data);

        // 3. 인사 관리 서비스의 등록 메서드를 호출하여 복합 테이블 저장을 실행한다.
        staffService.registerEmployee(memberDto, employeeInfoDto, employeeSalaryDto, profileUrl, actorUserId);
    }

    /**
     * 직원 정보 수정 집행
     * @param data 비포/애프터 구조를 포함할 수 있는 원시 변경 데이터
     * @param actorUserId 수정을 최종 승인한 관리자 ID
     */
    @SuppressWarnings("unchecked")
    private void executeEmployeeUpdate(Map<String, Object> data, String actorUserId) {
        // 1. 데이터가 변경 전(before)과 변경 후(after) 스냅샷 구조로 넘어온 경우, 실제 반영할 'after' 데이터를 타깃으로 지정한다.
        Map<String, Object> effectiveData = data.containsKey("after")
            ? (Map<String, Object>) data.get("after")
            : data;
            
        MemberDto memberDto = objectMapper.convertValue(effectiveData.get("memberDto"), MemberDto.class);

        // 2. 새롭게 수정 등록된 프로필 이미지가 존재하면 Cloudinary에 업로드 후 DTO 객체에 경로를 갱신한다.
        String newProfileUrl = uploadPendingImage(effectiveData);
        if (newProfileUrl != null) memberDto.setUserProfile(newProfileUrl);

        EmployeeInfoDto employeeInfoDto = objectMapper.convertValue(effectiveData.get("employeeInfoDto"), EmployeeInfoDto.class);
        EmployeeSalaryDto employeeSalaryDto = objectMapper.convertValue(effectiveData.get("employeeSalaryDto"), EmployeeSalaryDto.class);

        // 3. 변경 사항을 인사 마스터 DB에 통합 반영한다.
        staffService.updateEmployee(memberDto, employeeInfoDto, employeeSalaryDto, actorUserId);
    }

    /**
     * 직원 퇴사 처리 집행
     * @param data
     * @param actorUserId
     */
    private void executeEmployeeRetire(Map<String, Object> data, String actorUserId) {
        String userId = (String) data.get("userId");
        String retmtRsn = (String) data.get("retmtRsn");

        // 1. 대상 직원의 계정을 비활성화하고 퇴사 일자 및 퇴사 사유를 세팅한다.
        staffService.retireEmployee(userId, retmtRsn, actorUserId);
    }

    /**
     * 신규 학생 등록 집행
     * @param data
     * @param actorUserId
     */
    private void executeStudentRegister(Map<String, Object> data, String actorUserId) {
        MemberDto memberDto = objectMapper.convertValue(data.get("memberDto"), MemberDto.class);
        MemberCreateLogDto memberCreateLog = objectMapper.convertValue(data.get("memberCreateLog"), MemberCreateLogDto.class);

        String profileUrl = uploadPendingImage(data);

        // 1. 학생 서비스 계층과 연동하여 학생 계정 및 초기 수강 로그를 적재한다.
        staffService.registerStudent(memberDto, memberCreateLog, profileUrl, actorUserId);
    }

    /**
     * 학생 정보 수정 집행
     * @param data 비포/애프터 구조를 포함할 수 있는 원시 변경 데이터
     * @param actorUserId 수정을 최종 승인한 관리자 ID
     */
    @SuppressWarnings("unchecked")
    private void executeStudentUpdate(Map<String, Object> data, String actorUserId) {
        Map<String, Object> effectiveData = data.containsKey("after")
                ? (Map<String, Object>) data.get("after") : data;
        MemberDto memberDto = objectMapper.convertValue(effectiveData.get("memberDto"), MemberDto.class);

        String newProfileUrl = uploadPendingImage(effectiveData);
        if (newProfileUrl != null) memberDto.setUserProfile(newProfileUrl);

        // 1. 학생 인적 사항 변경 정보를 시스템에 영구 반영한다.
        staffService.updateStudent(memberDto, actorUserId);
    }

    /**
     * 학생 퇴원 처리 집행
     * @param data 비포/에프터 구조를 포함할 수 있는 원시 변경 데이터
     * @param actorUserId 수정을 최종 승인한 관리자 ID
     */
    private void executeStudentRetire(Map<String, Object> data, String actorUserId) {
        String userId = (String) data.get("userId");
        String withdrawRsn = (String) data.get("withdrawRsn");
        
        // 1. 대상 학생의 상태 코드를 퇴원으로 변경하고 일자를 기록한다.
        staffService.retireStudent(userId, withdrawRsn, actorUserId);
    }

    /* ── 조직 관리 집행 함수군 ── */

    /**
     * 신규 부서 생성 집행
     */
    private void executeDeptCreate(Map<String, Object> data, String actorUserId) {
        DepartmentDto dept = objectMapper.convertValue(data.get("dept"), DepartmentDto.class);
        dept.setRgtrId(actorUserId); // 1. 최종 수정 행위자를 결재 집행자로 바인딩한다.
        staffService.addDepartment(dept);
    }

    /**
     * 부서명 및 부서 정보 수정 집행
     */
    private void executeDeptUpdate(Map<String, Object> data, String actorUserId) {
        DepartmentDto dept = objectMapper.convertValue(data.get("dept"), DepartmentDto.class);
        dept.setLastMdfrId(actorUserId); // 1. 최종 수정 행위자를 결재 집행자로 바인딩한다.
        staffService.modifyDepartment(dept);
    }

    /**
     * 부서 사용 여부(활성화/비활성화) 상태 전환 집행
     */
    private void executeDeptToggle(Map<String, Object> data, String actorUserId) {
        String deptCd = (String) data.get("deptCd");
        String useYn  = (String) data.get("useYn");
        
        // 1. 부서의 토글 상태 값(Y/N)을 영속성 컨텍스트에 업데이트한다.
        staffService.toggleDeptUseYn(deptCd, useYn, actorUserId);
    }

    /**
     * 신규 직급 생성 집행
     */
    private void executeGradeCreate(Map<String, Object> data, String actorUserId) {
        JobGradeDto jbgr = objectMapper.convertValue(data.get("jbgr"), JobGradeDto.class);
        jbgr.setRgtrId(actorUserId);
        staffService.addJobGrade(jbgr);
    }

    /**
     * 직급 기준 정보 수정 집행
     */
    private void executeGradeUpdate(Map<String, Object> data, String actorUserId) {
        JobGradeDto jbgr = objectMapper.convertValue(data.get("jbgr"), JobGradeDto.class);
        jbgr.setLastMdfrId(actorUserId);
        staffService.modifyJobGrade(jbgr);
    }

    /**
     * 직급 마스터 사용 여부 상태 전환 집행
     */
    private void executeGradeToggle(Map<String, Object> data, String actorUserId) {
        String jbgrCd = (String) data.get("jbgrCd");
        String useYn  = (String) data.get("useYn");
        staffService.toggleJbgrUseYn(jbgrCd, useYn, actorUserId);
    }

    /**
     * 사수-부사수 매핑 관계 집행
     */
    private void executeMntMapping(Map<String, Object> data, String actorUserId) {
        String userId    = (String) data.get("userId");
        String mntUserId = (String) data.get("mntUserId");
        
        // 1. 사수 아이디의 Null 유무 및 공백 상태를 정제한 후 매핑 테이블에 할당한다.
        staffService.assignMntUserId(userId, mntUserId != null && !mntUserId.isBlank() ? mntUserId : null, actorUserId);
    }

    /**
     * Base64 바이너리 데이터를 물리 파일로 복원하여 Cloudinary CDN에 영구 업로드 처리
     *
     * ✔ 설계 목적: 결재 문서 결성 시점에는 멀티파트 파일을 들고 있을 수 없으므로 Base64 텍스트로 직렬화하여 담아두었다가, 최종 실행 시점에 실물 파일로 파싱하여 업로드함
     *
     * @param data 이미지 원시 스트링 문자열이 내포된 데이터 맵
     * @return 클라우드 저장소에 저장이 완료되어 영구 접근 가능한 이미지 URL 경로 스트링
     */
    private String uploadPendingImage(Map<String, Object> data) {
        String base64      = (String) data.get("profileImageBase64");
        String contentType = (String) data.get("profileImageType");
        
        // 1. 인코딩된 이미지 텍스트가 유효하지 않으면 처리를 스킵한다.
        if (base64 == null || base64.isBlank()) return null;
        try {
            // 2. Base64 표준 디코더를 활용하여 메모리 버퍼 상의 byte 배열로 디코딩한다.
            byte[] bytes = Base64.getDecoder().decode(base64);

            // 3. 파일 서비스에 바이너리 스트림을 전달하여 클라우드 업로드 후 식별 URL을 받온다.
            return cloudinaryUploadService.uploadBytesToCloudinary(bytes, contentType);
        } catch (Exception e) {
            log.error("[AdminExecution] Cloudinary 업로드 실패: {}", e.getMessage(), e);
            return null;
        }
    }
}
