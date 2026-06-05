package kr.or.ddit.controller.instructor;

import java.time.LocalDate;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import kr.or.ddit.finalProject.dto.classroom.ClassroomDetailResponse;
import kr.or.ddit.finalProject.dto.classroom.ClassroomListResponse;
import kr.or.ddit.finalProject.service.classroom.ClassroomHomeService;
import kr.or.ddit.finalProject.service.classroom.ClassroomService;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/instructor/classroom")
@RequiredArgsConstructor
public class InstructorClassroomController {

    private final ClassroomService classroomService;
    private final ClassroomHomeService classroomHomeService;

    @GetMapping("/list")
    public String classroomList(Model model, Authentication authentication) {
        String instrUserId = authentication.getName();
        List<ClassroomListResponse> classroomList = classroomService.retrieveClassroomList(instrUserId);
        model.addAttribute("classroomList", classroomList);
        return "admin:/instructor/classroomList";
    }

    @GetMapping("/detail/{classSn}")
    public String classroomDetail(@PathVariable Long classSn, Model model) {
        ClassroomDetailResponse classroom = classroomService.retrieveClassroomDetail(classSn);
        model.addAttribute("classroom", classroom);

        LocalDate now = LocalDate.now();
        int year  = now.getYear();
        int month = now.getMonthValue();

        model.addAttribute("calendarYear",    year);
        model.addAttribute("calendarMonth",   month);
        model.addAttribute("calendarPadding", classroomHomeService.retrieveCalendarPadding(year, month));
        model.addAttribute("calendarDays",    classroomHomeService.retrieveCalendarDays(classSn, year, month));
        model.addAttribute("weeklyData",      classroomHomeService.retrieveWeeklyData(classSn));
        model.addAttribute("weeklyCompareText", classroomHomeService.retrieveWeeklyCompareText(classSn));
        model.addAttribute("achievements",    classroomHomeService.retrieveAchievements(classSn));
        model.addAttribute("assignmentCount", classroomHomeService.retrieveUpcomingAssignmentCount(classSn));
        model.addAttribute("todayQuestion",   classroomHomeService.retrieveTodayQuestion(classSn));
        model.addAttribute("noticeCount",     0);  // 클래스별 공지 테이블 미구현

        return "instructor/classroom-home";
    }
}
