package kr.or.ddit.finalProject.dto.exam;

import lombok.Data;

/** 클래스 수강생의 과목별 평균 득점률 (약점 분석용) */
@Data
public class WeakPointDto {

    private Long subjId;
    private String subjNm;

    /** 평균 득점률 (0~100, SCORE / ALLOC_SCR 평균) */
    private int avgScoreRate;
}
