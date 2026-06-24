package kr.or.ddit.finalProject.dto.exam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeminiQuestionRequest {

    /** 클래스 SN (선택 — 있으면 약점 분석 컨텍스트 포함) */
    private Long classSn;

    /** 과목 ID (FK → SUBJECT.SUBJ_ID) */
    private Long subjId;

    /** 과목명 (프롬프트 삽입용) */
    private String subjNm;

    /** 난이도 */
    private Difficulty difficulty;

    /** 강사 추가 요구사항 (선택) — 프롬프트 끝에 삽입 */
    private String extraPrompt;
}
