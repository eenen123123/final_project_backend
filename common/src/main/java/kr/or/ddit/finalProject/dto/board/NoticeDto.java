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
public class NoticeDto implements Serializable {

    private Long postSn;
    private String noticeTypeCd; // 공지 유형 (CL_CODE: 103)

    // BOARD 조인 필드
    private String postSj;
    private String postCn;
    private String wrtrUserId;
    private Long topFixOrd;
    private String popupExpsYn;
    private LocalDateTime regDt;
    private LocalDateTime mdfcnDt;

    // 공통코드 조인 필드
    private String noticeTypeNm; // 공지 유형
}
