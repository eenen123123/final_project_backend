package kr.or.ddit.finalProject.mapper.calendar;

import java.util.List;
import java.util.Map;
import kr.or.ddit.finalProject.dto.calendar.CalendarScheduleDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CalendarScheduleMapper {

    // 월별 개인 일정 조회 (userId + year + month)
    List<CalendarScheduleDto> selectCalendarScheduleList(Map<String, Object> param);

    // 단건 조회
    CalendarScheduleDto selectCalendarSchedule(Long scheduleSn);

    // 등록
    int insertCalendarSchedule(CalendarScheduleDto dto);

    // 수정
    int updateCalendarSchedule(CalendarScheduleDto dto);

    // 삭제 (본인 일정만 삭제되도록 userId도 같이 받음)
    int deleteCalendarSchedule(@Param("scheduleSn") Long scheduleSn,
            @Param("userId") String userId);

}
