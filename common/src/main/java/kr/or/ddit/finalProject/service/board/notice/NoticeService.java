package kr.or.ddit.finalProject.service.board.notice;

import kr.or.ddit.finalProject.dto.board.NoticeDto;
import kr.or.ddit.finalProject.dto.board.req.NoticeSearchCondition;
import kr.or.ddit.finalProject.service.board.BoardService;

public interface NoticeService extends BoardService<NoticeDto, NoticeSearchCondition> {

    NoticeDto getPrevNotice(Long postSn);

    NoticeDto getNextNotice(Long postSn);
}
