package kr.or.ddit.finalProject.mapper;

import org.apache.ibatis.annotations.Mapper;
import kr.or.ddit.finalProject.dto.employee.EmployeeInfoDto;

@Mapper
public interface EmployeeMapper {
    int insertEmployeeInfo(EmployeeInfoDto employeeInfoDto);
}
