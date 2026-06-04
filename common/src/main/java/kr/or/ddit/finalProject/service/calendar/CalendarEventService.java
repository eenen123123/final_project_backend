package kr.or.ddit.finalProject.service.calendar;

import java.util.List;
import java.util.Map;
import kr.or.ddit.finalProject.dto.calendar.CalendarEventDto;

public interface CalendarEventService {

    // 월별 이벤트 조회
    List<CalendarEventDto> getCalendarEventList(Map<String, Object> param);

    // 단건 조회
    CalendarEventDto getCalendarEvent(Long eventSn);

    // 파라미터 없는 버전 추가
    List<CalendarEventDto> getCalendarEventList();

    // 등록
    int insertCalendarEvent(CalendarEventDto dto);

    // 수정
    int updateCalendarEvent(CalendarEventDto dto);

    // 삭제
    int deleteCalendarEvent(Long eventSn);

}
