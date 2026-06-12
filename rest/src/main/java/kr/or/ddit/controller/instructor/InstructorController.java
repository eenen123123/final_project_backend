package kr.or.ddit.controller.instructor;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.dto.instructor.CourseDetailResponse;
import kr.or.ddit.finalProject.dto.instructor.InstructorDetailResponse;
import kr.or.ddit.finalProject.dto.instructor.InstructorFeaturedCourseResponse;
import kr.or.ddit.finalProject.dto.instructor.InstructorListResponse;
import kr.or.ddit.finalProject.dto.instructor.InstructorPublicBoardDetail;
import kr.or.ddit.finalProject.dto.instructor.InstructorPublicBoardItem;
import kr.or.ddit.finalProject.dto.instructor.InstructorPublicCourseResponse;
import kr.or.ddit.finalProject.dto.instructor.InstructorRecentPostResponse;
import kr.or.ddit.finalProject.service.course.CourseService;
import kr.or.ddit.finalProject.service.instructor.InstructorBoardService;
import kr.or.ddit.finalProject.service.instructor.InstructorProfileService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/instructors")
@RequiredArgsConstructor
public class InstructorController {

    private final InstructorProfileService instructorProfileService;
    private final CourseService courseService;
    private final InstructorBoardService instructorBoardService;

    // GET /api/instructors?subjClId={number}
    @GetMapping
    public ResponseEntity<List<InstructorListResponse>> getInstructors(
            @RequestParam(required = false) Long subjClId) {
        return ResponseEntity.ok(instructorProfileService.retrieveInstructors(subjClId));
    }

    // GET /api/instructors/by-subject
    @GetMapping("/by-subject")
    public ResponseEntity<Map<String, List<InstructorListResponse>>> getInstructorsBySubject() {
        return ResponseEntity.ok(instructorProfileService.retrieveInstructorsBySubject());
    }

    @GetMapping("/{instrUuid}")
    public ResponseEntity<InstructorDetailResponse> getInstructorDetail(@PathVariable String instrUuid) {
        InstructorDetailResponse detail = instructorProfileService.retrieveInstructorDetail(instrUuid);
        if (detail == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(detail);
    }

    @GetMapping("/{instrUuid}/featured-courses")
    public ResponseEntity<List<InstructorFeaturedCourseResponse>> getFeaturedCourses(
            @PathVariable String instrUuid) {
        return ResponseEntity.ok(instructorProfileService.retrieveFeaturedCourses(instrUuid));
    }

    @GetMapping("/{instrUuid}/posts")
    public ResponseEntity<List<InstructorRecentPostResponse>> getRecentPosts(
            @PathVariable String instrUuid,
            @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(instructorProfileService.retrieveRecentPosts(instrUuid, size));
    }

    // GET /api/instructors/{instrUuid}/courses
    @GetMapping("/{instrUuid}/courses")
    public ResponseEntity<List<InstructorPublicCourseResponse>> getCourses(
            @PathVariable String instrUuid) {
        return ResponseEntity.ok(courseService.retrievePublicCoursesByInstructor(instrUuid));
    }

    // GET /api/instructors/{instrUuid}/courses/{courseSn}
    @GetMapping("/{instrUuid}/courses/{courseSn}")
    public ResponseEntity<CourseDetailResponse> getCourseDetail(
            @PathVariable String instrUuid,
            @PathVariable Long courseSn) {
        CourseDetailResponse detail = courseService.retrievePublicCourseDetail(instrUuid, courseSn);
        if (detail == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(detail);
    }

    // GET /api/instructors/{instrUuid}/board/notice?page=0&size=10
    @GetMapping("/{instrUuid}/board/notice")
    public ResponseEntity<PageResponse<InstructorPublicBoardItem>> getNoticeList(
            @PathVariable String instrUuid,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(instructorBoardService.getPublicBoardList(instrUuid, "02", page, size));
    }

    // GET /api/instructors/{instrUuid}/board/qna?page=0&size=10
    @GetMapping("/{instrUuid}/board/qna")
    public ResponseEntity<PageResponse<InstructorPublicBoardItem>> getQnaList(
            @PathVariable String instrUuid,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(instructorBoardService.getPublicBoardList(instrUuid, "03", page, size));
    }

    // GET /api/instructors/{instrUuid}/board/dataroom?page=0&size=10
    @GetMapping("/{instrUuid}/board/dataroom")
    public ResponseEntity<PageResponse<InstructorPublicBoardItem>> getDataroomList(
            @PathVariable String instrUuid,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(instructorBoardService.getPublicBoardList(instrUuid, "04", page, size));
    }

    // GET /api/instructors/{instrUuid}/board/{postSn}
    @GetMapping("/{instrUuid}/board/{postSn}")
    public ResponseEntity<InstructorPublicBoardDetail> getBoardDetail(
            @PathVariable String instrUuid,
            @PathVariable Long postSn) {
        InstructorPublicBoardDetail detail = instructorBoardService.getPublicBoardDetail(instrUuid, postSn);
        if (detail == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(detail);
    }
}
