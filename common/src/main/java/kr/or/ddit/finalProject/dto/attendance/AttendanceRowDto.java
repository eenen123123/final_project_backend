package kr.or.ddit.finalProject.dto.attendance;

import lombok.Data;

@Data
public class AttendanceRowDto {
    private String studentId;
    private String studentName;
    private String status; // ATTEND / LATE / ABSENT / EARLY_LEAVE / null(미입력)
}
