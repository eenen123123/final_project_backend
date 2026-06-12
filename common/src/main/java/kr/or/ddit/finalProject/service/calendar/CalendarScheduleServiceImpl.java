package kr.or.ddit.finalProject.service.calendar;

import java.util.List;
import java.util.Map;
import kr.or.ddit.finalProject.dto.calendar.CalendarScheduleDto;
import kr.or.ddit.finalProject.exception.ErrorCode;
import kr.or.ddit.finalProject.exception.FinalProjectException;
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
        CalendarScheduleDto dto = calendarScheduleMapper.selectCalendarSchedule(scheduleSn);
        if (dto == null) {
            throw new FinalProjectException(ErrorCode.CALENDAR_SCHEDULE_NOT_FOUND);
        }
        return dto;
    }

    @Override
    @Transactional
    public int insertCalendarSchedule(CalendarScheduleDto dto) {
        return calendarScheduleMapper.insertCalendarSchedule(dto);
    }

    @Override
    @Transactional
    public void updateCalendarSchedule(CalendarScheduleDto dto) {
        int updated = calendarScheduleMapper.updateCalendarSchedule(dto);
        if (updated == 0) {
            throw new FinalProjectException(ErrorCode.CALENDAR_SCHEDULE_NOT_FOUND);
        }
    }

    @Override
    @Transactional
    public void deleteCalendarSchedule(Long scheduleSn, String userId) {
        int deleted = calendarScheduleMapper.deleteCalendarSchedule(scheduleSn, userId);
        if (deleted == 0) {
            throw new FinalProjectException(ErrorCode.CALENDAR_SCHEDULE_NOT_FOUND);
        }
    }

}
