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
    public String insertLecture(@RequestParam Long courseId, Model model) {
        log.info("강의 등록 페이지 요청 - courseId: {}", courseId);
        var course = courseService.retrieveCourseBySn(courseId);
        if (course == null) {
            throw new FinalProjectException(ErrorCode.COURSE_NOT_FOUND);
        }
        model.addAttribute("course", course);

        var lectures = lectureService.retrieveLectureByCourseSn(courseId);
        model.addAttribute("lectures", lectures);



        return "admin:/lecture/insert-lecture";
    }

    @PostMapping("/insert")
    public String insertLecturePost(@ModelAttribute LectureDto lectureDto,
            Authentication authentication, RedirectAttributes redirectAttributes) {
        log.info("강의 등록 요청 - lectureDto: {}", lectureDto);
        String userId = authentication.getName();
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

        log.info("lecture : {}", lecture);
        return "admin:/lecture/view-lecture";
    }



}
