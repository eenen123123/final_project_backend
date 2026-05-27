package kr.or.ddit.finalProject.service.course;

import java.util.List;

import kr.or.ddit.finalProject.dto.course.CourseDto;

public interface CourseService {

    List<CourseDto> retrieveCourseList(String instrUserId);
}
