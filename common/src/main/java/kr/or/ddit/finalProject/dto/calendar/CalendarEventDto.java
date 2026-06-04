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
public class CalendarEventDto implements Serializable {

    private Long eventSn; // PK · 자동증가
    private String eventType; // 이벤트 유형 (event: 혜택/이벤트, academic: 학사일정)
    private String eventTitle; // 이벤트 제목
    private String eventCont; // 이벤트 내용
    private LocalDate startDt; // 시작일
    private LocalDate endDt; // 종료일
    private String regUserId; // 등록 관리자 ID (FK → MEMBER.USER_ID)
    private LocalDateTime regDt; // 등록일시
    private LocalDateTime mdfcnDt; // 수정일시
    private String mdfcnUserId; // 최종 수정 관리자 ID (FK → MEMBER.USER_ID)

}
