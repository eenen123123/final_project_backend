package kr.or.ddit.finalProject.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import kr.or.ddit.finalProject.dto.employee.EmployeeInfoDto;

@Mapper
public interface EmployeeMapper {
    int insertEmployeeInfo(EmployeeInfoDto employeeInfoDto);

    EmployeeInfoDto selectEmployeeInfoByUserId(String userId);

    List<EmployeeInfoDto> selectEmployeeListByDeptCd(String deptCd);

    EmployeeInfoDto selectTeamLeaderByDeptCd(String deptCd);

    List<EmployeeInfoDto> selectApproverCandidates(String userId);
}
