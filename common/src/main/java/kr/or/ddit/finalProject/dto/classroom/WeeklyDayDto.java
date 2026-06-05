package kr.or.ddit.finalProject.dto.classroom;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WeeklyDayDto {
    private String label;      // "월", "화", "수", "목", "금", "토", "일"
    private double heightPct;  // 바 높이 % (4 ~ 100)
    private boolean isMax;     // 이번 주 최대 완료일
    private boolean isEmpty;   // 완료 0건
}
