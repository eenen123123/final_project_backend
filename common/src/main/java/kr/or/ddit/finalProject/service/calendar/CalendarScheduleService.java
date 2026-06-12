package kr.or.ddit.finalProject.service.calendar;

import java.util.List;
import java.util.Map;
import kr.or.ddit.finalProject.dto.calendar.CalendarScheduleDto;

public interface CalendarScheduleService {

    // 월별 개인 일정 조회
    List<CalendarScheduleDto> getCalendarScheduleList(Map<String, Object> param);

    // 단건 조회
    CalendarScheduleDto getCalendarSchedule(Long scheduleSn);

    // 등록
    int insertCalendarSchedule(CalendarScheduleDto dto);

    // 수정
    void updateCalendarSchedule(CalendarScheduleDto dto);

    // 삭제
    void deleteCalendarSchedule(Long scheduleSn, String userId);

}
