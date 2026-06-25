package kr.or.ddit.finalProject.dto.exam;

import lombok.Data;

/** 난이도별 평균 득점률 */
@Data
public class DifficultyStatsDto {

    private String diffCd;       // EASY / MEDIUM / HARD / null
    private int avgScoreRate;    // 0~100
    private int answerCount;
}
