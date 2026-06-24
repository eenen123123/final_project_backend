package kr.or.ddit.finalProject.dto.classroom;

import lombok.Data;

@Data
public class StudentExamDto {
    private Long examSn;
    private String examRegNm;     // 시험명
    private String examStrtDt;    // 시험 시작일시
    private String examEndDt;     // 시험 종료일시
    private String takenYn;       // 응시 여부 (Y/N)
    private Double totScore;      // 점수 (응시 완료 시)
    private String sbmtDt;        // 응시 제출 일시
}
