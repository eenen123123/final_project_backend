package kr.or.ddit.finalProject.dto.classroom;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StudentExamResponse {
    private Long examSn;
    private String examNm;
    private String examStrtDt;
    private String examEndDt;
    private String status;    // UPCOMING | ONGOING | CLOSED
    private boolean attempted;
    private Double score;
}
