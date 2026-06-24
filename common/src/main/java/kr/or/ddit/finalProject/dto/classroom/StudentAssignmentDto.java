package kr.or.ddit.finalProject.dto.classroom;

import lombok.Data;

@Data
public class StudentAssignmentDto {
    private Long asgmtSn;
    private String asgmtSj;       // 과제 제목
    private String sbmtDdlnDt;    // 마감일시
    private String sbmtYn;        // 제출 여부 (Y/N)
    private String grddYn;        // 채점 여부 (Y/N)
    private Double score;         // 점수 (채점 완료 시)
}
