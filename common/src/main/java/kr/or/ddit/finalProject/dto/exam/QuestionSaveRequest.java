package kr.or.ddit.finalProject.dto.exam;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 문항 저장 요청 DTO (등록 / 수정 공용)
 *
 * POST /instructor/exams/questions 또는
 * POST /instructor/exams/questions/{sn}/update 폼 제출 시 바인딩됩니다.
 *
 * [유형별 입력 규칙]
 *   객관식(01): stem + choice1~4(보기) + corrAnswCn("1"~"4")
 *   주관식(02): stem + corrAnswCn(정답 키워드) + maxLength(최대 글자 수, 선택)
 *   서술형(03): stem + scoringCriteria(채점 기준, 선택) + corrAnswCn(모범 답안, 선택)
 *
 * [QSTN_CN JSON 변환]
 *   서비스에서 stem + choices 를 조합해 JSON 문자열을 생성한 뒤 DB에 저장합니다.
 *   이 DTO는 폼 바인딩 전용이며, 직접 DB에 저장되지 않습니다.
 */
@Data
public class QuestionSaveRequest {

    /**
     * 문항 유형 코드
     * 01: 객관식 / 02: 주관식 / 03: 서술형
     */
    private String qstnTypeCd;

    /** 문항 본문 (QSTN_CN JSON의 "stem" 값이 됩니다) */
    private String stem;

    // 객관식 보기 (qstnTypeCd = '01' 일 때만 사용)
    private String choice1;
    private String choice2;
    private String choice3;
    private String choice4;

    /**
     * 정답
     * 객관식: "1"~"4" 중 하나
     * 주관식: 정답 키워드 (채점 참고용)
     * 서술형: 모범 답안 (채점 참고용, null 허용)
     */
    private String corrAnswCn;

    /** 배점 */
    private BigDecimal allocScr;

    /** 해설 (선택 입력, 전 유형 공통) */
    private String explnCn;

    /** 과목 소분류 코드 (선택 입력) */
    private String subjDtclCd;

    /**
     * 주관식(02) 전용 — 답안 최대 글자 수 (선택)
     * QSTN_CN JSON의 "maxLength" 필드로 저장됩니다.
     */
    private Integer maxLength;

    /**
     * 서술형(03) 전용 — 채점 기준 (선택)
     * QSTN_CN JSON의 "scoringCriteria" 필드로 저장됩니다.
     */
    private String scoringCriteria;
}
