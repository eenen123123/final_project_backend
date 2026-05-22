package kr.or.ddit.finalProject.dto.chat;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MsgContDto {
    private Long msgSn; // 메시지 일련번호 (PK)
    private Long roomSn; // 채팅방 일련번호 (FK)
    private String msgTypeCd; // 메시지 유형코드 (01:텍스트, 02:이미지, 03:파일, 04:이모티콘)
    private String msgCn; // 메시지 내용 (텍스트 메시지의 경우 텍스트, 이미지/파일 메시지의 경우 파일 경로)
    private LocalDateTime sndDt; // 메시지 전송일시
    private String sendrUserId; // 메시지 발신자 사용자ID (FK)
    private String delYn; // 메시지 삭제 여부 (Y:삭제, N:존재)
    private LocalDateTime delDt; // 메시지 삭제일시 (삭제된 메시지의 경우 필수, 존재하는 메시지는 null 허용)
    private String atchFileId; // 첨부파일ID 


}
