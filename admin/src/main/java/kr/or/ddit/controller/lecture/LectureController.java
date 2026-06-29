package kr.or.ddit.controller.lecture;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import kr.or.ddit.finalProject.dto.lecture.LectureDto;
import kr.or.ddit.finalProject.exception.ErrorCode;
import kr.or.ddit.finalProject.exception.FinalProjectException;
import kr.or.ddit.finalProject.service.course.CourseService;
import kr.or.ddit.finalProject.service.lecture.LectureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Controller
@RequestMapping("/admin/lectures")
@RequiredArgsConstructor
public class LectureController {
    private final LectureService lectureService;
    private final CourseService courseService;

    @GetMapping("/insert")
    public String insertLecture(@RequestParam Long courseId, Authentication authentication,
            RedirectAttributes redirectAttributes, Model model) {
        var course = courseService.retrieveCourseBySn(courseId);
        if (course == null) {
            throw new FinalProjectException(ErrorCode.COURSE_NOT_FOUND);
        }
        if (!authentication.getName().equals(course.getInstrUserId())) {
            redirectAttributes.addFlashAttribute("errorMsg", "본인이 담당하는 강좌에만 강의를 등록할 수 있습니다.");
            return "redirect:/admin/lectures/list?courseId=" + courseId;
        }
        model.addAttribute("course", course);
        model.addAttribute("lectures", lectureService.retrieveLectureByCourseSn(courseId));
        return "admin:/lecture/insert-lecture";
    }

    @PostMapping("/insert")
    public String insertLecturePost(@ModelAttribute LectureDto lectureDto,
            Authentication authentication, RedirectAttributes redirectAttributes) {
        String userId = authentication.getName();
        var course = courseService.retrieveCourseBySn(lectureDto.getCourseSn());
        if (course == null) {
            throw new FinalProjectException(ErrorCode.COURSE_NOT_FOUND);
        }
        if (!userId.equals(course.getInstrUserId())) {
            redirectAttributes.addFlashAttribute("errorMsg", "본인이 담당하는 강좌에만 강의를 등록할 수 있습니다.");
            return "redirect:/admin/lectures/list?courseId=" + lectureDto.getCourseSn();
        }
        lectureDto.setRgtrId(userId);
        lectureDto.setLastMdfrId(userId);
        boolean created = lectureService.createLecture(lectureDto);
        if (!created) {
            redirectAttributes.addFlashAttribute("errorMsg", "강의 등록에 실패했습니다.");
            return "redirect:/admin/lectures/insert?courseId=" + lectureDto.getCourseSn();
        }
        redirectAttributes.addFlashAttribute("successMsg", "강의가 등록되었습니다.");
        return "redirect:/admin/lectures/insert?courseId=" + lectureDto.getCourseSn();
    }

    @GetMapping("/list")
    public String listLectures(@RequestParam Long courseId, Model model) {
        var course = courseService.retrieveCourseBySn(courseId);
        if (course == null) {
            throw new FinalProjectException(ErrorCode.COURSE_NOT_FOUND);
        }
        model.addAttribute("course", course);
        model.addAttribute("lectures", lectureService.retrieveLectureByCourseSn(courseId));
        return "admin:/lecture/list-lectures";
    }

    @GetMapping("/view")
    public String viewLecture(@RequestParam Long lectureId, Model model) {
        var lecture = lectureService.retrieveLectureBySn(lectureId);
        if (lecture == null) {
            throw new FinalProjectException(ErrorCode.LECTURE_NOT_FOUND);
        }
        var course = courseService.retrieveCourseBySn(lecture.getCourseSn());
        var lectures = lectureService.retrieveLectureByCourseSn(lecture.getCourseSn());
        model.addAttribute("lecture", lecture);
        model.addAttribute("course", course);
        model.addAttribute("lectures", lectures);
        return "admin:/lecture/view-lecture";
    }

    @GetMapping("/edit")
    public String editLecture(@RequestParam Long lectureId, Authentication authentication,
            RedirectAttributes redirectAttributes, Model model) {
        var lecture = lectureService.retrieveLectureBySn(lectureId);
        if (lecture == null) {
            throw new FinalProjectException(ErrorCode.LECTURE_NOT_FOUND);
        }
        var course = courseService.retrieveCourseBySn(lecture.getCourseSn());
        if (!authentication.getName().equals(course.getInstrUserId())) {
            redirectAttributes.addFlashAttribute("errorMsg", "본인이 담당하는 강좌의 강의만 수정할 수 있습니다.");
            return "redirect:/admin/lectures/view?lectureId=" + lectureId;
        }
        model.addAttribute("lecture", lecture);
        model.addAttribute("course", course);
        model.addAttribute("lectures", lectureService.retrieveLectureByCourseSn(lecture.getCourseSn()));
        return "admin:/lecture/insert-lecture";
    }

    @PostMapping("/edit")
    public String editLecturePost(@ModelAttribute LectureDto lectureDto,
            Authentication authentication, RedirectAttributes redirectAttributes) {
        String userId = authentication.getName();
        var lecture = lectureService.retrieveLectureBySn(lectureDto.getLectureSn());
        if (lecture == null) {
            throw new FinalProjectException(ErrorCode.LECTURE_NOT_FOUND);
        }
        var course = courseService.retrieveCourseBySn(lecture.getCourseSn());
        if (!userId.equals(course.getInstrUserId())) {
            redirectAttributes.addFlashAttribute("errorMsg", "본인이 담당하는 강좌의 강의만 수정할 수 있습니다.");
            return "redirect:/admin/lectures/view?lectureId=" + lectureDto.getLectureSn();
        }
        try {
            lectureService.modifyLecture(lectureDto, userId);
        } catch (IllegalArgumentException | SecurityException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/admin/lectures/edit?lectureId=" + lectureDto.getLectureSn();
        }
        redirectAttributes.addFlashAttribute("successMsg", "강의가 수정되었습니다.");
        return "redirect:/admin/lectures/view?lectureId=" + lectureDto.getLectureSn();
    }

    @PostMapping("/delete")
    public String deleteLecture(@RequestParam Long lectureId,
            Authentication authentication, RedirectAttributes redirectAttributes) {
        var lecture = lectureService.retrieveLectureBySn(lectureId);
        if (lecture == null) {
            throw new FinalProjectException(ErrorCode.LECTURE_NOT_FOUND);
        }
        Long courseSn = lecture.getCourseSn();
        var course = courseService.retrieveCourseBySn(courseSn);
        if (!authentication.getName().equals(course.getInstrUserId())) {
            redirectAttributes.addFlashAttribute("errorMsg", "본인이 담당하는 강좌의 강의만 삭제할 수 있습니다.");
            return "redirect:/admin/lectures/view?lectureId=" + lectureId;
        }
        try {
            lectureService.removeLecture(lectureId, authentication.getName());
        } catch (IllegalArgumentException | SecurityException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/admin/lectures/view?lectureId=" + lectureId;
        }
        redirectAttributes.addFlashAttribute("successMsg", "강의가 삭제되었습니다.");
        return "redirect:/admin/lectures/list?courseId=" + courseSn;
    }

}
