package kr.or.ddit.finalProject.dto.exam;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 문항 저장 요청 DTO (등록 / 수정 공용, 폼 바인딩 전용)
 *
 * [유형별 입력 규칙]
 *   MULTIPLE_CHOICE : stem + choices(가변 선지) + corrAnswCn(선지 레이블, 예:"A")
 *   SHORT_ANSWER    : stem + corrAnswCn(정답 텍스트) + maxLength(선택)
 *   ESSAY           : stem + scoringCriteria(채점 기준, 선택) + corrAnswCn(모범 답안, 선택)
 */
@Data
public class QuestionSaveRequest {

    /** 문항 유형 (QuestionType enum) */
    private QuestionType qstnTypeCd;

    /** 과목 ID (FK → SUBJECT.SUBJ_ID) */
    private Long subjId;

    /** 난이도 (Difficulty enum) */
    private Difficulty diffCd;

    /** 문항 본문 (LaTeX 포함 가능) */
    private String stem;

    /** AI가 생성한 세부 주제 (수동 입력 시 null 허용) */
    private String topic;

    /**
     * 선지 목록 (선지 수 가변)
     * 객관식에만 해당. 비어있는 항목은 서비스에서 제거합니다.
     */
    private List<String> choices;

    /**
     * 정답
     * 객관식: 선지 레이블 (예: "A")
     * 단답형: 정답 키워드
     * 서술형: 모범 답안 (null 허용)
     */
    private String corrAnswCn;

    /** 배점 */
    private BigDecimal allocScr;

    /** 해설 (선택 입력, 전 유형 공통) */
    private String explnCn;

    /** 단답형 전용 — 답안 최대 글자 수 (선택) */
    private Integer maxLength;

    /** 서술형 전용 — 채점 기준 루브릭 (선택) */
    private String scoringCriteria;

    /** AI 생성 여부 (Y=Gemini 생성, N=수동 작성 / 기본값 N) */
    private String aiGenYn;
}
