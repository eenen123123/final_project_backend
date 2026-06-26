package kr.or.ddit.finalProject.dto.classroom;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MySummaryResponse {
    private int progressRate;
    private int assignSubmitRate;
    private int upcomingExamCount;
    private Double avgScore;
}
