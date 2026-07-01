package kr.or.ddit.finalProject.dto.monitoring;

import lombok.Data;

@Data
public class ClassroomGradeStatsDto {
    private long classSn;
    private String classNm;
    private int takerCnt;
    private double avgScore;
    private double maxScore;
    private double minScore;
    private int cnt90Plus;    // 90점 이상
    private int cntBelow60;  // 60점 미만
}
