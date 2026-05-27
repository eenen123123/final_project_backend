package kr.or.ddit.finalProject.dto.board;

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
public class BoardDto implements Serializable {

    private Long postSn;        // PK 자동채번
    private String wrtrUserId;  // 작성자
    private String boardTypeCd; // 게시판 타입
    private String postSj;      // 제목
    private String postCn;      // 내용
    private String atchFileId;  // 첨부파일 분류 ID
    private Long topFixOrd;     // 상단고정
    private String popupExpsYn; // 팝업노출
    private Long inqCnt;        // 조회수
    private String lastMdfrId;  // 마지막수정자
    private LocalDateTime regDt; // 등록일
    private LocalDateTime mdfcnDt;// 수정일
}
