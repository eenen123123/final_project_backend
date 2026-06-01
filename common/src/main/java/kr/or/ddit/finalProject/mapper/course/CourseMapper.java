package kr.or.ddit.finalProject.mapper.course;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.course.CourseDto;

@Mapper
public interface CourseMapper {

    List<CourseDto> selectCourseByCurriculumId(@Param("curriculumId") Long curriculumId);

    CourseDto selectCourseBySn(@Param("courseSn") Long courseSn);

    int insertCourse(CourseDto courseDto);

    int updateCourse(CourseDto courseDto);

    int deleteCourse(@Param("courseSn") Long courseSn);

}
