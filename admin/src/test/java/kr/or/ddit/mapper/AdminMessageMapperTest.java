package kr.or.ddit.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import kr.or.ddit.finalProject.mapper.MessageMapper;
import kr.or.ddit.finalProject.paging.PaginationInfo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
public class AdminMessageMapperTest {

    @Autowired
    MessageMapper messageMapper;

    @Test
    void selectChatRoomByRoomSn() {
        // Given
        long roomSn = 10L; // 테스트할 채팅방 일련번호

        // When
        var room = messageMapper.selectChatRoomByRoomSn(roomSn);

        // Then
        log.info("조회된 채팅방 정보: {}", room.getParticipants());
    }

    @Test
    void selectAllChatRoomsByUserId() {
        // Given
        String userId = "testuser01"; // 테스트할 사용자ID  

        // When
        var chatRooms = messageMapper.selectAllChatRoomsByUserId(userId);
        // Then
        chatRooms.forEach(room -> log.info("조회된 채팅방: {}", room.getParticipants().size()));
    }

    @Test
    void selectChatMessagesByRoomSn() {
        // Given
        long roomSn = 10L; // 테스트할 채팅방 일련번호
        int page = 1;
        int screenSize = 20;

        // When
        var messages = messageMapper.selectChatMessagesByRoomSn(roomSn,
                new PaginationInfo<>(screenSize, page));

        // Then
        messages.forEach(message -> log.info("조회된 메시지: {}", message));
    }
}
