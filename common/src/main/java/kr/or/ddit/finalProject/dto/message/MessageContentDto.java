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
public class MessageContentDto implements Serializable {

    private Long msgSn; // 기본키(PK) · 시퀀스
    private Long roomSn;
    private String msgTypeCd; // 01:텍스트 02:이미지 03:파일
    private String msgCn;
    private LocalDateTime sndDt;
    private String sendrUserId; // MEMBER.USER_ID 참조
    private String delYn; // Y:삭제 / N:정상
    private LocalDateTime delDt; // DEL_YN='Y' 시 삭제 처리 일시
    private String atchFileId; // 공통첨부파일분류

    private String userName;
    private String fileNm; // 파일 원본명 (DB 미매핑, WS 브로드캐스트 및 SELECT JOIN 전용)

    private String partDeptNm; // 부서명
    private String partJbgrNm; // 직급명
}
