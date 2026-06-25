package kr.or.ddit.finalProject.dto.classroom;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StudentAssignmentDetail {
    private Long asgmtSn;
    private String asgmtNm;
    private String asgmtCn;
    private String dueDt;
    private boolean submitted;
    private String sbmtCn;
    private Double score;
    private String feedbackCn;   // 채점 완료 시 강사 피드백 (현재 DB에 없으면 null)
    private String resubmitYn;
}
