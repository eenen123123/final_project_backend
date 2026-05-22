package kr.or.ddit.finalProject.dto.chat;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class MsgRoomPart {
    private Long partSn; // 참여 일련번호 (PK)
    private Long roomSn; // 채팅방 일련번호 (FK)
    private String partUserId; // 참여자 사용자ID (FK)
    private LocalDateTime joinDt; // 참여 일시
    private LocalDateTime exitDt; // 퇴장 일시 (퇴장한 경우 필수, 참여 중인 경우 null 허용)
    private String notiYn; // 알림 여부 (Y:알림, N:무음)
    private Long lstReadMsgSn; // 마지막으로 읽은 메시지 일련번호, 읽지 않은 메시지 수 계산에 사용  
}
