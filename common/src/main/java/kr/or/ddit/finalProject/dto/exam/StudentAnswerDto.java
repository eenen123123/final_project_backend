package kr.or.ddit.finalProject.dto.exam;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class StudentAnswerDto {

    private Long qstnSn;          // 문항 PK
    private String qstnTypeCd;    // MULTIPLE_CHOICE / SHORT_ANSWER / ESSAY
    private String qstnCn;        // 문항 본문 (JSON, 내부 처리용)
    private BigDecimal allocScr;  // 배점
    private String corrAnswCn;    // 정답 — 서비스에서 1-indexed 숫자로 정규화됨
    private Integer sortOrd;      // 시험 내 문항 순서 (QSTN_ORDR)

    private Long sbmtAnswSn;      // ANSWER_SUBMIT PK (미응시 시 null)
    private String sbmtAnswCn;    // 학생 제출 답안 (1-indexed 숫자)
    private BigDecimal score;     // 획득 점수 (null=미채점)
    private String grdgUserId;    // 채점자 ID
    private String grdgDt;        // 채점일시 (yyyy-MM-dd HH:mm)

    // 서비스에서 qstnCn JSON 파싱 후 채워지는 필드
    private String stem;          // 문항 본문 텍스트
    private List<String> choices; // 선지 목록 (MC만)
    private Boolean correct;      // 객관식 정답 여부 (MC만, 미응시 시 null)
}
