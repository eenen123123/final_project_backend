package kr.or.ddit.controller.course;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.dto.course.CourseDto;
import kr.or.ddit.finalProject.dto.course.CourseSearchCondition;
import kr.or.ddit.finalProject.dto.course.SubjectClassificationDto;
import kr.or.ddit.finalProject.dto.member.MemberDto;
import kr.or.ddit.finalProject.paging.PaginationInfo;
import kr.or.ddit.finalProject.service.course.CourseService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/course")
@RequiredArgsConstructor
public class CourseListController {

    private final CourseService courseService;

    // GET /api/course?page=1&size=8&subjClId=1&instrNm=정승제
    @GetMapping
    public ResponseEntity<PageResponse<CourseDto>> getCourseList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(required = false) Long subjClId,
            @RequestParam(required = false) String instrNm) {

        CourseSearchCondition condition = CourseSearchCondition.builder()
                .subjClId(subjClId)
                .instrNm(instrNm)
                .opnnYn("Y")
                .build();

        PaginationInfo<CourseSearchCondition> paginationInfo = new PaginationInfo<>(size, 5, page);
        paginationInfo.setDetailCondition(condition);

        int totalCount = courseService.retrieveCourseListCount(paginationInfo);
        List<CourseDto> items = courseService.retrieveCourseList(paginationInfo);

        return ResponseEntity.ok(new PageResponse<>(items, totalCount));
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
