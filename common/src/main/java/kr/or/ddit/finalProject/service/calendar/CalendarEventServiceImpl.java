package kr.or.ddit.finalProject.service.calendar;

import java.util.List;
import java.util.Map;
import kr.or.ddit.finalProject.dto.calendar.CalendarEventDto;
import kr.or.ddit.finalProject.mapper.calendar.CalendarEventMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalendarEventServiceImpl implements CalendarEventService {

    private final CalendarEventMapper calendarEventMapper;

    @Override
    public List<CalendarEventDto> getCalendarEventList(Map<String, Object> param) {
        return calendarEventMapper.selectCalendarEventList(param);
    }

    @Override
    public CalendarEventDto getCalendarEvent(Long eventSn) {
        return calendarEventMapper.selectCalendarEvent(eventSn);
    }

    @Override
    @Transactional
    public int insertCalendarEvent(CalendarEventDto dto) {
        return calendarEventMapper.insertCalendarEvent(dto);
    }

    @Override
    @Transactional
    public int updateCalendarEvent(CalendarEventDto dto) {
        return calendarEventMapper.updateCalendarEvent(dto);
    }

    @Override
    @Transactional
    public int deleteCalendarEvent(Long eventSn) {
        return calendarEventMapper.deleteCalendarEvent(eventSn);
    }

    @Override
    public List<CalendarEventDto> getCalendarEventList() {
        return calendarEventMapper.selectAllCalendarEventList();
    }

}
