package kr.or.ddit.controller.chat;

import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import kr.or.ddit.finalProject.dto.message.MessageRoomDto;
import kr.or.ddit.finalProject.service.chat.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/chat/room")
public class ChatRoomPageController {

    private final ChatService chatService;

    @GetMapping("/list")
    public String getChatRoomList(Model model) {

        String userId = "testuser01"; // TODO: 로그인한 사용자ID로 변경
        List<MessageRoomDto> chatRoomList = chatService.getChatRoomList(userId);
        model.addAttribute("chatRoomList", chatRoomList);
        return "admin:/chat/chatList";
    }

    /**
     * 채팅방 페이지 조회
     * - 채팅방 페이지에서는 채팅방 정보와 함께 메시지 목록과 참여자 목록도 함께 조회하여 표시
     * - 일대일 채팅방과 그룹 채팅방 모두 동일한 페이지에서 처리, 화면에서 roomTypeCd로 구분하여 표시
     * 
     * @param roomSn 채팅방 일련번호 (필수)
     * @param model  모델 객체 (채팅방 정보, 메시지 목록, 참여자 목록 전달)
     * @return 채팅방 페이지 뷰 이름
     */
    @GetMapping("/page")
    public String getPage(@RequestParam long roomSn, Model model) {

        MessageRoomDto room = chatService.getGroupChatRoom(roomSn);
        model.addAttribute("room", room);

        return "admin:/chat/chatRoom";
    }

}
