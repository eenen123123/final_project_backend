package kr.or.ddit.controller.course;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import kr.or.ddit.finalProject.dto.course.AdminCourseSearchCondition;
import kr.or.ddit.finalProject.dto.course.CourseDto;
import kr.or.ddit.finalProject.dto.course.SubjectDto;
import kr.or.ddit.finalProject.paging.PaginationInfo;
import kr.or.ddit.finalProject.service.course.CourseService;
import kr.or.ddit.finalProject.service.curriculum.CurriculumService;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/course")
public class AdminCourseController {

    private final CourseService courseService;
    private final CurriculumService curriculumService;

    @GetMapping("/insert")
    public String insertForm(Authentication authentication, Model model) {
        String userId = authentication.getName();
        model.addAttribute("curriculumList", curriculumService.retrieveList(userId));
        model.addAttribute("subjClList", courseService.retrieveSubjectClassificationList());
        return "admin:/course/insert-course";
    }

    @ResponseBody
    @GetMapping("/subjects")
    public List<SubjectDto> subjectsByClassification(@RequestParam Long subjClId) {
        return courseService.retrieveSubjectsBySubjClId(subjClId);
    }

    @PostMapping("/insert")
    public String insert(@ModelAttribute CourseDto courseDto,
                         Authentication authentication,
                         RedirectAttributes redirectAttributes) {
        String userId = authentication.getName();
        courseDto.setInstrUserId(userId);
        courseDto.setRgtrId(userId);
        courseDto.setLastMdfrId(userId);
        boolean created = courseService.createCourse(courseDto);
        if (!created) {
            redirectAttributes.addFlashAttribute("errorMsg", "강좌 등록에 실패했습니다.");
            return "redirect:/admin/course/insert";
        }
        redirectAttributes.addFlashAttribute("successMsg", "강좌가 등록되었습니다.");
        return "redirect:/admin/course/list";
    }

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
