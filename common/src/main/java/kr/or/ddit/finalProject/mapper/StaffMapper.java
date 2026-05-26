package kr.or.ddit.finalProject.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import kr.or.ddit.finalProject.dto.employee.DepartmentDto;
import kr.or.ddit.finalProject.dto.employee.JobGradeDto;

@Mapper
public interface StaffMapper {
    // 부서 리스트 조회
    List<DepartmentDto> selectDepartmentList();
    
    // 직급 리스트 조회
    List<JobGradeDto> selectJobGradeList();
}
