package kr.or.ddit.finalProject.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.paging.PaginationInfo;

import kr.or.ddit.finalProject.dto.employee.DepartmentDto;
import kr.or.ddit.finalProject.dto.employee.EmployeeDetailDto;
import kr.or.ddit.finalProject.dto.employee.EmployeeInfoDto;
import kr.or.ddit.finalProject.dto.employee.EmployeeSalaryDto;
import kr.or.ddit.finalProject.dto.employee.JobGradeDto;
import kr.or.ddit.finalProject.dto.member.MemberCreateLogDto;
import kr.or.ddit.finalProject.dto.member.MemberDto;
import kr.or.ddit.finalProject.dto.member.MemberWithdrawLogDto;

@Mapper
public interface StaffMapper {
    // 부서 리스트 조회
    List<DepartmentDto> selectDepartmentList();
    
    // 직급 리스트 조회
    List<JobGradeDto> selectJobGradeList();

    // 직원 등록
    void insertEmployee(MemberDto memberDto);

    // 직원 정보 저장
    void insertEmployeeInfo(EmployeeInfoDto employeeInfoDto);

    // 직원 급여 정보 저장
    void insertEmployeeSalary(EmployeeSalaryDto employeeSalary);

    // 직원 리스트 조회
    List<EmployeeDetailDto> selectEmployeeList();

    // 단일 직원 상세 조회 (수정 전 스냅샷용)
    EmployeeDetailDto selectEmployeeDetailByUserId(String userId);

    // 재직 중인 직원 리스트 조회 (ENABLE = 'Y')
    List<EmployeeDetailDto> selectActiveEmployeeList();

    // 입사 연도 목록 조회 (중복 제거)
    List<Integer> selectJoinYearList();

    // 직원 중복 조회
    int checkIdExists(String usrId);

    // 아이디 중복 자동 순번 발급 및 중복 회피
    String selectMaxUserId(String baseId);

    // 직원 계정 수정 (MEMBER)
    void updateMember(MemberDto memberDto);

    // 직원 인사 정보 수정 (EMPLOYEE_INFO)
    void updateEmployeeInfo(EmployeeInfoDto employeeInfoDto);

    // 현재 적용 급여 조회 (USE_YN = 'Y')
    EmployeeSalaryDto selectCurrentSalary(String userId);

    // 현재 적용 급여를 비활성화 (USE_YN = 'N')
    void deactivateCurrentSalary(String userId);

    // 학생 계정 수정 (MEMBER + USER_ROLE + ENABLE 포함)
    void updateStudentMember(MemberDto memberDto);

    // 퇴사 처리: MEMBER.ENABLE = 'N'
    int updateMemberDisabled(String userId);

    // 퇴사 처리: EMPLOYEE_INFO 상태, 퇴사일, 퇴사사유 변경
    int updateEmployeeRetired(@Param("userId") String userId, @Param("retmtRsn") String retmtRsn, @Param("loginUserId") String loginUserId);

    // 퇴사 처리: EMPLOYEE_SALARY 현재 급여 비활성화
    int updateEmployeeSalaryInactive(@Param("userId") String userId, @Param("loginUserId") String loginUserId);

    // 직원 목록 동적 검색 (서버 페이징)
    List<EmployeeDetailDto> searchEmployeeList(PaginationInfo<Map<String, Object>> paging);

    // 직원 전체 건수 (페이지 버튼 계산용)
    int countSearchEmployeeList(PaginationInfo<Map<String, Object>> paging);

    // 재직 중인 직원 동적 검색 (권한 설정 페이지용, 서버 페이징)
    List<EmployeeDetailDto> searchActiveEmployeeList(PaginationInfo<Map<String, Object>> paging);

    // 재직 중인 직원 전체 건수 (권한 설정 페이지용)
    int countSearchActiveEmployeeList(PaginationInfo<Map<String, Object>> paging);

    // 학생 목록 동적 검색 (서버 페이징)
    List<MemberDto> searchStudentList(PaginationInfo<Map<String, Object>> paging);

    // 학생 전체 건수 (페이지 버튼 계산용)
    int countSearchStudentList(PaginationInfo<Map<String, Object>> paging);

    // 학생 리스트 조회
    List<MemberDto> selectStudentList();

    // 가입 연도 목록 조회 (중복 제거)
    List<Integer> selectStudentJoinYearList();

    // 학생 로그 정보 저장 (MEMBER_CREATE_LOG)
    void insertStudentLog(MemberCreateLogDto memberCreateLog);

    // 학생 탈퇴 로그 정보 저장 (MEMBER_WITHDRAW_LOG)
    int updateMemberWithdrwa(MemberWithdrawLogDto withdrawLog);
}
