package kr.or.ddit.finalProject.service.board.faq;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import kr.or.ddit.finalProject.dto.board.FaqDto;
import kr.or.ddit.finalProject.service.board.faq.FaqService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
class FaqServiceTest {

    @Autowired
    FaqService faqService;

    @Test
    void getFaqList() {
        List<FaqDto> list = faqService.getFaqList(null, null);
        System.out.println("FAQ 목록 크기: " + list.size());
        list.forEach(faq -> System.out.println("FAQ: " + faq));
    }

    @Test
    void getFaqListByCategory() {
        // 강의/교재 대분류만 조회
        List<FaqDto> list = faqService.getFaqList("01", null);
        list.forEach(faq -> log.info("FAQ: {}", faq));
    }

    @Test
    void getFaqById() {
        FaqDto faq = faqService.getFaqById(1L);
        log.info("FAQ: {}", faq);
    }
}
