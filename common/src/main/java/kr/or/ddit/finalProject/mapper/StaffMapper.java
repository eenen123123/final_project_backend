package kr.or.ddit.finalProject.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import kr.or.ddit.finalProject.dto.employee.DepartmentDto;
import kr.or.ddit.finalProject.dto.employee.EmployeeDetailDto;
import kr.or.ddit.finalProject.dto.employee.EmployeeInfoDto;
import kr.or.ddit.finalProject.dto.employee.EmployeeSalaryDto;
import kr.or.ddit.finalProject.dto.employee.JobGradeDto;
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
}
