package kr.or.ddit.finalProject.dto.attendance;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 클래스룸 수강생별 근태 특이사항 집계 (강사용 출결 조회)
 */
@Data
public class ClassAttendanceSummaryDto {

    private String studentId;
    private String studentName;
    private int absentCnt;
    private int lateCnt;
    private int earlyCnt;
    private String lastNote;
    private String lastType;      // ABSENT / LATE / EARLY_LEAVE
    private LocalDateTime lastNoteDt;
}
