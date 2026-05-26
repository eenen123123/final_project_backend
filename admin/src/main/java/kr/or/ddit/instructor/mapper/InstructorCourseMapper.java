package kr.or.ddit.instructor.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.dto.instructor.CourseDto;

@Mapper
public interface InstructorCourseMapper {

    List<CourseDto> selectCourseListByInstructor(@Param("instrUserId") String instrUserId);

    int insertCourse(CourseDto courseDto);
}
