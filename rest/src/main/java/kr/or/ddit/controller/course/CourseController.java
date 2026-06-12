package kr.or.ddit.controller.course;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kr.or.ddit.finalProject.dto.course.CourseResponseDto;
import kr.or.ddit.finalProject.exception.ErrorCode;
import kr.or.ddit.finalProject.exception.FinalProjectException;
import kr.or.ddit.finalProject.service.course.CourseService;
import kr.or.ddit.finalProject.service.lecture.LectureService;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/course")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final LectureService lectureService;

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getCourse(@PathVariable Long id,

        Authentication authentication
    ) {
        CourseResponseDto course = courseService.retrieveCourse(id);
        if (course == null) {
            throw new FinalProjectException(ErrorCode.COURSE_NOT_FOUND);
        }
        String userId = authentication.getName();
        var lectures = lectureService.retrieveLectureListByCourseSn(id, userId);
        Map<String, Object> response = Map.of("course", course, "lectures", lectures);
        return ResponseEntity.ok(response);
    }

}
