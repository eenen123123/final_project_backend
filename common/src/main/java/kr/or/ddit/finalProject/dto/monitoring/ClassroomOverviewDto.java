package kr.or.ddit.finalProject.dto.monitoring;

import lombok.Data;

@Data
public class ClassroomOverviewDto {
    private long classSn;
    private String classNm;
    private String classStatCd;
    private String enrlStrtYmd;
    private String enrlEndYmd;
    private String courseNm;
    private String instrUserId;
    private String instrUserNm;
    private int studentCnt;
    private int lectureCnt;
    private double avgProgressRate;
    private int completedLectureAvg;
}
