package kr.or.ddit.finalProject.service.staff;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import kr.or.ddit.finalProject.dto.employee.DepartmentDto;
import kr.or.ddit.finalProject.dto.employee.EmployeeDetailDto;
import kr.or.ddit.finalProject.dto.employee.EmployeeInfoDto;
import kr.or.ddit.finalProject.dto.employee.EmployeeSalaryDto;
import kr.or.ddit.finalProject.dto.employee.JobGradeDto;
import kr.or.ddit.finalProject.dto.member.MemberDto;

public interface  StaffService {

    // 부서 리스트 조회
    List<DepartmentDto> retrieveDepartmentList();

    // 직급 리스트 조회
    List<JobGradeDto> retrieveJobGradeList();

    // 직원 등록, 직원 정보 저장, 직원 급여 정보 저장
    void registerEmployee(MemberDto memberDto, EmployeeInfoDto employeeInfoDto, EmployeeSalaryDto employeeSalaryDto, MultipartFile profileImage, String loginAdminId);

    // 직원 리스트 조회
    List<EmployeeDetailDto> retrieveEmployeeList();

    // 입사 연도 목록 조회
    List<Integer> retrieveJoinYearList();

    // 아이디 중복 자동 순번 발급 및 중복 회피
    String getNextAvailableId(String baseId, String defaultSerial);

    // 직원 계정 수정 (MEMBER + EMPLOYEE_INFO + EMPLOYEE_SALARY 트랜잭션)
    void updateEmployee(MemberDto memberDto, EmployeeInfoDto employeeInfoDto, EmployeeSalaryDto employeeSalaryDto, String loginAdminId);
}
