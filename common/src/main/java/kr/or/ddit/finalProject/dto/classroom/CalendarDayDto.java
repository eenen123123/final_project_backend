package kr.or.ddit.finalProject.dto.classroom;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CalendarDayDto {
    private int day;         // 1 ~ 31
    private boolean today;   // 오늘 여부
    private boolean classDay; // 시험·과제 마감이 있는 날
}
