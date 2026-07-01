package kr.or.ddit.finalProject.dto.parent;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParentAttendanceResponse {

    private int year;
    private int month;
    private int lateCount;
    private int absentCount;
    private int earlyLeaveCount;
    private List<Record> records;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Record {
        private int day;
        private String status;
        private String note;
    }
}
