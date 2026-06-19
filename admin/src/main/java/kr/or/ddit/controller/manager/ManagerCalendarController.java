package kr.or.ddit.controller.manager;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.or.ddit.finalProject.dto.calendar.ManagerCalendarEventDto;
import kr.or.ddit.service.ConsultationService;
import kr.or.ddit.service.RetentionService;
import lombok.RequiredArgsConstructor;

/**
 * 매니저 통합 캘린더 API
 *  · 학부모 상담(CONSULTATION) + 퇴원 방어(RETENTION_PROCESS) 일정을 한 번에 반환
 *  · 학부모 상담 관리 / 퇴원 방어 두 화면이 동일한 캘린더로 공유한다.
 *
 * - GET /admin/manager/calendar/events?start=&end=
 */
@Controller
@RequestMapping("/admin/manager/calendar")
@RequiredArgsConstructor
public class ManagerCalendarController {

    private final ConsultationService consultationService;
    private final RetentionService retentionService;

    @GetMapping("/events")
    @ResponseBody
    public ResponseEntity<List<ManagerCalendarEventDto>> events(
            @RequestParam String start, @RequestParam String end) {

        List<ManagerCalendarEventDto> events = new ArrayList<>();

        consultationService.getForCalendar(start, end).forEach(c ->
                events.add(new ManagerCalendarEventDto(
                        "상담", c.getStudentNm(), c.getCnslDt(), c.getCnslSn(), c.getCnslStatNm())));

        retentionService.getProcessCalendar(start, end).forEach(p ->
                events.add(new ManagerCalendarEventDto(
                        "방어", p.getStudentNm(), p.getRtnpDt(), p.getRtnpSn(), p.getRtnpRsltNm())));

        return ResponseEntity.ok(events);
    }
}
