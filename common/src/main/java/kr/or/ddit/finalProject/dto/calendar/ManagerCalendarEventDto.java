package kr.or.ddit.finalProject.dto.calendar;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 매니저 통합 캘린더 이벤트 DTO (학부모 상담 + 퇴원 방어 공용)
 * 출처별로 색/동작을 구분해 한 캘린더에 함께 표시한다.
 */
@Data
@AllArgsConstructor
public class ManagerCalendarEventDto {
    private String source;       // 출처 (상담 / 방어)
    private String title;        // 대상자명
    private LocalDateTime dt;     // 일시
    private Long refId;          // 원본 PK (상담: CNSL_SN / 방어: RTNP_SN)
    private String label;        // 상태/결과명 (툴팁용)
}
