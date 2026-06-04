package kr.or.ddit.controller.calendar;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kr.or.ddit.finalProject.dto.calendar.CalendarEventDto;
import kr.or.ddit.finalProject.service.calendar.CalendarEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/calendar/event")
@RequiredArgsConstructor
public class UserCalendarEventController {

    private final CalendarEventService calendarEventService;

    // 월별 공통 이벤트 조회 (공휴일 + 혜택/이벤트 + 학사일정)
    // GET /api/calendar/event?year=2026&month=6
    @GetMapping
    public ResponseEntity<List<CalendarEventDto>> getCalendarEventList(@RequestParam int year,
            @RequestParam int month) {
        Map<String, Object> param = new HashMap<>();
        param.put("year", year);
        param.put("month", month);
        return ResponseEntity.ok(calendarEventService.getCalendarEventList(param));
    }

}
