package kr.or.ddit.finalProject.mapper.calendar;

import java.util.List;
import java.util.Map;
import kr.or.ddit.finalProject.dto.calendar.CalendarEventDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CalendarEventMapper {

    // 월별 이벤트 조회 (year, month 파라미터)
    List<CalendarEventDto> selectCalendarEventList(Map<String, Object> param);

    // 이벤트 단건 조회
    CalendarEventDto selectCalendarEvent(Long eventSn);

    // 이벤트 등록
    int insertCalendarEvent(CalendarEventDto dto);

    // 이벤트 수정
    int updateCalendarEvent(CalendarEventDto dto);

    // 이벤트 삭제
    int deleteCalendarEvent(Long eventSn);

    // 전체 조회 (관리자 페이지용)
    List<CalendarEventDto> selectAllCalendarEventList();

    // 월별 조회 (나중에 REST API용)
    // List<CalendarEventDto> selectCalendarEventList(Map<String, Object> param);

    // 유형 + 년도로 삭제 (배치용)
    void deleteCalendarEventByTypeAndYear(@Param("eventType") String eventType,
            @Param("year") int year);

}
