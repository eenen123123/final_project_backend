package kr.or.ddit.finalProject.dto.message;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageRoomSummaryDto implements Serializable {

    private Long roomSn; // 기본키(PK) · 시퀀스
    private String roomTypeCd; // 01:1:1 DM / 02:그룹채팅
    private String roomNm; // 1:1 DM은 NULL (상대방 이름 표시)
    private String opnrUserId; // MEMBER.USER_ID 참조
    private LocalDateTime roomCretDt; // 대화방 생성 일시
    private LocalDateTime lstMsgDt; // 채팅방 목록 최근 메시지 정렬 기준
    private String delYn; // Y:삭제 / N:정상

    private int participantCount; // 채팅방 참여자 수
    private int unreadMsgCount; // 안읽은 메시지 수

}
