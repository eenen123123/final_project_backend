package kr.or.ddit.finalProject.service.staff;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.ddit.finalProject.dto.employee.DepartmentDto;
import kr.or.ddit.finalProject.dto.employee.JobGradeDto;
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
    public void registerEmployee() {
        staffMapper.insertEmployee(null);
    }

    // 직원 정보 저장
    @Override
    public void saveEmployeeInfo() {
        staffMapper.insertEmployeeInfo(null);
    }

    // 직원 급여 정보 저장
    @Override
    public void saveEmployeeSalary() {
        staffMapper.insertEmployeeSalary(null);
    }

}
