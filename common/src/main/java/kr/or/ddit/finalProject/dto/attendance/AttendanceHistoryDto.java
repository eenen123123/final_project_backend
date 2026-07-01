package kr.or.ddit.finalProject.dto.attendance;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 학생 1명의 근태 특이사항(결석/지각/조퇴) 전체 이력
 */
@Data
public class AttendanceHistoryDto {

    private String atndType; // ABSENT / LATE / EARLY_LEAVE
    private LocalDateTime atndRegDt;
    private String note;
}
