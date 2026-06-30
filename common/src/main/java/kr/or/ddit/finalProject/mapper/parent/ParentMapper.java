package kr.or.ddit.finalProject.mapper.parent;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.parent.ParentChildDto;
import kr.or.ddit.finalProject.dto.student.StudentAttendanceDto;

@Mapper
public interface ParentMapper {

    List<ParentChildDto> selectChildrenByParentId(@Param("parentId") String parentId);

    boolean isParentOf(@Param("parentId") String parentId, @Param("studentId") String studentId);

    List<StudentAttendanceDto> selectMonthlyAttendance(
            @Param("studentId") String studentId,
            @Param("year") int year,
            @Param("month") int month);
}
