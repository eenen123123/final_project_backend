package kr.or.ddit.finalProject.service.board.notice;

import java.util.List;

import kr.or.ddit.finalProject.dto.board.NoticeDto;
import kr.or.ddit.finalProject.dto.board.req.NoticeSearchCondition;
import kr.or.ddit.finalProject.service.board.BoardService;

public interface NoticeService extends BoardService<NoticeDto, NoticeSearchCondition> {

    List<NoticeDto> getAll();

    NoticeDto getPrevNotice(Long postSn);

    NoticeDto getNextNotice(Long postSn);
}
