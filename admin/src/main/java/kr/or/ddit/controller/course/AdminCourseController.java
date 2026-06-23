package kr.or.ddit.controller.course;

import java.util.List;

import org.springframework.dao.DuplicateKeyException;
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
    public String insertForm(Model model, Authentication authentication) {
        model.addAttribute("curriculumList", curriculumService.retrieveList(authentication.getName()));
        model.addAttribute("subjClList", courseService.retrieveSubjectClassificationList());
        return "admin:/course/form-course";
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
        String errorMsg = validateCourseForm(courseDto);
        if (errorMsg != null) {
            redirectAttributes.addFlashAttribute("errorMsg", errorMsg);
            redirectAttributes.addFlashAttribute("course", courseDto);
            return "redirect:/admin/course/insert";
        }
        String userId = authentication.getName();
        boolean created;
        try {
            created = courseService.createCourse(courseDto, userId);
        } catch (DuplicateKeyException e) {
            redirectAttributes.addFlashAttribute("errorMsg", "동시 등록으로 인한 충돌이 발생했습니다. 다시 시도해 주세요.");
            return "redirect:/admin/course/insert";
        }
        if (!created) {
            redirectAttributes.addFlashAttribute("errorMsg", "강좌 등록에 실패했습니다.");
            return "redirect:/admin/course/insert";
        }
        redirectAttributes.addFlashAttribute("successMsg", "강좌가 등록되었습니다.");
        return "redirect:/admin/course/list";
    }

    /**
     * 강좌 수정 폼 페이지 반환. courseSn으로 강좌를 조회해 모델에 담는다.
     */
    @GetMapping("/edit")
    public String editForm(@RequestParam Long courseSn,
            Authentication authentication,
            RedirectAttributes redirectAttributes,
            Model model) {
        CourseDto course = courseService.retrieveCourseAdminDetail(courseSn);
        if (course == null) {
            redirectAttributes.addFlashAttribute("errorMsg", "존재하지 않는 강좌입니다.");
            return "redirect:/admin/course/list";
        }
        if (!authentication.getName().equals(course.getInstrUserId())) {
            redirectAttributes.addFlashAttribute("errorMsg", "본인이 등록한 강좌만 수정할 수 있습니다.");
            return "redirect:/admin/course/list";
        }
        if (!model.containsAttribute("course")) {
            model.addAttribute("course", course);
        }
        model.addAttribute("curriculumList", curriculumService.retrieveList(authentication.getName()));
        model.addAttribute("subjClList", courseService.retrieveSubjectClassificationList());
        return "admin:/course/form-course";
    }

    /**
     * 강좌 수정 처리. 수정 후 강좌 상세 페이지로 리다이렉트한다.
     */
    @PostMapping("/edit")
    public String edit(@ModelAttribute CourseDto courseDto,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        if (courseDto.getCourseSn() == null) {
            redirectAttributes.addFlashAttribute("errorMsg", "잘못된 요청입니다.");
            return "redirect:/admin/course/list";
        }
        String errorMsg = validateCourseForm(courseDto);
        if (errorMsg != null) {
            redirectAttributes.addFlashAttribute("errorMsg", errorMsg);
            redirectAttributes.addFlashAttribute("course", courseDto);
            return "redirect:/admin/course/edit?courseSn=" + courseDto.getCourseSn();
        }
        String userId = authentication.getName();
        try {
            courseService.modifyCourse(courseDto, userId);
        } catch (IllegalArgumentException | SecurityException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/admin/course/edit?courseSn=" + courseDto.getCourseSn();
        } catch (DuplicateKeyException e) {
            redirectAttributes.addFlashAttribute("errorMsg", "동시 수정으로 인한 충돌이 발생했습니다. 다시 시도해 주세요.");
            return "redirect:/admin/course/edit?courseSn=" + courseDto.getCourseSn();
        }
        redirectAttributes.addFlashAttribute("successMsg", "강좌가 수정되었습니다.");
        return "redirect:/admin/course/detail?courseSn=" + courseDto.getCourseSn();
    }

    /**
     * 강좌 삭제 처리. 강의가 존재하면 상세 페이지로, 성공 시 목록 페이지로 리다이렉트한다.
     */
    @PostMapping("/delete")
    public String delete(@RequestParam Long courseSn,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            courseService.removeCourse(courseSn, authentication.getName());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/admin/course/detail?courseSn=" + courseSn;
        }
        redirectAttributes.addFlashAttribute("successMsg", "강좌가 삭제되었습니다.");
        return "redirect:/admin/course/list";
    }

    /**
     * 강좌명 필수/길이, 수강료 최솟값을 검사한다. 문제가 있으면 오류 메시지를, 없으면 null을 반환한다.
     */
    private String validateCourseForm(CourseDto courseDto) {
        String courseNm = courseDto.getCourseNm();
        if (courseNm == null || courseNm.isBlank()) return "강좌명은 필수 입력 항목입니다.";
        if (courseNm.length() > 200) return "강좌명은 200자 이내로 입력해 주세요.";
        if (courseDto.getCoursePrice() != null && courseDto.getCoursePrice() < 0) return "수강료는 0원 이상이어야 합니다.";
        return null;
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
    public String courseDetail(@RequestParam Long courseSn,
            RedirectAttributes redirectAttributes,
            Model model) {
        CourseDto course = courseService.retrieveCourseAdminDetail(courseSn);
        if (course == null) {
            redirectAttributes.addFlashAttribute("errorMsg", "존재하지 않는 강좌입니다.");
            return "redirect:/admin/course/list";
        }
        model.addAttribute("course", course);
        return "admin:/course/detail-course";
    }

}
