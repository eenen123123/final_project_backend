package kr.or.ddit.config;

import java.security.Principal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;
import kr.or.ddit.finalProject.exception.ErrorCode;
import kr.or.ddit.finalProject.exception.FinalProjectException;
import kr.or.ddit.finalProject.service.chat.ChatService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class StompSubscriptionInterceptor implements ChannelInterceptor {

    @Autowired
    private ChatService chatService;

    // 클라이언트가 구독 요청을 보낼 때, 해당 사용자가 채팅방의 참여자인지 확인
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            String destination = accessor.getDestination(); // "/topic/messages/123"
            Principal user = accessor.getUser();
            if (destination != null && destination.startsWith("/topic/messages/")) {
                long roomSn = Long.parseLong(destination.replace("/topic/messages/", ""));
                String userId = user.getName();
                boolean isParticipant = chatService.isUserInChatRoom(roomSn, userId);

                if (!isParticipant) {
                    throw new FinalProjectException(ErrorCode.CHAT_ROOM_ACCESS_DENIED);
                }
            }
        }
        return message;
    }
}
