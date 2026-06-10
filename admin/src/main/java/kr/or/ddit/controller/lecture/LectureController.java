package kr.or.ddit.controller.lecture;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import kr.or.ddit.finalProject.exception.ErrorCode;
import kr.or.ddit.finalProject.exception.FinalProjectException;
import kr.or.ddit.finalProject.service.course.CourseService;
import kr.or.ddit.finalProject.service.lecture.LectureService;


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

}
