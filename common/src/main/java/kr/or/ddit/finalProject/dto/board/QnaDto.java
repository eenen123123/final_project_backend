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
public class QnaDto implements Serializable {

    private Long postSn;
    private String qnaCtgCd; // QnA 카테고리 (CL_CODE: 105)
    private String secrYn; // 비공개 여부
    private String answStatCd; // 답변 상태 (CL_CODE: 104)
    private String answCn; // 답변 내용
    private String answrUserId; // 답변자
    private LocalDateTime answDt; // 답변일

    // BOARD 조인 필드
    private String postSj;
    private String postCn;
    private String wrtrUserId;
    private LocalDateTime regDt;
    private LocalDateTime mdfcnDt;

    // 공통코드 조인 필드
    private String qnaCtgNm; // QnA 카테고리명
    private String answStatNm; // 답변 상태
}
