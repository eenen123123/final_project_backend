package kr.or.ddit.finalProject.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.employee.DepartmentDto;
import kr.or.ddit.finalProject.dto.employee.EmployeeDetailDto;
import kr.or.ddit.finalProject.dto.employee.EmployeeInfoDto;
import kr.or.ddit.finalProject.dto.employee.EmployeeSalaryDto;
import kr.or.ddit.finalProject.dto.employee.JobGradeDto;
import kr.or.ddit.finalProject.dto.member.MemberCreateLogDto;
import kr.or.ddit.finalProject.dto.member.MemberDto;

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

    // 퇴사 처리: MEMBER.ENABLE = 'N'
    int updateMemberDisabled(String userId);

    // 퇴사 처리: EMPLOYEE_INFO 상태, 퇴사일, 퇴사사유 변경
    int updateEmployeeRetired(@Param("userId") String userId, @Param("retmtRsn") String retmtRsn, @Param("loginUserId") String loginUserId);

    // 퇴사 처리: EMPLOYEE_SALARY 현재 급여 비활성화
    int updateEmployeeSalaryInactive(@Param("userId") String userId, @Param("loginUserId") String loginUserId);

    // 학생 리스트 조회
    List<MemberDto> selectStudentList();

    // 가입 연도 목록 조회 (중복 제거)
    List<Integer> selectStudentJoinYearList();

    // 학생 로그 정보 저장 (MEMBER_CREATE_LOG)
    void insertStudentLog(MemberCreateLogDto memberCreateLog);
}
