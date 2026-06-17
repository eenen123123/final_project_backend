package kr.or.ddit.finalProject.dto.suneung;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SUNEUNG_GRADE_CUT 테이블 매핑 DTO
 * 수능/모평 과목별 등급 구분 표준점수(등급컷) 한 행.
 */
@Data
@NoArgsConstructor
public class SuneungGradeCutDto {

    /** PK (GENERATED ALWAYS AS IDENTITY) */
    private Long id;

    /** 학년도 (예: 2026) */
    private Integer year;

    /** 시험 구분 (CSAT / JUNE_MOCK / SEPT_MOCK) */
    private ExamType examType;

    /** 과목명 */
    private String subject;

    /** 등급 (1~9) */
    private Integer grade;

    /** 등급 구분 표준점수 */
    private Integer cutScore;

    /** 인원(명) */
    private Long people;

    /** 비율(퍼센트) */
    private Double ratio;

    private Long subjClId; // 과목 대분류 ID
    /*
     * 1 국어
     * 2 수학
     * 3 영어
     * 4 사회탐구
     * 5 과학탐구
     * 21 한국사
     */

    private String subjClNm; // 과목 대분류 이름
}
