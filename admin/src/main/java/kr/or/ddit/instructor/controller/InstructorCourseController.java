package kr.or.ddit.instructor.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import kr.or.ddit.dto.instructor.CourseDto;
import kr.or.ddit.instructor.service.InstructorCourseService;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/instructor/course")
@RequiredArgsConstructor
public class InstructorCourseController {

    private final InstructorCourseService courseService;

    @GetMapping
    public String courseMainPage(Model model, Authentication authentication) {
        String instrUserId = authentication.getName();

        List<CourseDto> courseList = courseService.retrieveCourseList(instrUserId);
        model.addAttribute("courseList", courseList);

        return "admin:/instructor/course";
    }
}
