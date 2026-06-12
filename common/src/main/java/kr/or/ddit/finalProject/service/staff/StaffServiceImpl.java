package kr.or.ddit.finalProject.service.staff;

import java.util.List;
import java.util.Map;

import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.paging.PaginationInfo;
import kr.or.ddit.finalProject.dto.leave.AnnualLeaveHistoryDto;
import kr.or.ddit.finalProject.dto.leave.LeaveBalanceDto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Validator;
import kr.or.ddit.finalProject.dto.employee.DepartmentDto;
import kr.or.ddit.finalProject.dto.employee.EmployeeDetailDto;
import kr.or.ddit.finalProject.dto.employee.EmployeeInfoDto;
import kr.or.ddit.finalProject.dto.employee.EmployeeSalaryDto;
import kr.or.ddit.finalProject.dto.employee.JobGradeDto;
import kr.or.ddit.finalProject.dto.member.MemberCreateLogDto;
import kr.or.ddit.finalProject.dto.member.MemberDto;
import kr.or.ddit.finalProject.dto.member.MemberWithdrawLogDto;
import kr.or.ddit.finalProject.dto.user.SignupRequestRecord;
import kr.or.ddit.finalProject.exception.ErrorCode;
import kr.or.ddit.finalProject.exception.FinalProjectException;
import kr.or.ddit.finalProject.mapper.StaffMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StaffServiceImpl implements StaffService{

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    Validator validator;

    private final StaffMapper staffMapper;

    @Override public List<DepartmentDto> retrieveDepartmentList()  { return staffMapper.selectDepartmentList(); }
    @Override public void addDepartment(DepartmentDto dept)        { staffMapper.insertDepartment(dept); }
    @Override public void modifyDepartment(DepartmentDto dept)     { staffMapper.updateDepartment(dept); }
    @Override public void toggleDeptUseYn(String deptCd, String useYn, String loginUserId) {
        staffMapper.toggleDeptUseYn(deptCd, useYn, loginUserId);
    }

    @Override public List<JobGradeDto> retrieveJobGradeList()      { return staffMapper.selectJobGradeList(); }
    @Override public List<JobGradeDto> retrieveAllJobGradeList()   { return staffMapper.selectAllJobGradeDtos(); }
    @Override public void addJobGrade(JobGradeDto jbgr)            { staffMapper.insertJobGrade(jbgr); }
    @Override public void modifyJobGrade(JobGradeDto jbgr)         { staffMapper.updateJobGrade(jbgr); }
    @Override public void toggleJbgrUseYn(String jbgrCd, String useYn, String loginUserId) {
        staffMapper.toggleJbgrUseYn(jbgrCd, useYn, loginUserId);
    }
    @Override public void assignMntUserId(String userId, String mntUserId, String loginUserId) {
        staffMapper.updateMntUserId(userId, mntUserId, loginUserId);
    }

    @Override
    public Map<String, Object> searchJobGradeList(String deptCd, String useYn, int page, int size) {
        Map<String, Object> params = new java.util.HashMap<>();
        params.put("deptCd", (deptCd != null && !deptCd.isBlank()) ? deptCd : null);
        params.put("useYn",  (useYn  != null && !useYn.isBlank())  ? useYn  : null);
        params.put("offset", (page - 1) * size);
        params.put("size",   size);
        List<JobGradeDto> list  = staffMapper.selectJobGradeListPaged(params);
        int               total = staffMapper.countJobGradeListPaged(params);
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("list",       list);
        result.put("total",      total);
        result.put("page",       page);
        result.put("size",       size);
        result.put("totalPages", (int) Math.ceil((double) total / size));
        return result;
    }

    /**
     * 직원 등록, 직원 정보 저장, 직원 급여 정보 저장
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 예외 발생시 롤백
    public void registerEmployee(MemberDto memberDto, EmployeeInfoDto employeeInfoDto, EmployeeSalaryDto employeeSalaryDto, String profileUrl, String loginAdminId) {

        // 1. MemberDto에 데이터 넣기
        propareMemberForRegister(memberDto, profileUrl, "ROLE_ADMIN");

        // 2. EmployeeInfoDto에 데이터 넣기
        employeeInfoDto.setRgtrId(loginAdminId); // 최초등록자ID -> 현재 로그인한 관리자 ID
        employeeInfoDto.setLastMdfrId(loginAdminId); // 최종등록자ID -> 현재 로그인한 관리자 ID

        // 3. EmployeeSalaryDto 데이터 넣기
        employeeSalaryDto.setUserId(memberDto.getUserId());
        employeeSalaryDto.setUseYn("Y"); // 첫 등록이므로 현재 사용 여부는 무조건 'Y'로 설정
        employeeSalaryDto.setApplyYmd(employeeInfoDto.getJoinYmd()); // 급여 적용 시작일은 입사일과 동일하게 설정
        employeeSalaryDto.setRgtrId(loginAdminId); // 최초등록자ID -> 현재 로그인한 관리자 ID
        employeeSalaryDto.setLastMdfrId(loginAdminId); // 최종등록자ID -> 현재 로그인한 관리자 ID


        log.info("등록할 직원 정보: {}", memberDto);
        log.info("등록할 직원 상세 정보: {}", employeeInfoDto);
        log.info("등록할 직원 급여 정보: {}", employeeSalaryDto);

        /**
         * 일괄 트랜잭션 등록 처리
         * 1. 직원 등록 (MemberDto) -> 회원 마스터 테이블(MEMBER)에 INSERT
         * 2. 직원 상세 정보 저장 (EmployeeInfoDto) -> 직원 인사 관리 테이블(EMPLOYEE_INFO)에 INSERT
         * 3. 직원 급여 정보 저장 (EmployeeSalaryDto) -> 직원 급여 테이블(EMPLOYEE_SALARY)에 INSERT
         */
        try {
            staffMapper.insertEmployee(memberDto);
            staffMapper.insertEmployeeInfo(employeeInfoDto);
            staffMapper.insertEmployeeSalary(employeeSalaryDto);
        } catch (DataAccessException e) {
            log.error("[registerEmployee] DB INSERT 실패. userId={}, cause={}", memberDto.getUserId(), e.getMessage());
            throw new FinalProjectException(ErrorCode.EMPLOYEE_REGISTER_FAILED, e);
        }
    }

    /**
     * 직원 리스트 조회
     */
    @Override
    public List<EmployeeDetailDto> retrieveEmployeeList() {
        return staffMapper.selectEmployeeList();
    }

    @Override
    public EmployeeDetailDto retrieveEmployeeDetailById(String userId) {
        return staffMapper.selectEmployeeDetailByUserId(userId);
    }

    /**
     * 재직 중인 직원 리스트 조회
     */
    @Override
    public List<EmployeeDetailDto> retrieveActiveEmployeeList() {
        return staffMapper.selectActiveEmployeeList();
    }

    /**
     * 입사 연도 목록 조회
     */
    @Override
    public List<Integer> retrieveJoinYearList() {
        return staffMapper.selectJoinYearList();
    }

    /**
     * 아이디 중복 자동 순번 발급 및 중복 회피
     * defaultSerial 길이로 시리얼 자릿수를 자동 감지 (직원: "01" 2자리, 학생: "00001" 5자리)
     */
    @Override
    public String getNextAvailableId(String baseId, String defaultSerial) {
        int serialLen = defaultSerial.length(); // 직원=2, 학생=5
        String maxId = staffMapper.selectMaxUserId(baseId);

        if (maxId == null) {
            return baseId + defaultSerial;
        }

        try {
            String lastDigits = maxId.substring(maxId.length() - serialLen);
            int nextSerialInt = Integer.parseInt(lastDigits) + 1;
            String nextSerial = String.format("%0" + serialLen + "d", nextSerialInt);
            return baseId + nextSerial;
        } catch (NumberFormatException e) {
            log.error("[getNextAvailableId] ID 끝 {}자리 숫자 파싱 실패. maxId={}", serialLen, maxId);
            throw new FinalProjectException(ErrorCode.EMPLOYEE_ID_GENERATE_FAILED, e);
        }
    }

    /**
     * 직원 계정 수정 (MEMBER + EMPLOYEE_INFO + EMPLOYEE_SALARY 트랜잭션)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateEmployee(MemberDto memberDto, EmployeeInfoDto employeeInfoDto, EmployeeSalaryDto employeeSalaryDto, String loginAdminId) {
        // 연락처 하이픈 제거
        if (memberDto.getUserTelno() != null) {
            memberDto.setUserTelno(memberDto.getUserTelno().replaceAll("-", ""));
        }

        employeeInfoDto.setLastMdfrId(loginAdminId);

        try {
            staffMapper.updateMember(memberDto);

            staffMapper.updateEmployeeInfo(employeeInfoDto);

            // 급여 변경 시에만 이력 적립
            EmployeeSalaryDto currentSalary = staffMapper.selectCurrentSalary(memberDto.getUserId());
            boolean salaryChanged = currentSalary == null || !employeeSalaryDto.getBaseSalary().equals(currentSalary.getBaseSalary());

            if(salaryChanged) {
                employeeSalaryDto.setUserId(memberDto.getUserId());
                employeeSalaryDto.setUseYn("Y");
                employeeSalaryDto.setApplyYmd(java.time.LocalDate.now());
                employeeSalaryDto.setRgtrId(loginAdminId);
                employeeSalaryDto.setLastMdfrId(loginAdminId);

                staffMapper.deactivateCurrentSalary(memberDto.getUserId());
                staffMapper.insertEmployeeSalary(employeeSalaryDto);
            }
        } catch (DataAccessException e) {
            log.error("[updateEmployee] DB 수정 실패. userId={}, cause={}", memberDto.getUserId(), e.getMessage());
            throw new FinalProjectException(ErrorCode.EMPLOYEE_REGISTER_FAILED, e);
        }
    }

    private void validateMemberInput(MemberDto dto) {
        SignupRequestRecord record = new SignupRequestRecord(
            dto.getUserId(),
            dto.getUserEnpswd(),
            dto.getUserName(),
            dto.getUserGndrCd(),
            dto.getUserBrdt(),
            dto.getUserTelno(),
            dto.getUserEmailAddr(),
            dto.getUserZip(),
            dto.getUserAddr(),
            dto.getUserDaddr(),
            dto.getUserEnrrno()
        );

        var violations = validator.validate(record);
        if (!violations.isEmpty()) {
            String message = violations.iterator().next().getMessage();
            log.warn("[registerEmployee] 유효성 검사 실패: {}", message);
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 직원 퇴사 처리 (MEMBER + EMPLOYEE_INFO + EMPLOYEE_SALARY 비활성화)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void retireEmployee(String userId, String retmtRsn, String loginUserId) {
        if (userId == null || userId.isBlank()) {
            throw new FinalProjectException(ErrorCode.BAD_REQUEST);
        }
        if (retmtRsn == null || retmtRsn.isBlank() || retmtRsn.length() > 1000) {
            throw new FinalProjectException(ErrorCode.BAD_REQUEST);
        }

        try {
            // MEMBER.ENABLE = 'N'
            int memberResult = staffMapper.updateMemberDisabled(userId);
            if (memberResult != 1) {
                // 이미 퇴사된 직원
                throw new FinalProjectException(ErrorCode.EMPLOYEE_ALREADY_RETIRED);
            }

            int infoResult = staffMapper.updateEmployeeRetired(userId, retmtRsn, loginUserId);
            if (infoResult != 1) {
                // 이미 퇴사된 직원
                throw new FinalProjectException(ErrorCode.EMPLOYEE_ALREADY_RETIRED);
            }

            int salaryResult = staffMapper.updateEmployeeSalaryInactive(userId, loginUserId);
            if (salaryResult > 1) {
                // 직원 퇴사 처리 실패
                throw new FinalProjectException(ErrorCode.EMPLOYEE_RETIRE_FAILED);
            }

            log.info("[retirEmployee] 퇴사 처리 완료. userId={}", userId);
        } catch (DataAccessException e) {
            log.error("[retireEmployee] DB 처리 실패. userId={}, cause={}", userId, e.getMessage());
            throw new FinalProjectException(ErrorCode.EMPLOYEE_RETIRE_FAILED, e);
        }

    }

    @Override
    public PageResponse<EmployeeDetailDto> searchEmployeeList(PaginationInfo<Map<String, Object>> paging) {
        List<EmployeeDetailDto> items = staffMapper.searchEmployeeList(paging);
        int totalCount = staffMapper.countSearchEmployeeList(paging);
        return new PageResponse<>(items, totalCount);
    }

    @Override
    public PageResponse<EmployeeDetailDto> searchActiveEmployeeList(PaginationInfo<Map<String, Object>> paging) {
        List<EmployeeDetailDto> items = staffMapper.searchActiveEmployeeList(paging);
        int totalCount = staffMapper.countSearchActiveEmployeeList(paging);
        return new PageResponse<>(items, totalCount);
    }

    @Override
    public PageResponse<MemberDto> searchStudentList(PaginationInfo<Map<String, Object>> paging) {
        List<MemberDto> items = staffMapper.searchStudentList(paging);
        int totalCOunt = staffMapper.countSearchStudentList(paging);
        return new PageResponse<>(items, totalCOunt);
    }

    /**
     * 학생 리스트 조회
     */
    @Override
    public List<MemberDto> retrieveStudentList() {
        return staffMapper.selectStudentList();
    }

    @Override
    public MemberDto retrieveStudentById(String userId) {
        return staffMapper.selectStudentByUserId(userId);
    }

    /**
     * 가입 연도 목록 조회
     */
    @Override
    public List<Integer> retrieveMemberJoinYearList() {
        return staffMapper.selectStudentJoinYearList();
    }

    /**
     * 신규 학생 통합 등록 (계정 + 프로필 파일)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void registerStudent(MemberDto memberDto, MemberCreateLogDto memberCreateLog, String profileUrl, String loginAdmin) {
        // 1. MemberDto에 데이터 넣기
        propareMemberForRegister(memberDto, profileUrl, "ROLE_STUDENT");
        
        // 2. memberCreateLog에 데이터넣기
        memberCreateLog.setRgtrId(loginAdmin);     // 최초등록자ID -> 현재 로그인한 관리자 ID
        memberCreateLog.setLastMdfrId(loginAdmin); // 최종등록자ID -> 현재 로그인한 관리자 ID

        log.info("등록할 회원 정보: {}", memberDto);
        log.info("등록할 학생 로그: {}", memberCreateLog);

        /**
         * 일괄 트랜잭션 등록 처리
         * 1. 회원 등록 (MemberDto) -> 회원 마스터 테이블(MEMBER)에 INSERT
         * 2. 회원 로그 정보 저장 (MemberCreateLog) -> 회원 마스터 테이블(MEMBER_CREATE_LOG)에 INSERT
         */
        try {
            staffMapper.insertEmployee(memberDto);
            staffMapper.insertStudentLog(memberCreateLog);
        } catch (DataAccessException e) {
            log.error("[registerStudent] DB INSERT 실패, userId={}, cause={}", memberDto.getUserId(), e.getMessage());
            throw new FinalProjectException(ErrorCode.MEMBER_ID_GENETATE_FAILED, e);
        }

    }   

    /**
     * 학생 정보 수정 (MEMBER: 기본정보 + USER_ROLE + ENABLE)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStudent(MemberDto memberDto, String loginAdminId) {
        if (memberDto.getUserTelno() != null) {
            memberDto.setUserTelno(memberDto.getUserTelno().replaceAll("-", ""));
        }
        try {
            staffMapper.updateStudentMember(memberDto);
        } catch (DataAccessException e) {
            log.error("[updateStudent] DB 수정 실패. userId={}, cause={}", memberDto.getUserId(), e.getMessage());
            throw new FinalProjectException(ErrorCode.EMPLOYEE_REGISTER_FAILED, e);
        }
    }

    /**
     * 회원 공통 등록 전처리
     */
    private void propareMemberForRegister(MemberDto memberDto, String profileUrl, String userRole) {

        // 0. 유효성 검사 (암호화·가공 이전 원본값 기준)
        validateMemberInput(memberDto);

        // 1. 권한 설정
        memberDto.setUserRole(userRole); 

        // 2. 기본 프로필 설정
        memberDto.setUserProfile(profileUrl);

        // 3. 비밀번호 암호화
        memberDto.setUserEnpswd(passwordEncoder.encode(memberDto.getUserEnpswd()));

        // 4. 주민등록번호 암호화
        if (hasText(memberDto.getUserEnrrno())) {
            memberDto.setUserEnrrno(passwordEncoder.encode(memberDto.getUserEnrrno()));
        }

        // 5. 연락처 하이픈 제거
        memberDto.setUserTelno(removeHyphen(memberDto.getUserTelno()));

        // 6. ID 생성
        String uniqueUserId = generateUniqueUserId(memberDto.getUserId());
        memberDto.setUserId(uniqueUserId);
    }

    /**
     * USER_ID 중복 체크 후 최종 ID 생성
     * 학생 ID 패턴 (\d{2}S\d{5}) 자동 감지 → 시리얼 5자리 처리
     * 직원 ID 패턴 그 외 → 기존 시리얼 2자리 처리
     */
    private String generateUniqueUserId(String initialUserId) {

        if (initialUserId == null || initialUserId.isBlank()) {
            log.error("[generateUniqueUserId] USER_ID가 비어있습니다.");
            throw new FinalProjectException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }

        // 학생 ID 형식: {YY}S{5자리} (예: 26S00001, 총 8자리)
        boolean isStudentId = initialUserId.matches("\\d{2}S\\d{5}");
        int serialLen = isStudentId ? 5 : 2;
        int baseLen   = initialUserId.length() - serialLen;

        if (baseLen <= 0) {
            log.error("[generateUniqueUserId] 잘못된 USER_ID 형식, initialUserId={}", initialUserId);
            throw new FinalProjectException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }

        String generatedId = initialUserId;
        String baseId      = generatedId.substring(0, baseLen); // 직원: "202605KH", 학생: "26S"

        boolean isDuplicate = true;
        int safetyCount = 0;

        while (isDuplicate && safetyCount < 100) {
            int count = staffMapper.checkIdExists(generatedId);

            if (count == 0) {
                isDuplicate = false;
            } else {
                String maxId = staffMapper.selectMaxUserId(baseId);
                String lastDigits = maxId.substring(maxId.length() - serialLen);
                int nextSerialInt = Integer.parseInt(lastDigits) + 1;
                String nextSerial = String.format("%0" + serialLen + "d", nextSerialInt);
                generatedId = baseId + nextSerial;
                safetyCount++;
            }
        }

        if (isDuplicate) {
            log.error("[generateUniqueUserId] ID 생성 실패 - 100회 시도 소진. baseId={}", baseId);
            throw new FinalProjectException(ErrorCode.USER_ID_ALREADY_EXISTS);
        }

        return generatedId;
    }

    /**
     * 전화번호 하이픈 제거
     */
    private String removeHyphen(String value) {
        if (value == null) {
            return null;
        }

        return value.replace("-", "");
    }

    /**
     * 문자열 값 존재 여부 체크
     */
    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * 학생 탈퇴 처리
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void retireStudent(String userId, String withdrawRsn, String loginUserId) {
        if (userId == null || userId.isBlank()) {
            throw new FinalProjectException(ErrorCode.BAD_REQUEST);
        }
        if (withdrawRsn == null || withdrawRsn.isBlank() || withdrawRsn.length() > 1000) {
            throw new FinalProjectException(ErrorCode.BAD_REQUEST);
        }

        try {
            // MEMBER.ENABLE = 'N'
            int memberResult = staffMapper.updateMemberDisabled(userId);
            if (memberResult != 1) {
                // 이미 탈퇴된 회원
                throw new FinalProjectException(ErrorCode.MEMBER_ALREADY_RETIRED);
            }

            MemberWithdrawLogDto withdrawLog = MemberWithdrawLogDto.builder()
                    .userId(userId)
                    .withdrawRsn(withdrawRsn)
                    .rgtrId(loginUserId)
                    .lastMdfrId(loginUserId)
                    .build();
            int infoResult = staffMapper.updateMemberWithdrwa(withdrawLog);
            if (infoResult > 1) {
                // 회원 탈퇴 처리 실패
                throw new FinalProjectException(ErrorCode.MEMBER_RETIRE_FAILED);
            }

            log.info("[retireStudent] 탈퇴 처리 완료. userId={}", userId);
            
        } catch (DataAccessException e) {
            log.error("[retireStudent] DB 처리 실패. userId={}, cause={}", userId, e.getMessage());
            throw new FinalProjectException(ErrorCode.MEMBER_RETIRE_FAILED, e);
        }
    }

    // ── 휴가 현황 / 잔여 연차 (조회 전용) ──

    @Override
    public PageResponse<AnnualLeaveHistoryDto> searchLeaveHistory(PaginationInfo<Map<String, Object>> paging) {
        List<AnnualLeaveHistoryDto> items = staffMapper.searchLeaveHistory(paging);
        int totalCount = staffMapper.countLeaveHistory(paging);
        return new PageResponse<>(items, totalCount);
    }

    @Override
    public PageResponse<LeaveBalanceDto> searchLeaveBalance(PaginationInfo<Map<String, Object>> paging) {
        List<LeaveBalanceDto> items = staffMapper.searchLeaveBalance(paging);
        int totalCount = staffMapper.countLeaveBalance(paging);
        return new PageResponse<>(items, totalCount);
    }

    @Override
    public Map<String, Object> getLeaveSummary() {
        return staffMapper.selectLeaveSummary();
    }

    @Override
    public void insertLeaveHistory(AnnualLeaveHistoryDto dto) {
        staffMapper.insertLeaveHistory(dto);
    }
}
