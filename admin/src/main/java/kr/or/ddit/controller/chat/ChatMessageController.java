package kr.or.ddit.controller.chat;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import kr.or.ddit.finalProject.dto.file.FileDto;
import kr.or.ddit.finalProject.dto.message.MessageContentDto;
import kr.or.ddit.finalProject.exception.FinalProjectException;
import kr.or.ddit.finalProject.service.chat.ChatService;
import kr.or.ddit.finalProject.service.file.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;



@Slf4j
@RequiredArgsConstructor
@RestController
public class ChatMessageController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;
    private final FileUploadService fileUploadService;

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


    // 메시지를 읽은 것으로 처리하는 엔드포인트 (예: 사용자가 채팅방에서 메시지를 읽었을 때 호출, 구독한 클라이언트에서 메시지를 읽었을 때 프론트엔드에서 호출)
    @PostMapping("/chat/read")
    public String markMessageAsRead(@RequestParam String roomSn, @RequestParam String msgSn,
            Authentication authentication) {
        chatService.updateLastReadMessage(Long.parseLong(roomSn), authentication.getName(),
                Long.parseLong(msgSn));
        return "OK";
    }

    // AdminExceptionHandler(@ControllerAdvice)보다 컨트롤러 로컬 핸들러가 우선 적용되므로
    // 채팅 엔드포인트에서 발생한 예외는 여기서 JSON으로 반환 (페이지 리다이렉트 방지)
    @ExceptionHandler(FinalProjectException.class)
    public ResponseEntity<Map<String, String>> handleException(FinalProjectException ex) {
        return ResponseEntity.status(ex.getErrorCode().getStatus())
                .body(Map.of("message", ex.getErrorCode().getMessage()));
    }

    @PostMapping("/chat/file")
    public ResponseEntity<MessageContentDto> uploadFile(@RequestParam MultipartFile file,
            @RequestParam String roomSn, Authentication authentication) {
        String userId = authentication.getName();
        FileDto fileDto = fileUploadService.uploadFile(file, userId);

        // fileExtNm은 확장자가 아닌 MIME 타입(예: image/png)을 저장함
        String msgTypeCd =
                fileDto.getFileExtNm() != null && fileDto.getFileExtNm().startsWith("image/") ? "02"
                        : "03";

        MessageContentDto msg = MessageContentDto.builder().roomSn(Long.parseLong(roomSn))
                .sendrUserId(userId).msgTypeCd(msgTypeCd).msgCn(fileDto.getSavePathNm())
                .atchFileId(String.valueOf(fileDto.getAtchFileDtlSn())).build();

        chatService.sendMessage(msg);

        // fileNm은 DB에 저장되지 않으므로 DB 저장 완료 후 WS 브로드캐스트용으로만 세팅
        msg.setFileNm(fileDto.getOrgnFileNm());

        messagingTemplate.convertAndSend("/topic/messages/" + roomSn, msg);

        return ResponseEntity.ok(msg);
    }

}
