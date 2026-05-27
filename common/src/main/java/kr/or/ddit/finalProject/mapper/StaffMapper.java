package kr.or.ddit.finalProject.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import kr.or.ddit.finalProject.dto.employee.DepartmentDto;
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
}
