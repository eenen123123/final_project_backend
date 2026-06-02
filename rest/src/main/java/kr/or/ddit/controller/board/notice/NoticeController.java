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

    // 공지사항 이전글
    // GET /api/notice/{postSn}/prev?
    @GetMapping("/{postSn}/prev")
    public ResponseEntity<NoticeDto> getPrevNotice(@PathVariable Long postSn){
        NoticeDto prev = noticeService.getPrevNotice(postSn);
        return prev != null ? ResponseEntity.ok(prev) : ResponseEntity.noContent().build();
    }

    // 공지사항 다음글
    // GET /api/notice/{postSn}/next?
    @GetMapping("/{postSn}/next")
    public ResponseEntity<NoticeDto> getNextNotice(@PathVariable Long postSn){
        NoticeDto next = noticeService.getNextNotice(postSn);
        return next != null ? ResponseEntity.ok(next) : ResponseEntity.noContent().build();
    }
    
    
    
    
}
