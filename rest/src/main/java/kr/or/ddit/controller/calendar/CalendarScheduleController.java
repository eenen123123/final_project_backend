package kr.or.ddit.controller.calendar;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kr.or.ddit.finalProject.dto.calendar.CalendarScheduleDto;
import kr.or.ddit.finalProject.service.calendar.CalendarScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/calendar/schedule")
@RequiredArgsConstructor
public class CalendarScheduleController {

    private final CalendarScheduleService calendarScheduleService;

    // 월별 개인 일정 조회
    // GET /api/calendar/schedule?year=2026&month=6
    @GetMapping
    public ResponseEntity<List<CalendarScheduleDto>> getCalendarScheduleList(@RequestParam int year,
            @RequestParam int month, Authentication authentication) {
        Map<String, Object> param = new HashMap<>();
        param.put("userId", authentication.getName());
        param.put("year", year);
        param.put("month", month);
        return ResponseEntity.ok(calendarScheduleService.getCalendarScheduleList(param));
    }

    // 단건 조회
    // GET /api/calendar/schedule/{scheduleSn}
    @GetMapping("/{scheduleSn}")
    public ResponseEntity<CalendarScheduleDto> getCalendarSchedule(@PathVariable Long scheduleSn) {
        return ResponseEntity.ok(calendarScheduleService.getCalendarSchedule(scheduleSn));
    }

    // 등록
    // POST /api/calendar/schedule
    @PostMapping
    public ResponseEntity<Integer> insertCalendarSchedule(@RequestBody CalendarScheduleDto dto,
            Authentication authentication) {
        dto.setUserId(authentication.getName());
        return ResponseEntity.ok(calendarScheduleService.insertCalendarSchedule(dto));
    }

    // 수정
    // PUT /api/calendar/schedule/{scheduleSn}
    @PutMapping("/{scheduleSn}")
    public ResponseEntity<Void> updateCalendarSchedule(@PathVariable Long scheduleSn,
            @RequestBody CalendarScheduleDto dto, Authentication authentication) {
        dto.setScheduleSn(scheduleSn);
        dto.setUserId(authentication.getName());
        calendarScheduleService.updateCalendarSchedule(dto);
        return ResponseEntity.noContent().build();
    }

    // 삭제
    // DELETE /api/calendar/schedule/{scheduleSn}
    @DeleteMapping("/{scheduleSn}")
    public ResponseEntity<Void> deleteCalendarSchedule(@PathVariable Long scheduleSn,
            Authentication authentication) {
        calendarScheduleService.deleteCalendarSchedule(scheduleSn, authentication.getName());
        return ResponseEntity.noContent().build();
    }

}
