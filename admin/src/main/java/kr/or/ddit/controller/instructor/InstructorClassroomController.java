package kr.or.ddit.controller.instructor;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import kr.or.ddit.finalProject.dto.classroom.ClassroomDetailResponse;
import kr.or.ddit.finalProject.dto.classroom.ClassroomListResponse;
import kr.or.ddit.finalProject.service.classroom.ClassroomService;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/instructor/classroom")
@RequiredArgsConstructor
public class InstructorClassroomController {

    private final ClassroomService classroomService;

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

        //임시로 넣어두는 데이터들
        model.addAttribute("calendarPadding", List.of());
        model.addAttribute("calendarDays", List.of());
        model.addAttribute("weeklyData", List.of());
        model.addAttribute("achievements", List.of());
        model.addAttribute("weeklyCompareText", "");
        model.addAttribute("noticeCount", 0);
        model.addAttribute("assignmentCount", 0);
        model.addAttribute("calendarYear", 2026);
        model.addAttribute("calendarMonth", 6);
        // todayQuestion, recentNotice는 안 담아도 됨

        return "instructor/classroom-home";
    }

}
