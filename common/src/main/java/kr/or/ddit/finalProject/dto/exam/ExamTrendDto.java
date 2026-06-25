package kr.or.ddit.finalProject.dto.exam;

import lombok.Data;

/** 시험별 평균 득점률 추이 */
@Data
public class ExamTrendDto {

    private Long examSn;
    private String examNm;
    private int avgScoreRate;   // 0~100 (TOT_SCORE / 만점 * 100)
    private int takerCount;
}
