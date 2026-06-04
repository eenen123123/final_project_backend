package kr.or.ddit.controller.board.qna;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.or.ddit.finalProject.dto.board.QnaDto;
import kr.or.ddit.finalProject.dto.board.req.QnaRequestDto;
import kr.or.ddit.finalProject.dto.board.req.QnaSearchCondition;
import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.paging.PaginationInfo;
import kr.or.ddit.finalProject.service.board.qna.QnaService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/qna")
@RequiredArgsConstructor
public class QnaController {

    private final QnaService qnaService;
    private final ObjectMapper objectMapper;

    // GET /api/qna/paged?page=1&size=10&keyword=xxx&myOnly=true
    @GetMapping("/paged")
    public ResponseEntity<PageResponse<QnaDto>> getQnaListPaged(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String qnaCtgCd,
            @RequestParam(required = false) String answStatCd,
            @RequestParam(defaultValue = "false") boolean myOnly,
            Authentication authentication) {
        String wrtrUserId = (myOnly && authentication != null) ? authentication.getName() : null;
        PaginationInfo<QnaSearchCondition> paginationInfo = new PaginationInfo<>(size, 5, page);
        paginationInfo.setDetailCondition(new QnaSearchCondition(keyword, qnaCtgCd, answStatCd, wrtrUserId));
        return ResponseEntity.ok(qnaService.getList(paginationInfo));
    }

    // GET /api/qna/{postSn}
    @GetMapping("/{postSn}")
    public ResponseEntity<QnaDto> getQnaById(@PathVariable Long postSn, Authentication authentication) {
        return ResponseEntity.ok(qnaService.getById(postSn, authentication));
    }

    // POST /api/qna
    @PostMapping
    public ResponseEntity<Void> createQna(@RequestBody QnaRequestDto req, Authentication authentication) {
        QnaDto dto = new QnaDto();
        dto.setQnaCtgCd(req.getQnaCtgCd());
        dto.setSecrYn(req.getSecrYn());
        dto.setPostSj(req.getPostSj());
        try {
            dto.setPostCn(objectMapper.writeValueAsString(req.getPostCn()));
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().build();
        }
        qnaService.create(dto, authentication);
        return ResponseEntity.ok().build();
    }

    // PUT /api/qna/{postSn}
    @PutMapping("/{postSn}")
    public ResponseEntity<Void> updateQna(@PathVariable Long postSn, @RequestBody QnaDto dto) {
        dto.setPostSn(postSn);
        qnaService.update(dto);
        return ResponseEntity.ok().build();
    }

    // DELETE /api/qna/{postSn}
    @DeleteMapping("/{postSn}")
    public ResponseEntity<Void> deleteQna(@PathVariable Long postSn) {
        qnaService.delete(postSn);
        return ResponseEntity.ok().build();
    }
}
