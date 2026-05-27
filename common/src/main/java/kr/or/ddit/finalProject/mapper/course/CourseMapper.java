package kr.or.ddit.finalProject.mapper.course;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.course.CourseDto;

@Mapper
public interface CourseMapper {

    List<CourseDto> selectCourseListByInstructor(@Param("instrUserId") String instrUserId);

    int insertCourse(CourseDto courseDto);
}
