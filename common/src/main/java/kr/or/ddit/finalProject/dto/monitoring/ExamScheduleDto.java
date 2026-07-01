package kr.or.ddit.finalProject.dto.monitoring;

import lombok.Data;

@Data
public class ExamScheduleDto {
    private long examSn;
    private String examRegNm;
    private String examStrtDt;  // YYYY-MM-DD HH24:MI
    private String examEndDt;
    private long classSn;
    private String classNm;
    private int targetCnt;      // 수강생 수
    private int takerCnt;       // 응시자 수 (완료 시험용)
    private double avgScore;    // 평균 점수 (완료 시험용)
}
