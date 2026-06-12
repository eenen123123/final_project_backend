package kr.or.ddit.controller.textbook;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.dto.subject.SubjectDto;
import kr.or.ddit.finalProject.dto.textbook.TextbookDto;
import kr.or.ddit.finalProject.paging.PaginationInfo;
import kr.or.ddit.finalProject.service.course.CourseService;
import kr.or.ddit.finalProject.service.textbook.TextbookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/textbook")
@RequiredArgsConstructor
public class TextbookListController {

    private final TextbookService textbookService;
    private final CourseService courseService;

    // GET /api/textbook/subjects?subjClId=1 — 소분류 목록
    @GetMapping("/subjects")
    public ResponseEntity<List<SubjectDto>> getSubjects(@RequestParam Long subjClId) {
        return ResponseEntity.ok(courseService.retrieveSubjectsBySubjClId(subjClId));
    }

    // GET /api/textbook?page=1&size=8&keyword=수학&subjClId=2&subjId=5&sort=recent
    @GetMapping
    public ResponseEntity<PageResponse<TextbookDto>> getTextbookList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long subjClId,
            @RequestParam(required = false) Long subjId,
            @RequestParam(defaultValue = "recent") String sort) {

        PaginationInfo<TextbookDto> paginationInfo = new PaginationInfo<>(size, 5, page);
        TextbookDto condition = TextbookDto.builder()
                .keyword(keyword).subjClId(subjClId).subjId(subjId).sort(sort).build();
        paginationInfo.setDetailCondition(condition);

        int totalCount = textbookService.retrieveTextbookListCount(paginationInfo);
        List<TextbookDto> items = textbookService.retrieveTextbookList(paginationInfo);

        return ResponseEntity.ok(new PageResponse<>(items, totalCount));
    }

    // GET /api/textbook/{textbookSn}
    @GetMapping("/{textbookSn}")
    public ResponseEntity<TextbookDto> getTextbook(@PathVariable Long textbookSn) {
        TextbookDto textbookDto = textbookService.retrieveTextbookBySn(textbookSn);
        if (textbookDto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(textbookDto);
    }

    @GetMapping("/course/{courseSn}")
    public ResponseEntity<List<TextbookDto>> getTextbookListByCourseSn(
            @PathVariable Long courseSn) {
        List<TextbookDto> textbooks = textbookService.retrieveTextbookListByCourseSn(courseSn);
        return ResponseEntity.ok(textbooks);
    }

}
