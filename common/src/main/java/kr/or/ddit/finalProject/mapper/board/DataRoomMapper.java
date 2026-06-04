package kr.or.ddit.finalProject.mapper.board;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.board.DataRoomDto;
import kr.or.ddit.finalProject.dto.board.req.DataRoomSearchCondition;
import kr.or.ddit.finalProject.paging.PaginationInfo;

@Mapper
public interface DataRoomMapper {

    List<DataRoomDto> findDataRoomList(@Param("dataCtg") String dataCtg);

    List<DataRoomDto> findDataRoomListPaged(PaginationInfo<DataRoomSearchCondition> paginationInfo);

    int countDataRoomList(PaginationInfo<DataRoomSearchCondition> paginationInfo);

    DataRoomDto findDataRoomById(@Param("postSn") Long postSn);

    int insertBoard(DataRoomDto dataRoomDto);

    int insertDataRoom(DataRoomDto dataRoomDto);

    int updateBoard(DataRoomDto dataRoomDto);

    int updateDataRoom(DataRoomDto dataRoomDto);

    int deleteDataRoom(@Param("postSn") Long postSn);

    int deleteBoard(@Param("postSn") Long postSn);
}
