package kr.or.ddit.controller.course;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.dto.course.CourseResponseDto;
import kr.or.ddit.finalProject.dto.course.SubjectClassificationDto;
import kr.or.ddit.finalProject.dto.member.MemberDto;
import kr.or.ddit.finalProject.service.course.CourseService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/course/list")
@RequiredArgsConstructor
public class CourseListController {

    private final CourseService courseService;

    // GET /api/course?page=1&size=8&subjClId=1&instrNm=정승제
    @GetMapping
    public ResponseEntity<PageResponse<CourseResponseDto>> getCourseList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") String category,
            @RequestParam(defaultValue = "") String keyword
    ) {
        PageResponse<CourseResponseDto> courseList
                = courseService.retrieveCourseListForMain(category, keyword, page);
        return ResponseEntity.ok(courseList);
    }

    // GET /api/course/subject-classification
    @GetMapping("/subject-classification")
    public ResponseEntity<List<SubjectClassificationDto>> getSubjectClassificationList() {
        return ResponseEntity.ok(courseService.retrieveSubjectClassificationList());
    }

    // GET /api/course/instructors?subjClId=1
    @GetMapping("/instructors")
    public ResponseEntity<List<MemberDto>> getInstructors(
            @RequestParam(required = false) Long subjClId) {
        return ResponseEntity.ok(courseService.retrieveInstructorsBySubjClId(subjClId));
    }
}
