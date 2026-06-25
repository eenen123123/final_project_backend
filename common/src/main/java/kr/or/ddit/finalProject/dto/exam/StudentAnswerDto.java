package kr.or.ddit.finalProject.dto.exam;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class StudentAnswerDto {

    private Long qstnSn;          // 문항 PK
    private String qstnTypeCd;    // MULTIPLE_CHOICE / SHORT_ANSWER / ESSAY
    private String qstnCn;        // 문항 본문 (JSON)
    private BigDecimal allocScr;  // 배점
    private String corrAnswCn;    // 정답 (객관식 번호 or 주관식 텍스트)
    private Integer sortOrd;      // 시험 내 문항 순서 (QSTN_ORDR)

    private Long sbmtAnswSn;      // ANSWER_SUBMIT PK (미응시 시 null)
    private String sbmtAnswCn;    // 학생 제출 답안
    private BigDecimal score;     // 획득 점수 (null=미채점)
    private String grdgUserId;    // 채점자 ID
    private String grdgDt;        // 채점일시 (yyyy-MM-dd HH:mm)
}
