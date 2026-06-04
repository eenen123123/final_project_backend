package kr.or.ddit.finalProject.dto.calendar;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarScheduleDto implements Serializable {

    private Long scheduleSn; // PK · 자동증가
    private String userId; // 사용자 ID (FK → MEMBER.USER_ID)
    private String scheduleType; // 일정 유형 (academic: 학습일정, personal: 개인일정)
    private String scheduleTitle; // 일정 제목
    private String scheduleCont; // 일정 내용
    private LocalDate startDt; // 시작일
    private LocalDate endDt; // 종료일
    private LocalDateTime regDt; // 등록일시
    private LocalDateTime mdfcnDt; // 수정일시

}
