package kr.or.ddit.finalProject.service.staff;

import java.util.List;

import kr.or.ddit.finalProject.dto.employee.DepartmentDto;
import kr.or.ddit.finalProject.dto.employee.EmployeeInfoDto;
import kr.or.ddit.finalProject.dto.employee.EmployeeSalaryDto;
import kr.or.ddit.finalProject.dto.employee.JobGradeDto;
import kr.or.ddit.finalProject.dto.member.MemberDto;

public interface  StaffService {

    // 부서 리스트 조회
    List<DepartmentDto> retrieveDepartmentList();

    // 직급 리스트 조회
    List<JobGradeDto> retrieveJobGradeList();

    // 직원 등록
    void registerEmployee(MemberDto memberDto);

    // 직원 정보 저장
    void saveEmployeeInfo(EmployeeInfoDto employeeInfoDto);

    // 직원 급여 정보 저장
    void saveEmployeeSalary(EmployeeSalaryDto employeeSalaryDto);
}
