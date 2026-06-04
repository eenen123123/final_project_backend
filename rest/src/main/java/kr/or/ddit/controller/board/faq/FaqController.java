package kr.or.ddit.controller.board.faq;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kr.or.ddit.finalProject.dto.board.FaqDto;
import kr.or.ddit.finalProject.dto.board.req.FaqSearchCondition;
import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.paging.PaginationInfo;
import kr.or.ddit.finalProject.service.board.faq.FaqService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/faq")
@RequiredArgsConstructor
public class FaqController {

    private final FaqService faqService;

    // GET /api/faq/paged?page=1&size=10&keyword=xxx&faqCtgCd=01&faqSubCtgCd=01
    @GetMapping("/paged")
    public ResponseEntity<PageResponse<FaqDto>> getFaqListPaged(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String faqCtgCd,
            @RequestParam(required = false) String faqSubCtgCd) {
        PaginationInfo<FaqSearchCondition> paginationInfo = new PaginationInfo<>(size, 5, page);
        paginationInfo.setDetailCondition(new FaqSearchCondition(keyword, faqCtgCd, faqSubCtgCd));
        return ResponseEntity.ok(faqService.getList(paginationInfo));
    }

    // GET /api/faq/{postSn}
    @GetMapping("/{postSn}")
    public ResponseEntity<FaqDto> getFaqById(@PathVariable Long postSn) {
        return ResponseEntity.ok(faqService.getById(postSn, null));
    }

    // GET /api/faq/{postSn}/prev?faqCtgCd=01
    @GetMapping("/{postSn}/prev")
    public ResponseEntity<FaqDto> getPrevFaq(@PathVariable Long postSn, @RequestParam String faqCtgCd) {
        FaqDto prev = faqService.getPrevFaq(postSn, faqCtgCd);
        return prev != null ? ResponseEntity.ok(prev) : ResponseEntity.noContent().build();
    }

    // GET /api/faq/{postSn}/next?faqCtgCd=01
    @GetMapping("/{postSn}/next")
    public ResponseEntity<FaqDto> getNextFaq(@PathVariable Long postSn, @RequestParam String faqCtgCd) {
        FaqDto next = faqService.getNextFaq(postSn, faqCtgCd);
        return next != null ? ResponseEntity.ok(next) : ResponseEntity.noContent().build();
    }
}
