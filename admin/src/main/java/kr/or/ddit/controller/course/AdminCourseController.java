package kr.or.ddit.controller.course;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import kr.or.ddit.finalProject.dto.course.CourseDto;
import kr.or.ddit.finalProject.dto.course.AdminCourseSearchCondition;
import kr.or.ddit.finalProject.paging.PaginationInfo;
import kr.or.ddit.finalProject.service.course.CourseService;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/course")
public class AdminCourseController {

    private final CourseService courseService;

    @GetMapping("/list")
    public String listCourses(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String opnnYn,
            Model model) {
        PaginationInfo<AdminCourseSearchCondition> paginationInfo = new PaginationInfo<>(10, 5, page);
        AdminCourseSearchCondition condition = AdminCourseSearchCondition.builder()
                .keyword(keyword).opnnYn(opnnYn).build();
        paginationInfo.setDetailCondition(condition);
        int totalCount = courseService.retrieveCourseListCount(paginationInfo);
        List<CourseDto> courseList = courseService.retrieveCourseList(paginationInfo);
        model.addAttribute("courseList", courseList);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("paginationInfo", paginationInfo);
        return "admin:/course/list-courses";
    }

    @GetMapping("/detail")
    public String courseDetail(@RequestParam Long courseSn, Model model) {
        CourseDto course = courseService.retrieveCourseAdminDetail(courseSn);
        model.addAttribute("course", course);
        return "admin:/course/detail-course";
    }

}
