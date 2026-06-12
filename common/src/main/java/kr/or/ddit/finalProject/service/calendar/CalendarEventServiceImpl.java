package kr.or.ddit.finalProject.service.calendar;

import java.util.List;
import java.util.Map;
import kr.or.ddit.finalProject.dto.calendar.CalendarEventDto;
import kr.or.ddit.finalProject.exception.ErrorCode;
import kr.or.ddit.finalProject.exception.FinalProjectException;
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
        CalendarEventDto dto = calendarEventMapper.selectCalendarEvent(eventSn);
        if (dto == null) {
            throw new FinalProjectException(ErrorCode.CALENDAR_EVENT_NOT_FOUND);
        }
        return dto;
    }

    @Override
    @Transactional
    public int insertCalendarEvent(CalendarEventDto dto) {
        return calendarEventMapper.insertCalendarEvent(dto);
    }

    @Override
    @Transactional
    public void updateCalendarEvent(CalendarEventDto dto) {
        int updated = calendarEventMapper.updateCalendarEvent(dto);
        if (updated == 0) {
            throw new FinalProjectException(ErrorCode.CALENDAR_EVENT_NOT_FOUND);
        }
    }

    @Override
    @Transactional
    public void deleteCalendarEvent(Long eventSn) {
        int deleted = calendarEventMapper.deleteCalendarEvent(eventSn);
        if (deleted == 0) {
            throw new FinalProjectException(ErrorCode.CALENDAR_EVENT_NOT_FOUND);
        }
    }

    @Override
    public List<CalendarEventDto> getCalendarEventList() {
        return calendarEventMapper.selectAllCalendarEventList();
    }

}
