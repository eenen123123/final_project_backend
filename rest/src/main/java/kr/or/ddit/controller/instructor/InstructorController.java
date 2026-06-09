package kr.or.ddit.controller.instructor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.dto.instructor.InstructorDetailResponse;
import kr.or.ddit.finalProject.dto.instructor.InstructorFeaturedCourseResponse;
import kr.or.ddit.finalProject.dto.instructor.InstructorListResponse;
import kr.or.ddit.finalProject.dto.instructor.InstructorPublicBoardDetail;
import kr.or.ddit.finalProject.dto.instructor.InstructorPublicBoardItem;
import kr.or.ddit.finalProject.dto.instructor.InstructorPublicCourseResponse;
import kr.or.ddit.finalProject.dto.instructor.InstructorRecentPostResponse;
import kr.or.ddit.finalProject.mapper.course.CourseMapper;
import kr.or.ddit.finalProject.mapper.instructor.InstructorBoardMapper;
import kr.or.ddit.finalProject.mapper.instructor.InstructorFeaturedCourseMapper;
import kr.or.ddit.finalProject.mapper.instructor.InstructorMapper;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/instructors")
@RequiredArgsConstructor
public class InstructorController {

    private final InstructorMapper instructorMapper;
    private final InstructorFeaturedCourseMapper featuredCourseMapper;
    private final InstructorBoardMapper instructorBoardMapper;
    private final CourseMapper courseMapper;

    // GET /api/instructors/by-subject
    // 과목 분류별 강사 목록 반환
    // 응답 예: { "프로그래밍": [{instrUserId, userName, instrProfileImg}, ...], ... }
    @GetMapping("/by-subject")
    public ResponseEntity<Map<String, List<InstructorListResponse>>> getInstructorsBySubject() {
        List<InstructorListResponse> flat = instructorMapper.selectInstructorsBySubject();

        Map<String, List<InstructorListResponse>> grouped = flat.stream()
                .collect(Collectors.groupingBy(
                        InstructorListResponse::getSubjectClNm,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        return ResponseEntity.ok(grouped);
    }

    @GetMapping("/{instrUuid}")
    public ResponseEntity<InstructorDetailResponse> getInstructorDetail(@PathVariable String instrUuid) {
        InstructorDetailResponse detail = instructorMapper.selectInstructorByUuid(instrUuid);
        if (detail == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(detail);
    }

    @GetMapping("/{instrUuid}/featured-courses")
    public ResponseEntity<List<InstructorFeaturedCourseResponse>> getFeaturedCourses(@PathVariable String instrUuid) {
        return ResponseEntity.ok(featuredCourseMapper.selectFeaturedCourses(instrUuid));
    }

    @GetMapping("/{instrUuid}/posts")
    public ResponseEntity<List<InstructorRecentPostResponse>> getRecentPosts(
            @PathVariable String instrUuid,
            @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(instructorBoardMapper.selectRecentPosts(instrUuid, size));
    }

    // GET /api/instructors/{instrUuid}/courses
    @GetMapping("/{instrUuid}/courses")
    public ResponseEntity<List<InstructorPublicCourseResponse>> getCourses(
            @PathVariable String instrUuid) {
        return ResponseEntity.ok(courseMapper.selectCoursesByInstrUuid(instrUuid));
    }

    // GET /api/instructors/{instrUuid}/board/notice?page=0&size=10
    @GetMapping("/{instrUuid}/board/notice")
    public ResponseEntity<PageResponse<InstructorPublicBoardItem>> getNoticeList(
            @PathVariable String instrUuid,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        int offset = page * size;
        int total = instructorBoardMapper.selectPublicBoardCount(instrUuid, "02");
        List<InstructorPublicBoardItem> items =
                instructorBoardMapper.selectPublicBoardList(instrUuid, "02", offset, size);
        return ResponseEntity.ok(new PageResponse<>(items, total));
    }

    // GET /api/instructors/{instrUuid}/board/qna?page=0&size=10
    @GetMapping("/{instrUuid}/board/qna")
    public ResponseEntity<PageResponse<InstructorPublicBoardItem>> getQnaList(
            @PathVariable String instrUuid,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        int offset = page * size;
        int total = instructorBoardMapper.selectPublicBoardCount(instrUuid, "03");
        List<InstructorPublicBoardItem> items =
                instructorBoardMapper.selectPublicBoardList(instrUuid, "03", offset, size);
        return ResponseEntity.ok(new PageResponse<>(items, total));
    }

    // GET /api/instructors/{instrUuid}/board/material?page=0&size=10
    @GetMapping("/{instrUuid}/board/material")
    public ResponseEntity<PageResponse<InstructorPublicBoardItem>> getMaterialList(
            @PathVariable String instrUuid,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        int offset = page * size;
        int total = instructorBoardMapper.selectPublicBoardCount(instrUuid, "04");
        List<InstructorPublicBoardItem> items =
                instructorBoardMapper.selectPublicBoardList(instrUuid, "04", offset, size);
        return ResponseEntity.ok(new PageResponse<>(items, total));
    }

    // GET /api/instructors/{instrUuid}/board/{postSn}
    @GetMapping("/{instrUuid}/board/{postSn}")
    public ResponseEntity<InstructorPublicBoardDetail> getBoardDetail(
            @PathVariable String instrUuid,
            @PathVariable Long postSn) {
        InstructorPublicBoardDetail detail =
                instructorBoardMapper.selectPublicBoardDetail(instrUuid, postSn);
        if (detail == null) {
            return ResponseEntity.notFound().build();
        }
        instructorBoardMapper.incrementViewCount(postSn);
        detail.setPrevPost(instructorBoardMapper.selectPrevPost(instrUuid, detail.getBoardTypeCd(), postSn));
        detail.setNextPost(instructorBoardMapper.selectNextPost(instrUuid, detail.getBoardTypeCd(), postSn));
        if ("Y".equals(detail.getHasFile())) {
            detail.setFiles(instructorBoardMapper.selectBoardFiles(postSn));
        }
        return ResponseEntity.ok(detail);
    }

}
