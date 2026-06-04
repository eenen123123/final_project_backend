package kr.or.ddit.finalProject.service.calendar;

import java.util.List;
import java.util.Map;
import kr.or.ddit.finalProject.dto.calendar.CalendarScheduleDto;
import kr.or.ddit.finalProject.mapper.calendar.CalendarScheduleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalendarScheduleServiceImpl implements CalendarScheduleService {

    private final CalendarScheduleMapper calendarScheduleMapper;

    @Override
    public List<CalendarScheduleDto> getCalendarScheduleList(Map<String, Object> param) {
        return calendarScheduleMapper.selectCalendarScheduleList(param);
    }

    @Override
    public CalendarScheduleDto getCalendarSchedule(Long scheduleSn) {
        return calendarScheduleMapper.selectCalendarSchedule(scheduleSn);
    }

    @Override
    @Transactional
    public int insertCalendarSchedule(CalendarScheduleDto dto) {
        return calendarScheduleMapper.insertCalendarSchedule(dto);
    }

    @Override
    @Transactional
    public int updateCalendarSchedule(CalendarScheduleDto dto) {
        return calendarScheduleMapper.updateCalendarSchedule(dto);
    }

    @Override
    @Transactional
    public int deleteCalendarSchedule(Long scheduleSn, String userId) {
        return calendarScheduleMapper.deleteCalendarSchedule(scheduleSn, userId);
    }

}
