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
import kr.or.ddit.finalProject.dto.subject.SubjectDto;
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

    /**
     * 강좌 등록 폼 페이지 반환. 현재 로그인한 강사의 커리큘럼 목록과 과목 분류 목록을 모델에 담아 전달한다.
     */
    @GetMapping("/insert")
    public String insertForm(Authentication authentication, Model model) {
        String userId = authentication.getName();
        model.addAttribute("curriculumList", curriculumService.retrieveList(userId));
        model.addAttribute("subjClList", courseService.retrieveSubjectClassificationList());
        return "admin:/course/insert-course";
    }

    /**
     * 과목 분류 ID에 해당하는 과목 목록을 JSON으로 반환. 강좌 등록 폼에서 과목 분류 선택 시 Ajax로 호출된다.
     */
    @ResponseBody
    @GetMapping("/subjects")
    public List<SubjectDto> subjectsByClassification(@RequestParam Long subjClId) {
        return courseService.retrieveSubjectsBySubjClId(subjClId);
    }

    /**
     * 강좌 등록 처리. 등록자/수정자 ID를 현재 로그인 사용자로 설정한 뒤 강좌를 생성한다. 실패 시 등록 폼으로, 성공 시 목록
     * 페이지로 리다이렉트한다.
     */
    @PostMapping("/insert")
    public String insert(@ModelAttribute CourseDto courseDto,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        String userId = authentication.getName();
        String courseNm = courseDto.getCourseNm();
        if (courseNm == null || courseNm.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMsg", "강좌명은 필수 입력 항목입니다.");
            return "redirect:/admin/course/insert";
        }
        if (courseNm.length() > 200) {
            redirectAttributes.addFlashAttribute("errorMsg", "강좌명은 200자 이내로 입력해 주세요.");
            return "redirect:/admin/course/insert";
        }
        if (courseDto.getCoursePrice() != null && courseDto.getCoursePrice() < 0) {
            redirectAttributes.addFlashAttribute("errorMsg", "수강료는 0원 이상이어야 합니다.");
            return "redirect:/admin/course/insert";
        }
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

    /**
     * 강좌 목록 페이지 반환. 키워드·공개여부 조건과 페이지 번호를 받아 페이징 처리된 강좌 목록을 모델에 담는다.
     */
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

    /**
     * 강좌 상세 페이지 반환. courseSn으로 강좌 정보를 조회해 모델에 담는다.
     */
    @GetMapping("/detail")
    public String courseDetail(@RequestParam Long courseSn, Model model) {
        CourseDto course = courseService.retrieveCourseAdminDetail(courseSn);
        model.addAttribute("course", course);
        return "admin:/course/detail-course";
    }

}
