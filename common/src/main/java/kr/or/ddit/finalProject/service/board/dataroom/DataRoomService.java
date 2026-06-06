package kr.or.ddit.finalProject.service.board.dataroom;

import java.util.List;

import kr.or.ddit.finalProject.dto.board.DataRoomDto;
import kr.or.ddit.finalProject.dto.board.req.DataRoomSearchCondition;
import kr.or.ddit.finalProject.service.board.BoardService;

public interface DataRoomService extends BoardService<DataRoomDto, DataRoomSearchCondition> {

    List<DataRoomDto> getAll(String dataCtg);
}
