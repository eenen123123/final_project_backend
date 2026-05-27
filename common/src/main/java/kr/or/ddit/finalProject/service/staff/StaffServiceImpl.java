package kr.or.ddit.finalProject.service.staff;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.ddit.finalProject.dto.employee.DepartmentDto;
import kr.or.ddit.finalProject.dto.employee.EmployeeDetailDto;
import kr.or.ddit.finalProject.dto.employee.EmployeeInfoDto;
import kr.or.ddit.finalProject.dto.employee.EmployeeSalaryDto;
import kr.or.ddit.finalProject.dto.employee.JobGradeDto;
import kr.or.ddit.finalProject.dto.member.MemberDto;
import kr.or.ddit.finalProject.mapper.StaffMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StaffServiceImpl implements StaffService{

    private final StaffMapper staffMapper;

    // 부서 리스트 조회
    @Override
    public List<DepartmentDto> retrieveDepartmentList() {
        return staffMapper.selectDepartmentList();
    }

    // 직급 리스트 조회
    @Override
    public List<JobGradeDto> retrieveJobGradeList() {
        return staffMapper.selectJobGradeList();
    }

    // 직원 등록
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void registerEmployee(MemberDto memberDto) {
        staffMapper.insertEmployee(memberDto);
    }

    // 직원 정보 저장
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveEmployeeInfo(EmployeeInfoDto employeeInfoDto) {
        staffMapper.insertEmployeeInfo(employeeInfoDto);
    }

    // 직원 급여 정보 저장
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveEmployeeSalary(EmployeeSalaryDto employeeSalaryDto) {
        staffMapper.insertEmployeeSalary(employeeSalaryDto);
    }

    // 직원 리스트 조회
    @Override
    public List<EmployeeDetailDto> retrieveEmployeeList() {
        return staffMapper.selectEmployeeList();
    }

}
