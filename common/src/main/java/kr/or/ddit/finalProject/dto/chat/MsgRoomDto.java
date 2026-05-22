package kr.or.ddit.finalProject.dto.chat;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "roomSn") // roomSn을 기준으로 equals()와 hashCode() 생성
public class MsgRoomDto {
    private Long roomSn; // 채팅방 일련번호 (PK)
    private String roomTypeCd; // 채팅방 유형코드 (01:일대일, 02:그룹)
    private String roomNm; // 채팅방 이름 (그룹 채팅방의 경우 필수, 일대일 채팅방은 null 허용)
    private String opnrUserId; // 채팅방 개설자 사용자ID (FK)
    private LocalDateTime roomCretDt; // 채팅방 생성일시
    private LocalDateTime lstMsgDt; // 마지막 메시지 전송일시
    private String delYn; // 채팅방 삭제 여부 (Y:삭제, N:존재)

    private List<MsgContDto> msgContList; // MsgRoomDto has many MsgContDto
    private List<MsgRoomPart> msgRoomPartList; // MsgRoomDto has many MsgRoomPart

}
