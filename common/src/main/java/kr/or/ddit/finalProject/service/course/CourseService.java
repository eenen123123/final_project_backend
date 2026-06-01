package kr.or.ddit.finalProject.service.course;

import java.util.List;

import kr.or.ddit.finalProject.dto.course.CourseDto;

public interface CourseService {

    List<CourseDto> retrieveCourseByCurriculumId(Long curriculumId);

    boolean createCourse(CourseDto courseDto);

    void modifyCourse(CourseDto courseDto, String currentUserId);

    void removeCourse(Long courseSn, String currentUserId);

}
