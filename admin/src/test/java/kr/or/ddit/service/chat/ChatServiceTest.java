package kr.or.ddit.service.chat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import kr.or.ddit.finalProject.dto.message.MessageRoomDto;
import kr.or.ddit.finalProject.service.chat.ChatService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
public class ChatServiceTest {

    @Autowired
    ChatService chatService;

    @Test
    void createGroupChatRoomTest() {
        String roomNm = "테스트 그룹 채팅방";
        String opnrUserId = "testuser01";
        List<String> partUserIds = List.of("testinstructor01");

        MessageRoomDto newRoom = chatService.createGroupChatRoom(roomNm, opnrUserId, partUserIds);
        log.info("Created group chat room: {}", newRoom);
    }

    @Test
    void getChatRoomListTest() {
        String userId = "testuser01";
        List<MessageRoomDto> chatRoomList = chatService.getChatRoomList(userId);
        for (MessageRoomDto room : chatRoomList) {
            log.info("Chat Room: {}", room);
        }

    }

}
