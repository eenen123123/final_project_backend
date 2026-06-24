package kr.or.ddit.finalProject.dto.exam;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * QUESTION_LIST 테이블 매핑 DTO
 *
 * [QSTN_TYPE_CD — QuestionType enum]
 *   MULTIPLE_CHOICE : 객관식 (choices 배열, 선지 수 가변)
 *   SHORT_ANSWER    : 단답형 (maxLength 선택)
 *   ESSAY           : 서술형 (scoringCriteria 선택)
 *
 * [DIFF_CD — Difficulty enum]
 *   EASY / MEDIUM / HARD
 *
 * [QSTN_CN 저장 형식 — JSON CLOB]
 *   {"stem":"...","topic":"...","choices":["A. ...","B. ..."],"chartData":null}
 *   topic, chartData 는 AI 생성 문항에서 사용. 수동 문항은 null 허용.
 *   조회 후 서비스에서 필드로 파싱합니다.
 *
 * [STAT_CD]
 *   01: 정상 / 99: 삭제됨 (논리 삭제)
 */
@Data
public class QuestionDto {

    /** 문항 일련번호 (PK) */
    private Long qstnSn;

    /** 과목 ID (FK → SUBJECT.SUBJ_ID) */
    private Long subjId;

    /** 등록자 ID (문항을 생성한 강사) */
    private String rgtrId;

    /** 최종 수정자 ID */
    private String lastMdfrId;

    /** 등록 일시 */
    private String regDt;

    /** 최종 수정 일시 */
    private String mdfcnDt;

    /** 문항 유형 (QuestionType enum) */
    private QuestionType qstnTypeCd;

    /** 난이도 (Difficulty enum) */
    private Difficulty diffCd;

    /** AI 생성 여부 (Y=Gemini 생성, N=수동 작성) */
    private String aiGenYn;

    /** 배점 (소수점 2자리) */
    private BigDecimal allocScr;

    /**
     * 정답
     * 객관식: 선지 레이블 (예: "A")
     * 단답형: 정답 텍스트
     * 서술형: 채점 기준 참고용 모범 답안 (null 허용)
     */
    private String corrAnswCn;

    /** 해설 (선택 입력) */
    private String explnCn;

    /** 상태 코드 (01: 정상 / 99: 삭제됨) */
    private String statCd;

    /** QSTN_CN CLOB — JSON 직렬화 문자열 (DB 저장용, 클라이언트 노출 불필요) */
    @JsonIgnore
    private String qstnCn;

    // ── QSTN_CN JSON 파싱 결과 (DB 컬럼 아님, 서비스에서 채워짐) ──────────

    /** 문항 본문 (LaTeX 포함 가능) */
    private String stem;

    /** AI가 생성한 세부 주제 (예: "삼각함수 - 사인법칙") */
    private String topic;

    /** 선지 목록 (선지 수 가변, 객관식에만 해당) */
    private List<String> choices;

    /** 단답형 전용 — 답안 최대 글자 수 (null=제한 없음) */
    private Integer maxLength;

    /** 서술형 전용 — 채점 기준 루브릭 */
    private String scoringCriteria;

    /** Chart.js용 도표 데이터 JSON (AI 생성 도표 문항에만 해당, null 허용) */
    private String chartData;
}
