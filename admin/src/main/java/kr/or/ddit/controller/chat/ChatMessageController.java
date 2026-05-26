package kr.or.ddit.controller.chat;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RestController;
import kr.or.ddit.finalProject.dto.message.MessageContentDto;
import kr.or.ddit.finalProject.service.chat.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;



@Slf4j
@RequiredArgsConstructor
@RestController
public class ChatMessageController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    @MessageMapping("/chat/send")
    public ResponseEntity<MessageContentDto> sendMessage(MessageContentDto messageContent,
            Authentication authentication) {
        // 메세지를 받음
        log.info("Received message: {}", messageContent);

        chatService.sendMessage(messageContent);
        // 위에서 예외가 발생하지 않으면, 해당 사용자가 채팅방의 참여자이므로 메시지를 전송

        messagingTemplate.convertAndSend("/topic/messages/" + messageContent.getRoomSn(),
                messageContent);

        return ResponseEntity.ok(messageContent); // 클라이언트에게도 메시지 내용을 반환
    }

    @GetMapping("/chat/more")
    public ResponseEntity<List<MessageContentDto>> getMoreMessages(@RequestParam String roomSn,
            @RequestParam int size, @RequestParam int page, Authentication authentication) {
        List<MessageContentDto> messages =
                chatService.getChatMessages(Long.parseLong(roomSn), size, page, authentication);

        return ResponseEntity.ok(messages);
    }

}
