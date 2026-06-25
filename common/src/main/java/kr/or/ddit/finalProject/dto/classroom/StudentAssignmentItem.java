package kr.or.ddit.finalProject.dto.classroom;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StudentAssignmentItem {
    private Long asgmtSn;
    private String asgmtNm;
    private String dueDt;
    private boolean submitted;
    private Double score;
}
