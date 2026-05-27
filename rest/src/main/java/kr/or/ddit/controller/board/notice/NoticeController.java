package kr.or.ddit.controller.board.notice;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kr.or.ddit.finalProject.dto.board.NoticeDto;
import kr.or.ddit.finalProject.service.board.notice.NoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/notice")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    // 공지사항 목록 조회
    // GET /api/notice?noticeTypeCd=01
    @GetMapping
    public ResponseEntity<List<NoticeDto>> getNoticeList(
            @RequestParam(required = false) String noticeTypeCd) {
        return ResponseEntity.ok(noticeService.getNoticeList(noticeTypeCd));
    }

    // 공지사항 단건 조회
    // GET /api/notice/{postSn}
    @GetMapping("/{postSn}")
    public ResponseEntity<NoticeDto> getNoticeById(@PathVariable Long postSn) {
        return ResponseEntity.ok(noticeService.getNoticeById(postSn));
    }
}
