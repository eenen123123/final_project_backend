package kr.or.ddit.finalProject.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import kr.or.ddit.finalProject.dto.message.MessageContentDto;
import kr.or.ddit.finalProject.dto.message.MessageRoomDto;
import kr.or.ddit.finalProject.dto.message.MessageRoomSummaryDto;
import kr.or.ddit.finalProject.paging.PaginationInfo;

@Mapper
public interface MessageMapper {

    int insertGroupChatRoom(MessageRoomDto roomDto);

    void insertChatRoomParticipant(@Param("roomSn") Long roomSn, @Param("userId") String userId);

    Long selectOneOnOneChatRoom(@Param("myId") String myId, @Param("otherId") String otherId);

    List<MessageRoomSummaryDto> selectAllChatRoomsByUserId(@Param("userId") String userId);

    MessageRoomDto selectChatRoomByRoomSn(@Param("roomSn") Long roomSn,
            @Param("userId") String userId);

    int isParticipant(@Param("roomSn") Long roomSn, @Param("userId") String userId);

    int insertChatMessage(MessageContentDto messageContentDto);

    List<MessageContentDto> selectChatMessagesByRoomSn(@Param("roomSn") Long roomSn,
            @Param("paginationInfo") PaginationInfo<MessageContentDto> paginationInfo);

    MessageContentDto selectSenderInfo(@Param("userId") String userId);

    int updateLastSentMessage(@Param("roomSn") Long roomSn);

    void updateLastReadMessage(@Param("roomSn") Long roomSn, @Param("userId") String userId,
            @Param("msgSn") long msgSn);

    List<String> selectChatRoomParticipantIds(Long roomSn);
}
