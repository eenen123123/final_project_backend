package kr.or.ddit.finalProject.dto.parent;

import lombok.Data;

/**
 * 학부모 홈 대시보드 - 자녀의 수강 기간 전체 누적 근태 특이사항 요약
 */
@Data
public class ParentAttendanceSummaryDto {

    private int lateCount;
    private int absentCount;
    private int earlyLeaveCount;
}
