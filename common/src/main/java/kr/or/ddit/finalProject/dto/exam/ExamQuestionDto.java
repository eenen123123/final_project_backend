package kr.or.ddit.finalProject.dto.exam;

import lombok.Data;

import java.math.BigDecimal;

/**
 * EXAM_QUESTION 테이블과 QUESTION_LIST 조인 결과 DTO
 *
 * 시험 상세 페이지에서 포함된 문항 목록을 표시하거나,
 * 시험 등록/수정 폼에서 문항 선택 목록을 렌더링할 때 사용합니다.
 */
@Data
public class ExamQuestionDto {

    /** 시험 일련번호 (FK → EXAM.EXAM_SN) */
    private Long examSn;

    /** 문항 일련번호 (FK → QUESTION_LIST.QSTN_SN) */
    private Long qstnSn;

    /** 시험 내 문항 순서 (1부터 시작) */
    private Integer qstnOrdr;

    // ── QUESTION_LIST 조인 컬럼 ─────────────────────────────────────────────

    /** 문항 유형 (QuestionType enum) */
    private QuestionType qstnTypeCd;

    /**
     * 문항 본문 (QSTN_CN JSON의 "stem" 값, 서비스에서 파싱)
     * DB 컬럼이 아닙니다.
     */
    private String stem;

    /** 배점 */
    private BigDecimal allocScr;

    /** 정답 */
    private String corrAnswCn;

    /** 등록자 ID (문항 소유자 확인 및 목록 표시용) */
    private String rgtrId;
}
