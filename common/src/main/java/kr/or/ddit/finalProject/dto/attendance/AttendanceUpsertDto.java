package kr.or.ddit.finalProject.dto.attendance;

import lombok.Data;

@Data
public class AttendanceUpsertDto {
    private String stdUserId;
    private String atndTypeCd; // 01=출석, 02=결석, 03=지각, 04=조퇴
    private String date;       // yyyy-MM-dd
}
