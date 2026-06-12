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
import kr.or.ddit.finalProject.dto.course.CourseDetailResponse;
import kr.or.ddit.finalProject.dto.instructor.profile.InstructorDetailResponse;
import kr.or.ddit.finalProject.dto.instructor.profile.InstructorFeaturedCourseResponse;
import kr.or.ddit.finalProject.dto.instructor.profile.InstructorListResponse;
import kr.or.ddit.finalProject.dto.instructor.board.InstructorPublicBoardDetail;
import kr.or.ddit.finalProject.dto.instructor.board.InstructorPublicBoardItem;
import kr.or.ddit.finalProject.dto.instructor.profile.InstructorPublicCourseResponse;
import kr.or.ddit.finalProject.dto.instructor.profile.InstructorRecentPostResponse;
import kr.or.ddit.finalProject.service.course.CourseService;
import kr.or.ddit.finalProject.service.instructor.InstructorBoardService;
import kr.or.ddit.finalProject.service.instructor.InstructorService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/instructors")
@RequiredArgsConstructor
public class InstructorController {

    private final InstructorService instructorService;
    private final CourseService courseService;
    private final InstructorBoardService instructorBoardService;

    /**
     * 과목 분류 ID로 강사 목록 조회. subjClId 생략 시 전체 반환.
     */
    @GetMapping
    public ResponseEntity<List<InstructorListResponse>> getInstructors(
            @RequestParam(required = false) Long subjClId) {
        return ResponseEntity.ok(instructorService.retrieveInstructors(subjClId));
    }

    /**
     * 전체 강사 목록을 과목 분류별로 그룹핑하여 반환.
     */
    @GetMapping("/by-subject")
    public ResponseEntity<Map<String, List<InstructorListResponse>>> getInstructorsBySubject() {
        return ResponseEntity.ok(instructorService.retrieveInstructorsBySubject());
    }

    /**
     * UUID로 강사 공개 프로필 상세 조회. 존재하지 않으면 404.
     */
    @GetMapping("/{instrUuid}")
    public ResponseEntity<InstructorDetailResponse> getInstructorDetail(@PathVariable String instrUuid) {
        InstructorDetailResponse detail = instructorService.retrieveInstructorDetail(instrUuid);
        if (detail == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(detail);
    }

    /**
     * 강사의 추천 강좌 목록 조회.
     */
    @GetMapping("/{instrUuid}/featured-courses")
    public ResponseEntity<List<InstructorFeaturedCourseResponse>> getFeaturedCourses(
            @PathVariable String instrUuid) {
        return ResponseEntity.ok(instructorService.retrieveFeaturedCourses(instrUuid));
    }

    /**
     * 강사의 최근 게시글 목록 조회. size 기본값 5.
     */
    @GetMapping("/{instrUuid}/posts")
    public ResponseEntity<List<InstructorRecentPostResponse>> getRecentPosts(
            @PathVariable String instrUuid,
            @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(instructorService.retrieveRecentPosts(instrUuid, size));
    }

    /**
     * 강사의 공개 강좌 목록 조회.
     */
    @GetMapping("/{instrUuid}/courses")
    public ResponseEntity<List<InstructorPublicCourseResponse>> getCourses(
            @PathVariable String instrUuid) {
        return ResponseEntity.ok(courseService.retrievePublicCoursesByInstructor(instrUuid));
    }

    /**
     * 강사의 특정 강좌 상세 조회 (강의 목록 포함). 존재하지 않으면 404.
     */
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

    /**
     * 강사 공개 공지사항 목록 페이징 조회.
     */
    @GetMapping("/{instrUuid}/board/notice")
    public ResponseEntity<PageResponse<InstructorPublicBoardItem>> getNoticeList(
            @PathVariable String instrUuid,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(instructorBoardService.getPublicBoardList(instrUuid, "02", page, size));
    }

    /**
     * 강사 공개 Q&A 목록 페이징 조회.
     */
    @GetMapping("/{instrUuid}/board/qna")
    public ResponseEntity<PageResponse<InstructorPublicBoardItem>> getQnaList(
            @PathVariable String instrUuid,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(instructorBoardService.getPublicBoardList(instrUuid, "03", page, size));
    }

    /**
     * 강사 공개 자료실 목록 페이징 조회.
     */
    @GetMapping("/{instrUuid}/board/dataroom")
    public ResponseEntity<PageResponse<InstructorPublicBoardItem>> getDataroomList(
            @PathVariable String instrUuid,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(instructorBoardService.getPublicBoardList(instrUuid, "04", page, size));
    }

    /**
     * 게시글 상세 조회 (이전/다음글·첨부파일 포함). 조회수 증가. 존재하지 않으면 404.
     */
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
