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

    // 자료실 게시글 등록
    int insertDataRoom(DataRoomDto dataRoomdto);

    // 자료실 게시글 수정
    int updateDataRoom(DataRoomDto dataRoomdto);

    // 자료실 게시글 삭제
    int deleteDataRoom(@Param("postSn") Long postSn);
}
