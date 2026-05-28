package kr.or.ddit.controller.board.qna;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kr.or.ddit.finalProject.dto.board.QnaDto;
import kr.or.ddit.finalProject.service.board.qna.QnaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/qna")
@RequiredArgsConstructor
public class QnaController {

    private final QnaService qnaService;

    // QnA 목록 조회
    // GET /api/qna?qnaCtgCd=01&answStatCd=01
    @GetMapping
    public ResponseEntity<List<QnaDto>> getQnaList(@RequestParam(required = false) String qnaCtgCd,
            @RequestParam(required = false) String answStatCd) {
        return ResponseEntity.ok(qnaService.getQnaList(qnaCtgCd, answStatCd));
    }

    // QnA 단건 조회
    // GET /api/qna/{postSn}
    @GetMapping("/{postSn}")
    public ResponseEntity<QnaDto> getQnaById(@PathVariable Long postSn) {
        return ResponseEntity.ok(qnaService.getQnaById(postSn));
    }

    // QnA 등록 (사용자)
    // POST /api/qna
    @PostMapping
    public ResponseEntity<Void> createQna(@RequestBody QnaDto qnaDto) {
        log.info("qna controller : {]", qnaDto.toString());
        qnaService.createQna(qnaDto);
        return ResponseEntity.ok().build();
    }

    // QnA 수정 (사용자)
    // PUT /api/qna/{postSn}
    @PutMapping("/{postSn}")
    public ResponseEntity<Void> updateQna(@PathVariable Long postSn, @RequestBody QnaDto qnaDto) {
        qnaDto.setPostSn(postSn);
        qnaService.updateQna(qnaDto);
        return ResponseEntity.ok().build();
    }
}
