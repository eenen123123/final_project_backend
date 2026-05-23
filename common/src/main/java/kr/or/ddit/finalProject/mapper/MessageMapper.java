package kr.or.ddit.finalProject.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.message.MessageRoomDto;

@Mapper
public interface MessageMapper {

    int insertGroupChatRoom(MessageRoomDto roomDto);

    void insertChatRoomParticipant(@Param("roomSn") Long roomSn, @Param("userId") String userId);

    List<MessageRoomDto> selectAllChatRoomsByUserId(@Param("userId") String userId);

}
