package kr.or.ddit.finalProject.mapper.board;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import kr.or.ddit.finalProject.dto.board.DataRoomDto;

@Mapper
public interface DataRoomMapper {

    // 자료실 목록 조회
    List<DataRoomDto> findDataRoomList(@Param("dataCtg") String dataCtg);

    // 자료실 단건 조회
    DataRoomDto findDataRoomById(@Param("postSn") Long postSn);

    // 자료실 등록 - BOARD INSERT
    int insertBoard(DataRoomDto dataRoomDto);

    // 자료실 등록 - DATA_ROOM INSERT
    int insertDataRoom(DataRoomDto dataRoomDto);

    // 자료실 수정 - BOARD UPDATE
    int updateBoard(DataRoomDto dataRoomDto);

    // 자료실 수정 - DATA_ROOM UPDATE
    int updateDataRoom(DataRoomDto dataRoomDto);

    // 자료실 삭제 - DATA_ROOM DELETE (FK 때문에 먼저)
    int deleteDataRoom(@Param("postSn") Long postSn);

    // 자료실 삭제 - BOARD DELETE
    int deleteBoard(@Param("postSn") Long postSn);

}
