package kr.or.ddit.finalProject.dto.attendance;

import lombok.Data;

@Data
public class MyAttendanceDto {
    private String atndDate; // YYYY-MM-DD
    private String status;   // ATTEND / ABSENT / LATE / EARLY_LEAVE / null(미입력)
}
