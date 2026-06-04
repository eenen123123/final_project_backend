package kr.or.ddit.controller.board.notice;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kr.or.ddit.finalProject.dto.board.NoticeDto;
import kr.or.ddit.finalProject.dto.board.req.NoticeSearchCondition;
import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.paging.PaginationInfo;
import kr.or.ddit.finalProject.service.board.notice.NoticeService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notice")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    // GET /api/notice/paged?page=1&size=10&keyword=xxx&noticeTypeCd=01
    @GetMapping("/paged")
    public ResponseEntity<PageResponse<NoticeDto>> getNoticeListPaged(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String noticeTypeCd) {
        PaginationInfo<NoticeSearchCondition> paginationInfo = new PaginationInfo<>(size, 5, page);
        paginationInfo.setDetailCondition(new NoticeSearchCondition(keyword, noticeTypeCd));
        return ResponseEntity.ok(noticeService.getList(paginationInfo));
    }

    // GET /api/notice/{postSn}
    @GetMapping("/{postSn}")
    public ResponseEntity<NoticeDto> getNoticeById(@PathVariable Long postSn) {
        return ResponseEntity.ok(noticeService.getById(postSn, null));
    }

    // GET /api/notice/{postSn}/prev
    @GetMapping("/{postSn}/prev")
    public ResponseEntity<NoticeDto> getPrevNotice(@PathVariable Long postSn) {
        NoticeDto prev = noticeService.getPrevNotice(postSn);
        return prev != null ? ResponseEntity.ok(prev) : ResponseEntity.noContent().build();
    }

    // GET /api/notice/{postSn}/next
    @GetMapping("/{postSn}/next")
    public ResponseEntity<NoticeDto> getNextNotice(@PathVariable Long postSn) {
        NoticeDto next = noticeService.getNextNotice(postSn);
        return next != null ? ResponseEntity.ok(next) : ResponseEntity.noContent().build();
    }
}
