package kr.or.ddit.finalProject.service.board.faq;

import java.util.List;

import kr.or.ddit.finalProject.dto.board.FaqDto;
import kr.or.ddit.finalProject.dto.board.req.FaqSearchCondition;
import kr.or.ddit.finalProject.service.board.BoardService;

public interface FaqService extends BoardService<FaqDto, FaqSearchCondition> {

    List<FaqDto> getAll(String faqCtgCd, String faqSubCtgCd);

    FaqDto getPrevFaq(Long postSn, String faqCtgCd);

    FaqDto getNextFaq(Long postSn, String faqCtgCd);
}
