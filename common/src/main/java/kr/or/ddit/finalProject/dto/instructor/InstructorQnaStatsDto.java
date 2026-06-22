package kr.or.ddit.finalProject.dto.instructor;

import lombok.Data;

@Data
public class InstructorQnaStatsDto {
    private String instrUserId;
    private String instrUserNm;
    private int totalCnt;
    private int answeredCnt;
    private int unansweredCnt;
    private double answerRate;
    private Double avgResponseHours;
}
