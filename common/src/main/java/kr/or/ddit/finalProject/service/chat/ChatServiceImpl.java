package kr.or.ddit.finalProject.service.chat;

import java.util.ArrayList;
import java.util.List;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.ddit.finalProject.dto.message.CreateMessageRoomRequestDto;
import kr.or.ddit.finalProject.dto.message.MessageContentDto;
import kr.or.ddit.finalProject.dto.message.MessageRoomDto;
import kr.or.ddit.finalProject.dto.message.MessageRoomSummaryDto;
import kr.or.ddit.finalProject.exception.ErrorCode;
import kr.or.ddit.finalProject.exception.FinalProjectException;
import kr.or.ddit.finalProject.mapper.MemberMapper;
import kr.or.ddit.finalProject.mapper.MessageMapper;
import kr.or.ddit.finalProject.paging.PaginationInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final MemberMapper memberMapper;
    private final MessageMapper messageMapper;


    @Override
    @Transactional
    public MessageRoomDto createGroupChatRoom(String creatorUserId,
            CreateMessageRoomRequestDto requestDto) {
        // 채팅방 생성

        String roomNm = requestDto.getRoomName();
        List<String> partUserIds = requestDto.getParticipantIds();

        // partUserIds 목록의 회원들이 존재하는지 확인
        int existingCount = memberMapper.isAllExistUsers(partUserIds);
        if (existingCount != partUserIds.size()) {
            throw new FinalProjectException(ErrorCode.USER_NOT_FOUND);
        }

        // 채팅방 생성
        MessageRoomDto newRoom = MessageRoomDto.builder().roomTypeCd("02") // 그룹채팅
                .roomNm(roomNm).opnrUserId(creatorUserId).build();
        int result = messageMapper.insertGroupChatRoom(newRoom);
        if (result <= 0) {
            throw new FinalProjectException(ErrorCode.CHAT_ROOM_CREATION_FAILED);
        }

        // 채팅방 참여자 추가 (creatorUserId 포함)
        List<String> participantIds = new ArrayList<>(partUserIds);
        participantIds.add(creatorUserId);

        for (String userId : participantIds) {
            messageMapper.insertChatRoomParticipant(newRoom.getRoomSn(), userId);
        }

        return newRoom;
    }

    @Override
    public List<MessageContentDto> getChatMessages(long roomSn, int screenSize, int page,
            Authentication authentication) {

        boolean isParticipant = messageMapper.isParticipant(roomSn, authentication.getName()) > 0;
        if (!isParticipant) {
            throw new FinalProjectException(ErrorCode.CHAT_ROOM_ACCESS_DENIED);
        }

        PaginationInfo<MessageContentDto> paginationInfo = new PaginationInfo<>(screenSize, page);
        List<MessageContentDto> messages =
                messageMapper.selectChatMessagesByRoomSn(roomSn, paginationInfo);

        // 메시지를 읽은 것으로 처리 (lst_read_msg_sn 업데이트)
        if (!messages.isEmpty()) {
            long lastMsgSn = messages.get(0).getMsgSn(); // DESC로 가져왔으므로 첫 번째 메시지가 가장 최신 메시지
            updateLastReadMessage(roomSn, authentication.getName(), lastMsgSn);
        }

        /*
        
        private Long msgSn; // 기본키(PK) · 시퀀스
        private Long roomSn;
        private String msgTypeCd; // 01:텍스트 02:이미지 03:파일
        private String msgCn;
        private LocalDateTime sndDt;
        private String sendrUserId; // MEMBER.USER_ID 참조
        private String delYn; // Y:삭제 / N:정상
        private LocalDateTime delDt; // DEL_YN='Y' 시 삭제 처리 일시
        private String atchFileId; // 공통첨부파일분류
        
        private String memName;
        private String memRole;
        */

        return messages;
    }


    @Override
    public List<MessageRoomSummaryDto> getChatRoomList(String userId) {
        return messageMapper.selectAllChatRoomsByUserId(userId);
    }

    @Override
    public MessageRoomDto getGroupChatRoom(long roomSn, Authentication authentication) {
        String userId = authentication.getName();

        // 채팅방의 정보를 조회 하는데, 요청한 사용자가 해당 채팅방의 참여자인지 확인하고 참여자가 아니라면 예외를 발생시킴
        MessageRoomDto room = messageMapper.selectChatRoomByRoomSn(roomSn, userId);
        if (room == null) {
            throw new FinalProjectException(ErrorCode.CHAT_ROOM_NOT_FOUND);
        }

        boolean isParticipant = messageMapper.isParticipant(roomSn, userId) > 0;
        if (!isParticipant) {
            throw new FinalProjectException(ErrorCode.CHAT_ROOM_ACCESS_DENIED);
        }

        return room;
    }

    @Override
    public Long getOrCreateOneOnOneChatRoom(String myId, String otherId) {
        // otherId가 존재하는 회원인지 확인
        if (memberMapper.isAllExistUsers(List.of(otherId)) != 1) {
            throw new FinalProjectException(ErrorCode.USER_NOT_FOUND);
        }
        // 이미 존재하는 일대일 채팅방이 있는지 확인
        Long existingRoomSn = messageMapper.selectOneOnOneChatRoom(myId, otherId);
        if (existingRoomSn != null) {
            return existingRoomSn;
        }

        // 일대일 채팅방이 존재하지 않으면 새로 생성
        MessageRoomDto newRoom = new MessageRoomDto();
        newRoom.setRoomNm("1:1 채팅방 (" + myId + " & " + otherId + ")");
        newRoom.setRoomTypeCd("01"); // 01: 1:1 DM
        newRoom.setOpnrUserId(myId);
        messageMapper.insertGroupChatRoom(newRoom);

        // 채팅방 참여자 추가 (myId와 otherId)
        messageMapper.insertChatRoomParticipant(newRoom.getRoomSn(), myId);
        messageMapper.insertChatRoomParticipant(newRoom.getRoomSn(), otherId);

        return newRoom.getRoomSn();
    }

    @Override
    @Transactional
    public void sendMessage(MessageContentDto messageContentDto) {

        // 메시지를 저장하기 전에, 해당 사용자가 채팅방의 참여자인지 확인하고 참여자가 아니라면 예외를 발생시킴
        boolean isParticipant = messageMapper.isParticipant(messageContentDto.getRoomSn(),
                messageContentDto.getSendrUserId()) > 0;
        if (!isParticipant) {
            throw new FinalProjectException(ErrorCode.CHAT_ROOM_ACCESS_DENIED);
        }

        // 메시지 저장
        int result = messageMapper.insertChatMessage(messageContentDto);
        log.info("Inserted chat message: {}", messageContentDto);
        if (result <= 0) {
            throw new FinalProjectException(ErrorCode.CHAT_MESSAGE_CREATION_FAILED);
        }

        // 메세지 저장 후, 해당 채팅방의 마지막 메세지를 갱신
        int updateResult = messageMapper.updateLastSentMessage(messageContentDto.getRoomSn());
        if (updateResult <= 0) {
            log.warn("Failed to update last sent message timestamp for roomSn: {}",
                    messageContentDto.getRoomSn());
        }

        // message_room_participant 테이블에서 해당하는 참여자의 lst_read_msg_sn보다 큰 메시지들을 읽지 않은 메시지로 간주하여, 필요에 따라 알림 처리 등을 할 수 있음

        // 브로드캐스트용 발신자 정보 채우기 (실시간 메시지에 이름/부서/직급 표시)
        MessageContentDto senderInfo =
                messageMapper.selectSenderInfo(messageContentDto.getSendrUserId());
        if (senderInfo != null) {
            messageContentDto.setUserName(senderInfo.getUserName());
            messageContentDto.setPartDeptNm(senderInfo.getPartDeptNm());
            messageContentDto.setPartJbgrNm(senderInfo.getPartJbgrNm());
        }
    }

    @Override
    public boolean isUserInChatRoom(long roomSn, String userId) {
        return messageMapper.isParticipant(roomSn, userId) > 0;
    }

    // 사용자가 채팅방에서 메시지를 읽었을 때, 해당 메시지의 일련번호(msgSn)를 기준으로 lst_read_msg_sn을 업데이트하는 메서드
    @Override
    public void updateLastReadMessage(long roomSn, String userId, long msgSn) {
        messageMapper.updateLastReadMessage(roomSn, userId, msgSn);

    }

    // ChatServiceImpl
    @Override
    public List<String> getChatRoomParticipantIds(long roomSn) {
        return messageMapper.selectChatRoomParticipantIds(roomSn);
    }


}
