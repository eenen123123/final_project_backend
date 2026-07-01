package kr.or.ddit.finalProject.service.board.qna;

import org.springframework.security.core.Authentication;

import kr.or.ddit.finalProject.dto.board.QnaDto;
import kr.or.ddit.finalProject.dto.board.req.QnaSearchCondition;
import kr.or.ddit.finalProject.service.board.BoardService;

public interface QnaService extends BoardService<QnaDto, QnaSearchCondition> {

    void answerQna(QnaDto qnaDto);

    void update(QnaDto dto, Authentication authentication);

    void delete(Long postSn, Authentication authentication);
}
