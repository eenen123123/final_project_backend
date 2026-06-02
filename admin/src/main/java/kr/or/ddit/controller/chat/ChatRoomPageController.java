package kr.or.ddit.controller.chat;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import kr.or.ddit.finalProject.dto.member.AdminMemberDto;
import kr.or.ddit.finalProject.dto.message.CreateMessageRoomRequestDto;
import kr.or.ddit.finalProject.dto.message.MessageContentDto;
import kr.or.ddit.finalProject.dto.message.MessageRoomDto;
import kr.or.ddit.finalProject.dto.message.MessageRoomParticipantDto;
import kr.or.ddit.finalProject.dto.message.MessageRoomSummaryDto;
import kr.or.ddit.finalProject.mapper.MemberMapper;
import kr.or.ddit.finalProject.service.chat.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/chat/room")
public class ChatRoomPageController {

    private final ChatService chatService;
    private final MemberMapper memberMapper;

    @GetMapping("/list")
    public String getChatRoomList(Model model, Authentication authentication) {

        String userId = authentication.getName();
        List<MessageRoomSummaryDto> chatRoomList = chatService.getChatRoomList(userId);
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
    public String getPage(@RequestParam long roomSn, Model model, Authentication authentication) {

        MessageRoomDto room = chatService.getGroupChatRoom(roomSn, authentication);
        model.addAttribute("room", room);

        Map<String, List<MessageRoomParticipantDto>> groupedParticipants =
                room.getParticipants().stream()
                        .collect(Collectors.groupingBy(MessageRoomParticipantDto::getPartDeptNm,
                                LinkedHashMap::new, Collectors.toList()));
        model.addAttribute("groupedParticipants", groupedParticipants);

        List<MessageContentDto> messages =
                chatService.getChatMessages(roomSn, 20, 1, authentication); // 최신 20개 메시지
        Collections.reverse(messages); // DESC로 가져온 거 뒤집어서 오래된 순으로 표시
        model.addAttribute("chatMessages", messages);
        model.addAttribute("currentUserId", authentication.getName());


        return "admin:/chat/chatRoom";
    }

    // 채팅방 생성 페이지로 이동
    @GetMapping("/create")
    public String createChatRoom(Model model, Authentication authentication) {
        String userId = authentication.getName();
        List<AdminMemberDto> adminUsers = memberMapper.getAdminUsers(userId);
        Map<String, List<AdminMemberDto>> groupedAdminUsers = adminUsers.stream()
                .collect(Collectors.groupingBy(adminUser -> adminUser.getEmployeeInfo().getDeptNm(),
                        LinkedHashMap::new, Collectors.toList()));
        model.addAttribute("groupedAdminUsers", groupedAdminUsers);
        return "admin:/chat/createChatRoom";
    }

    // 채팅방 생성 처리
    @PostMapping("/create")
    public String createChatRoom(@ModelAttribute CreateMessageRoomRequestDto requestDto,
            Authentication authentication) {
        String creatorUserId = authentication.getName();
        MessageRoomDto newRoom = chatService.createGroupChatRoom(creatorUserId, requestDto);
        log.info("Created group chat room: {}", newRoom);
        return "redirect:/admin/chat/room/page?roomSn=" + newRoom.getRoomSn();
    }

    // 일대일 채팅방 생성 또는 기존 채팅방으로 이동
    @GetMapping("/direct")
    public String createDirectChatRoom(@RequestParam(defaultValue = "") String otherUserId,
            Authentication authentication, Model model) {
        // otherUserId가 없으면 사용자 목록에서 선택하도록 페이지로 이동, 있으면 바로 1:1 채팅방으로 이동
        if (otherUserId.trim().isBlank()) {
            String userId = authentication.getName();
            List<AdminMemberDto> adminUsers = memberMapper.getAdminUsers(userId);
            Map<String, List<AdminMemberDto>> groupedAdminUsers = adminUsers.stream()
                    .collect(Collectors.groupingBy(
                            adminUser -> adminUser.getEmployeeInfo().getDeptNm(),
                            LinkedHashMap::new, Collectors.toList()));
            model.addAttribute("groupedAdminUsers", groupedAdminUsers);
            return "admin:/chat/createDirectChatRoom";
        } else {
            Long roomSn =
                    chatService.getOrCreateOneOnOneChatRoom(authentication.getName(), otherUserId);
            return "redirect:/admin/chat/room/page?roomSn=" + roomSn;
        }
    }
}
