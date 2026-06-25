package kr.or.ddit.finalProject.service.staff;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.paging.PaginationInfo;

import kr.or.ddit.finalProject.dto.employee.DepartmentDto;
import kr.or.ddit.finalProject.dto.employee.EmployeeDetailDto;
import kr.or.ddit.finalProject.dto.employee.EmployeeInfoDto;
import kr.or.ddit.finalProject.dto.employee.EmployeeSalaryDto;
import kr.or.ddit.finalProject.dto.employee.JobGradeDto;
import kr.or.ddit.finalProject.dto.member.MemberCreateLogDto;
import kr.or.ddit.finalProject.dto.member.MemberDto;
import kr.or.ddit.finalProject.dto.leave.AnnualLeaveHistoryDto;
import kr.or.ddit.finalProject.dto.leave.LeaveBalanceDto;


/**
 * StaffService
 * 
 * ✔ 인사 관리(HR) 시스템의 핵심 비즈니스 요구사항을 정의하는 명세서
 * 
 * ✔ 역할 요약
 * ---------------------------------------------------------------------------
 * - 신규 직원 등록 및 프로필 이미지 업로드 처리
 * - 직원 기본 정보, 인사 정보, 급여 정보의 통합 관리(CUD)
 * - 인사 대시보드 구현을 위한 조회 및 검색 메타데이터 제공
 * - 퇴사 처리 및 보안을 위한 계정 상태 제어
 * 
 * ✔ 설계 목적
 * ---------------------------------------------------------------------------
 * 1. 인사 데이터의 무결성을 보장하기 위해 다중 테이블 복합 비즈니스 로직 정의
 * 2. 상위 컨트롤러와 하위 데이터 접근 계층(Mapper) 간의 느슨한 결합 제공
 * 3. 기획서상의 인사 관리 유즈케이스(Use Case)를 단일 인터페이스로 응집
 * 
 * ✔ 아키텍처 위치 (Service Interface Layer)
 * ---------------------------------------------------------------------------
 * [StaffController] -> [StaffService (Specification)]
 * ↑ (Implements)
 * [StaffServiceImpl] -> [StaffMapper]
 */
public interface StaffService {

    /**
     * 부서 리스트 조회
     * 
     * ✔ 사용 시나리오: 직원 등록 폼이나 검색 필터의 부서 선택(Select Box) 목록을 동적으로 구성할 때 호출한다.
     * @return 시스템에 등록된 전체 부서 정보 DTO 리스트
     */
    List<DepartmentDto> retrieveDepartmentList();

    void addDepartment(DepartmentDto dept);

    void modifyDepartment(DepartmentDto dept);

    void toggleDeptUseYn(String deptCd, String useYn, String loginUserId);

    /**
     * 직급 리스트 조회
     * 
     * ✔ 사용 시나리오: 직원 등록 폼이나 검색 필터의 직급 선택 목록을 동적으로 구성할 때 호출한다.
     * @return 시스템에 등록된 전체 직급 정보 DTO 리스트
     */
    List<JobGradeDto> retrieveJobGradeList();

    List<JobGradeDto> retrieveAllJobGradeList();

    void addJobGrade(JobGradeDto jbgr);

    void modifyJobGrade(JobGradeDto jbgr);

    void toggleJbgrUseYn(String jbgrCd, String useYn, String loginUserId);

    void assignMntUserId(String userId, String mntUserId, String loginUserId);

    /** 직급 목록 DB 페이징+필터 */
    Map<String, Object> searchJobGradeList(String deptCd, String useYn, int page, int size);

    /**
     * 신규 직원 통합 등록 (계정 + 인사 + 급여 + 프로필 파일)
     * 
     * ✔ 사용 시나리오
     * --------------------------------------------------------------------------------
     * - 인사 관리자가 신규 직원을 시스템에 등록할 때 사용한다.
     * - 파일 업로드 시스템 및 다중 테이블 프랜잭션 처리가 수반되어야 한다.
     * 
     * @param memberDto         사용자 계정 기본 정보 (MEMBER)
     * @param employeeInfoDto   직원 상세 인사 정보 (EMPLOYEE_INFO)
     * @param employeeSalaryDto 직원 급여 정보 (EMPLOYEE_SALARY)
     * @param profileImage      cloudinary에 저장할 프로필 이미지 파일 (MultipartFile)
     * @param loginAdminId      해당 등록 작업을 수행할 이력 추적용 관리자 ID
     */
    void registerEmployee(MemberDto memberDto, EmployeeInfoDto employeeInfoDto,
            EmployeeSalaryDto employeeSalaryDto, String profileUrl, String loginAdminId);

    /**
     * 인사 대시보드용 전체 직원 목록 조회
     *
     * ✔ 사용 시나리오: 관리자 페이지 직원 메인 테이블에 재직자 및 퇴사자를 아우르는 상세 데이터를 출력할 때 사용한다.
     * @return 테이블 결합(JOIN)을 통해 확보된 직원 상세 정보 DTO 리스트
     */
    List<EmployeeDetailDto> retrieveEmployeeList();

    /** 상태별 직원 수 조회 (stats 카드용) */
    Map<String, Object> getEmployeeStatusCounts();

    /** 단일 직원 상세 조회 (수정 전 스냅샷용) */
    EmployeeDetailDto retrieveEmployeeDetailById(String userId);

    /**
     * 재직 중인 직원 목록 조회 (권한 설정 페이지용)
     *
     * ✔ 사용 시나리오: 접속 이력/권한 관리 테이블에 퇴사자를 제외한 재직자만 표시할 때 사용한다.
     * @return ENABLE = 'Y' 인 재직 직원 상세 정보 DTO 리스트
     */
    List<EmployeeDetailDto> retrieveActiveEmployeeList();

    /**
     * 시스템 등록 직원의 전체 입사 연도 고유 목록 조회
     * 
     * ✔ 사용 시나리오: 직원 목록 검색 조건 중 '입사 연도' 필터 항목을 동적으로 채우기 위해 사용한다.
     * @return 중복이 제거된 입사 연도(Integer) 리스트
     */
    List<Integer> retrieveJoinYearList();

    /**
     * 고유 아이디 생성을 위한 자동 순번 발급 및 중복 검증
     * 
     * ✔ 사용 시나리오: 신규 등록 시 규칙성 있는 사번 형태의 아이디를 자동 제안하거나 중복을 회피하기 위해 호출한다.
     * @param baseId        기준이 되는 기본 아이디 포맷
     * @param defaultSerial 중복 시 결합할 초기 순번 문자열
     * @return 중복이 검증되어 즉시 사용 가능한 고유 아이디 문자열
     */
    String getNextAvailableId(String baseId, String defaultSerial);

    /**
     * 직원 상세 정보 수정 (계정 + 인사 + 급여 통합 갱싱)
     * 
     * ✔ 사용 시나리오
     * -------------------------------------------------------------------------------------
     * - 모달 및 상세 페이지에서 직원의 신상 정보, 직급, 부서, 급여 등을 수정할 때 사용한다.
     * - 데이터 원자성 보장을 위해 단일 트랜잭션 안에서 처리가 완료되어야 한다.
     * 
     * @param memberDto         변경할 사용자 계정 정보
     * @param employeeInfoDto   변경할 인사 정보
     * @param employeeSalaryDto 변경할 급여 정보
     * @param loginAdminId      작업을 수행한 관리자 ID (이력 기록용)
     */
    void updateEmployee(MemberDto memberDto, EmployeeInfoDto employeeInfoDto,
            EmployeeSalaryDto employeeSalaryDto, String loginAdminId);

    /**
     * 직원 퇴사 처리 (계정 및 인사 정보 비활성화)
     * 
     * ✔ 사용 시나리오
     * -------------------------------------------------------------------------------------
     * - 재직 중인 직원의 상태를 '퇴사'로 변경하고 시스템 접근 권한을 박탈할 때 사용한다.
     * - 물리적 삭제(DELETE)가 아닌 논리적 삭제(UPDATE) 및 퇴사 사유 기록을 원칙으로 한다.
     * 
     * @param userId        퇴사 처리할 대상 직원의 고유 계정 ID
     * @param retmtRsn      퇴사 사유 명목 문자열
     * @param loginUserId   작업을 진행한 관리자 ID
     */
    void retireEmployee(String userId, String retmtRsn, String loginUserId);

    /**
     * 직원 목록 동적 검색 + 서버 페이징 (keyword, year, status, deptCd, jbgrCd, emplTypeCd, page, screenSize)
     */
    PageResponse<EmployeeDetailDto> searchEmployeeList(PaginationInfo<Map<String, Object>> paging);

    /**
     * 재직 중인 직원 동적 검색 + 서버 페이징 (권한 설정 페이지 전용: keyword, deptCd, jbgrNm, online)
     */
    PageResponse<EmployeeDetailDto> searchActiveEmployeeList(
            PaginationInfo<Map<String, Object>> paging);

    /**
     * 학생 목록 동적 검색 (keyword, year, userRole, enable)
     */
    PageResponse<MemberDto> searchStudentList(PaginationInfo<Map<String, Object>> paging);

    /**
     * 학생 리스트 조회
     * @return
     */
    List<MemberDto> retrieveStudentList();

    MemberDto retrieveStudentById(String userId);

    /**
     * 시스템 등록 학생의 전체 가입 연도 고유 목록 조회
     * 
     * ✔ 사용 시나리오: 학생 목록 검색 조건 중 '가입 연도' 필터 항목을 동적으로 채우기 위해 사용한다.
     * @return 중복이 제거된 가입 연도(Integer) 리스트
     */
    List<Integer> retrieveMemberJoinYearList();

    /**
     * 신규 학생 통합 등록 (계정 + 프로필 파일)
     * 
     * @param memberDto
     * @param profileUrl
     * @param loginAdmin
     */
    void registerStudent(MemberDto memberDto, MemberCreateLogDto memberCreateLog, String profileUrl,
            String loginAdmin);

    /**
     * 학생 상세 정보 수정 (계정 + 프로필 파일)
     */
    void updateStudent(MemberDto memberDto, String loginAdminId);

    /**
     * 학생 탈퇴 처리 (계정 및 인사 정보 비활성화)
     */
    void retireStudent(String userId, String withdrawRsn, String loginUserId);

    // ── 휴가 현황 / 잔여 연차 (조회 전용, 서버 페이징/필터) ──

    /** 승인 적재된 휴가 사용 현황 (서버 페이징/필터) */
    PageResponse<AnnualLeaveHistoryDto> searchLeaveHistory(PaginationInfo<Map<String, Object>> paging);

    /** 직급 연차 - 연도별 사용 연차 = 잔여 연차 현황 (서버 페이징/필터) */
    PageResponse<LeaveBalanceDto> searchLeaveBalance(PaginationInfo<Map<String, Object>> paging);

    /** 휴가 현황 상단 요약 카드 데이터 (오늘/이번달/올해/예정 건수) */
    Map<String, Object> getLeaveSummary();

    /** 휴가 결재 승인 시 휴가 이력 적재 */
    void insertLeaveHistory(AnnualLeaveHistoryDto dto);
}
