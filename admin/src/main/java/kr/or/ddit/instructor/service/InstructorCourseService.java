package kr.or.ddit.instructor.service;

import java.util.List;

import kr.or.ddit.dto.instructor.CourseDto;

public interface InstructorCourseService {

    List<CourseDto> retrieveCourseList(String instrUserId);
}
