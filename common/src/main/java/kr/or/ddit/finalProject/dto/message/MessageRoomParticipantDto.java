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
public class MessageRoomParticipantDto implements Serializable {

    private Long partSn; // 기본키(PK) · 시퀀스
    private Long roomSn;
    private String partUserId; // 채팅방 구성원
    private LocalDateTime joinDt; // 대화방 입장 일시
    private LocalDateTime exitDt; // 그룹 채팅 나가기 시 기록
    private String notiYn; // Y:알림ON / N:알림OFF
    private Long lstReadMsgSn; // 안읽은 메시지 수 계산 기준 읽을 때마다 갱신

    private String partUserName; //  이름

    private String partDeptNm; // 부서명
    private String partJbgrNm; // 직급명
}
