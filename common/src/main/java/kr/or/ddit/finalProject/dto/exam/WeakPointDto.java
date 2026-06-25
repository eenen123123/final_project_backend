package kr.or.ddit.finalProject.dto.exam;

import lombok.Data;

/** 클래스 수강생의 세부 주제별 평균 득점률 (약점 분석용) */
@Data
public class WeakPointDto {

    private String topic;

    /** 평균 득점률 (0~100, SCORE / ALLOC_SCR 평균) */
    private int avgScoreRate;

    /** 분석에 사용된 답안 수 */
    private int answerCount;
}
