package kr.or.ddit.controller.board.faq;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import kr.or.ddit.finalProject.dto.board.FaqDto;
import kr.or.ddit.finalProject.service.board.faq.FaqService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/faq")
@RequiredArgsConstructor
public class FaqController {

    private final FaqService faqService;

    // FAQ 목록 조회
    // GET /api/faq?faqCtgCd=01&faqSubCtgCd=01
    @GetMapping
    public ResponseEntity<List<FaqDto>> getFaqList(@RequestParam(required = false) String faqCtgCd,
            @RequestParam(required = false) String faqSubCtgCd) {
        return ResponseEntity.ok(faqService.getFaqList(faqCtgCd, faqSubCtgCd));
    }

    // FAQ 단건 조회
    // GET /api/faq/{postSn}
    @GetMapping("/{postSn}")
    public ResponseEntity<FaqDto> getFaqById(@PathVariable Long postSn) {
        return ResponseEntity.ok(faqService.getFaqById(postSn));
    }
}
