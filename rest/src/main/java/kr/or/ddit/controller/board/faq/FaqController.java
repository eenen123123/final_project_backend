package kr.or.ddit.controller.board.faq;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import kr.or.ddit.finalProject.dto.board.FaqDto;
import kr.or.ddit.finalProject.service.board.faq.FaqService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;


@Slf4j
@RestController
@RequestMapping("/api/faq")
@RequiredArgsConstructor
public class FaqController {
    
    private final FaqService faqService;

    // FAQ 목록 조회
    // GET (ex. /api/faq?faqCtgCd(대분류)=01&faqSubCtgCd(중분류)=01)
    @GetMapping
    public ResponseEntity<List<FaqDto>> getFaqList(
            @RequestParam(required = false) String faqCtgCd,
            @RequestParam(required = false) String faqSubCtgCd) {
        // log.info("FAQ 목록 조회 => 대분류 : {}, 소분류 : {}", faqCtgCd, faqSubCtgCd);
        return ResponseEntity.ok(faqService.getFaqList(faqCtgCd, faqSubCtgCd));
            }
    // FAQ 단건 조회
    @GetMapping("/{postSn}")
    public ResponseEntity<FaqDto> getFaqById(@PathVariable Long postSn) {
        // log.info("FAQ 단건 조회 요청 - postSn: {}", postSn);
        return ResponseEntity.ok(faqService.getFaqById(postSn));
    }

    // FAQ 등록
    // POST /api/faq
    @PostMapping
    public ResponseEntity<Void> createFaq(@RequestBody FaqDto faqDto) {
        faqService.createFaq(faqDto);
        return ResponseEntity.ok().build();
    }
    // FAQ 수정
    // PUT /api/faq/{postSn}
    @PutMapping("/{postSn}")
    public ResponseEntity<Void> updateFaq(@PathVariable Long postSn, @RequestBody FaqDto faqDto) {
        faqDto.setPostSn(postSn);
        faqService.updateFaq(faqDto);
        return ResponseEntity.ok().build();
    }
    // FAQ 삭제
    // DELETE /api/faq/{postSn}
    @DeleteMapping("/{postSn}")
    public ResponseEntity<Void> deleteFaq(@PathVariable Long postSn) {
        faqService.deleteFaq(postSn);
        return ResponseEntity.ok().build();

    }
}        
    
    
 


