package kr.or.ddit.service;

import java.util.List;
import org.springframework.stereotype.Service;
import kr.or.ddit.finalProject.dto.employee.EmployeeInfoDto;
import kr.or.ddit.finalProject.mapper.EmployeeMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminEmployeeService {

    private final EmployeeMapper employeeMapper;

    public EmployeeInfoDto getEmployeeInfoByUserId(String userId) {
        return employeeMapper.selectEmployeeInfoByUserId(userId);
    }

    public List<EmployeeInfoDto> getEmployeeListByDeptCd(String deptCd) {
        return employeeMapper.selectEmployeeListByDeptCd(deptCd);
    }

    public EmployeeInfoDto getTeamLeaderByDeptCd(String deptCd) {
        return employeeMapper.selectTeamLeaderByDeptCd(deptCd);
    }

    public List<EmployeeInfoDto> getApproverCandidates(String userId) {
        return employeeMapper.selectApproverCandidates(userId);
    }
}
