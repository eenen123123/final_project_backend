package kr.or.ddit.controller.calendar;

import kr.or.ddit.finalProject.batch.HolidayBatchService;
import kr.or.ddit.finalProject.dto.calendar.CalendarEventDto;
import kr.or.ddit.finalProject.service.calendar.CalendarEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/admin/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarEventService calendarEventService;
    private final HolidayBatchService holidayBatchService;

    // ── 캘린더 관리 메인 페이지 ───────────────────────────
    @GetMapping
    public String calendarMain(Model model) {
        model.addAttribute("pageTitle", "캘린더 관리 | HERMES");
        model.addAttribute("eventList", calendarEventService.getCalendarEventList()); // 파라미터 없이
        return "admin:/calendar/calendar_main";
    }

    // ── 등록 폼 ──────────────────────────────────────────
    @GetMapping("/write")
    public String writeForm(Model model, Authentication authentication) {
        model.addAttribute("pageTitle", "일정 등록 | HERMES");
        model.addAttribute("currentUser", authentication.getName());
        return "admin:/calendar/calendar_write";
    }

    // ── 등록 처리 ─────────────────────────────────────────
    @PostMapping("/write")
    public String write(CalendarEventDto dto, Authentication authentication) {
        dto.setRegUserId(authentication.getName());
        calendarEventService.insertCalendarEvent(dto);
        return "redirect:/admin/calendar";
    }

    // ── 수정 폼 ──────────────────────────────────────────
    @GetMapping("/edit/{eventSn}")
    public String editForm(@PathVariable Long eventSn, Model model) {
        model.addAttribute("pageTitle", "일정 수정 | HERMES");
        model.addAttribute("event", calendarEventService.getCalendarEvent(eventSn));
        return "admin:/calendar/calendar_edit";
    }

    // ── 수정 처리 ─────────────────────────────────────────
    @PostMapping("/edit/{eventSn}")
    public String edit(@PathVariable Long eventSn, CalendarEventDto dto,
            Authentication authentication) {
        dto.setEventSn(eventSn);
        dto.setMdfcnUserId(authentication.getName());
        calendarEventService.updateCalendarEvent(dto);
        return "redirect:/admin/calendar";
    }

    // ── 삭제 처리 ─────────────────────────────────────────
    @PostMapping("/delete/{eventSn}")
    public String delete(@PathVariable Long eventSn) {
        calendarEventService.deleteCalendarEvent(eventSn);
        return "redirect:/admin/calendar";
    }

    // 배치 수동 실행 (테스트용)
    @GetMapping("/batch/holiday/{year}")
    @ResponseBody
    public String runHolidayBatch(@PathVariable int year) {
        holidayBatchService.fetchAndSaveHolidays(year);
        return year + "년 공휴일 배치 완료";
    }


}
