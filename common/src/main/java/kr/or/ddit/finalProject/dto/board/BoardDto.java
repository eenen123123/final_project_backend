package kr.or.ddit.finalProject.dto.board;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardDto implements Serializable {

    private Long postSn; // 기본키(PK) · 시퀀스
    private String wrtrUserId; // MEMBER.USER_ID 참조
    private String boardTypeCd;
    private String postSj;
    private String postCn;
    private String atchFileId; // 공통첨부파일분류
    private Long topFixOrd; // NULL이면 일반 게시글
    private String popupExpsYn; // Y:팝업노출 / N:미노출
    private Long inqCnt;
    private String lastMdfrId; // 마지막 수정자
    private LocalDateTime regDt;
    private LocalDateTime mdfcnDt;
}