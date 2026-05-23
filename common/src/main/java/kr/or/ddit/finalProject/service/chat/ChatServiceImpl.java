package kr.or.ddit.finalProject.service.chat;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.ddit.finalProject.dto.message.MessageContentDto;
import kr.or.ddit.finalProject.dto.message.MessageRoomDto;
import kr.or.ddit.finalProject.exception.ErrorCode;
import kr.or.ddit.finalProject.exception.FinalProjectException;
import kr.or.ddit.finalProject.mapper.MemberMapper;
import kr.or.ddit.finalProject.mapper.MessageMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final MemberMapper memberMapper;
    private final MessageMapper messageMapper;

    @Override
    @Transactional
    public MessageRoomDto createGroupChatRoom(String roomNm, String opnrUserId, List<String> partUserIds) {
        // 채팅방 생성

        // partUserIds 목록의 회원들이 존재하는지 확인
        int existingCount = memberMapper.isAllExistUsers(partUserIds);
        if (existingCount != partUserIds.size()) {
            throw new FinalProjectException(ErrorCode.USER_NOT_FOUND);
        }

        // 채팅방 생성
        MessageRoomDto newRoom = MessageRoomDto.builder()
                .roomTypeCd("02") // 그룹채팅
                .roomNm(roomNm)
                .opnrUserId(opnrUserId)
                .build();
        int result = messageMapper.insertGroupChatRoom(newRoom);
        if (result <= 0) {
            throw new FinalProjectException(ErrorCode.CHAT_ROOM_CREATION_FAILED);
        }

        // 채팅방 참여자 추가 (opnrUserId 포함)
        List<String> participantIds = new ArrayList<>(partUserIds);
        participantIds.add(opnrUserId);

        for (String userId : participantIds) {
            messageMapper.insertChatRoomParticipant(newRoom.getRoomSn(), userId);
        }

        return newRoom;
    }

    @Override
    public List<MessageContentDto> getChatMessages(long roomSn, int page, int screenSize) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<MessageRoomDto> getChatRoomList(String userId) {
        return messageMapper.selectAllChatRoomsByUserId(userId);
    }

    @Override
    public MessageRoomDto getGroupChatRoom(long roomSn) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MessageRoomDto getOrCreateOneOnOneChatRoom(String myId, String otherId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void sendMessage(Long roomSn, String sendrUserId, String msgTypeCd, String msgCn) {
        // TODO Auto-generated method stub

    }

}
