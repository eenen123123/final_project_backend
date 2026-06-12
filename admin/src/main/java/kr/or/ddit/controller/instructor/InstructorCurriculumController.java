package kr.or.ddit.controller.instructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.or.ddit.finalProject.dto.course.CourseDto;
import kr.or.ddit.finalProject.dto.curriculum.CourseSaveRequest;
import kr.or.ddit.finalProject.dto.curriculum.CurriculumDto;
import kr.or.ddit.finalProject.dto.curriculum.CurriculumSaveRequest;
import kr.or.ddit.finalProject.dto.curriculum.LectureSaveRequest;
import kr.or.ddit.finalProject.dto.lecture.LectureDto;
import kr.or.ddit.finalProject.service.course.CourseService;
import kr.or.ddit.finalProject.service.curriculum.CurriculumService;
import kr.or.ddit.finalProject.service.lecture.LectureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/instructor/curriculum")
@RequiredArgsConstructor
public class InstructorCurriculumController {

    private final CurriculumService curriculumService;
    private final CourseService courseService;
    private final LectureService lectureService;

    // =====================================================================
    // 커리큘럼 CRUD
    // =====================================================================
    @GetMapping
    public String curriculumMainPage(Model model, Authentication authentication) {
        String loginInstructorId = authentication.getName();

        List<CurriculumDto> curriculumList = curriculumService.retrieveList(loginInstructorId);
        model.addAttribute("curriculumList", curriculumList);
        return "admin:/instructor/curriculum";
    }

    @PostMapping("/save")
    @ResponseBody
    public ResponseEntity<String> saveCurriculum(@RequestBody CurriculumSaveRequest request,
            Authentication authentication) {
        String loginInstructorId = authentication.getName();

        CurriculumDto curriculumDto = new CurriculumDto();
        curriculumDto.setTitle(request.getTitle());
        curriculumDto.setInstructorId(loginInstructorId);
        curriculumDto.setRgtrId(loginInstructorId);
        curriculumDto.setLastMdfrId(loginInstructorId);

        boolean created = curriculumService.createCurriculum(curriculumDto);
        if (!created) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("FAIL");
        }
        return ResponseEntity.ok("SUCCESS");
    }

    @PutMapping("/modify/{curriculumId}")
    @ResponseBody
    public ResponseEntity<String> modifyCurriculum(@PathVariable Long curriculumId,
            @RequestBody CurriculumSaveRequest request,
            Authentication authentication) {
        String loginInstructorId = authentication.getName();

        CurriculumDto curriculumDto = new CurriculumDto();
        curriculumDto.setCurriculumId(curriculumId);
        curriculumDto.setTitle(request.getTitle());
        curriculumService.modifyCurriculum(curriculumDto, loginInstructorId);
        return ResponseEntity.ok("SUCCESS");
    }

    @DeleteMapping("/delete/{curriculumId}")
    @ResponseBody
    public ResponseEntity<String> deleteCurriculum(@PathVariable Long curriculumId,
            Authentication authentication) {
        String loginInstructorId = authentication.getName();

        curriculumService.removeCurriculumLogically(curriculumId, loginInstructorId);
        return ResponseEntity.ok("SUCCESS");
    }

    // =====================================================================
    // 강좌(Course) CRUD
    // =====================================================================
    @GetMapping("/{curriculumId}/courses")
    @ResponseBody
    public ResponseEntity<List<CourseDto>> getCourseList(@PathVariable Long curriculumId) {
        List<CourseDto> courses = courseService.retrieveCourseByCurriculumId(curriculumId);
        return ResponseEntity.ok(courses);
    }

    @PostMapping("/{curriculumId}/courses")
    @ResponseBody
    public ResponseEntity<String> saveCourse(@PathVariable Long curriculumId,
            @RequestBody CourseSaveRequest request,
            Authentication authentication) {
        String loginInstructorId = authentication.getName();

        CourseDto courseDto = new CourseDto();
        courseDto.setCurriculumId(curriculumId);
        courseDto.setCourseNm(request.getCourseNm());
        courseDto.setCourseExplnCn(request.getCourseExplnCn());
        courseDto.setOpnnYn(request.getOpnnYn() != null ? request.getOpnnYn() : "Y");
        courseDto.setSortOrd(request.getSortOrd());
        boolean created = courseService.createCourse(courseDto, loginInstructorId);
        if (!created) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("FAIL");
        }
        return ResponseEntity.ok("SUCCESS");
    }

    @PutMapping("/courses/{courseSn}")
    @ResponseBody
    public ResponseEntity<String> modifyCourse(@PathVariable Long courseSn,
            @RequestBody CourseSaveRequest request,
            Authentication authentication) {
        String loginInstructorId = authentication.getName();

        CourseDto courseDto = new CourseDto();
        courseDto.setCourseSn(courseSn);
        courseDto.setCourseNm(request.getCourseNm());
        courseDto.setCourseExplnCn(request.getCourseExplnCn());
        courseDto.setOpnnYn(request.getOpnnYn());
        courseDto.setSortOrd(request.getSortOrd());
        courseService.modifyCourse(courseDto, loginInstructorId);
        return ResponseEntity.ok("SUCCESS");
    }

    @DeleteMapping("/courses/{courseSn}")
    @ResponseBody
    public ResponseEntity<String> deleteCourse(@PathVariable Long courseSn,
            Authentication authentication) {
        String loginInstructorId = authentication.getName();

        courseService.removeCourse(courseSn, loginInstructorId);
        return ResponseEntity.ok("SUCCESS");
    }

    // =====================================================================
    // 강의(Lecture) CRUD
    // =====================================================================
    @GetMapping("/courses/{courseSn}/lectures")
    @ResponseBody
    public ResponseEntity<List<LectureDto>> getLectureList(@PathVariable Long courseSn) {
        List<LectureDto> lectures = lectureService.retrieveLectureByCourseSn(courseSn);
        return ResponseEntity.ok(lectures);
    }

    @PostMapping("/courses/{courseSn}/lectures")
    @ResponseBody
    public ResponseEntity<String> saveLecture(@PathVariable Long courseSn,
            @RequestBody LectureSaveRequest request,
            Authentication authentication) {
        String loginInstructorId = authentication.getName();

        LectureDto lectureDto = new LectureDto();
        lectureDto.setCourseSn(courseSn);
        lectureDto.setLectureNm(request.getLectureNm());
        lectureDto.setLectureTypeCd(request.getLectureTypeCd());
        lectureDto.setLectureDuration(request.getLectureDuration());
        lectureDto.setLectureExplnCn(request.getLectureExplnCn());
        lectureDto.setOpnnYn(request.getOpnnYn() != null ? request.getOpnnYn() : "Y");
        lectureDto.setLockYn(request.getLockYn() != null ? request.getLockYn() : "N");
        lectureDto.setSortOrd(request.getSortOrd());
        lectureDto.setPrereqLectureSn(request.getPrereqLectureSn());
        lectureDto.setRgtrId(loginInstructorId);
        lectureDto.setLastMdfrId(loginInstructorId);

        boolean created = lectureService.createLecture(lectureDto);
        if (!created) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("FAIL");
        }
        return ResponseEntity.ok("SUCCESS");
    }

    @PutMapping("/courses/lectures/{lectureSn}")
    @ResponseBody
    public ResponseEntity<String> modifyLecture(@PathVariable Long lectureSn,
            @RequestBody LectureSaveRequest request,
            Authentication authentication) {
        String loginInstructorId = authentication.getName();

        LectureDto lectureDto = new LectureDto();
        lectureDto.setLectureSn(lectureSn);
        lectureDto.setLectureNm(request.getLectureNm());
        lectureDto.setLectureTypeCd(request.getLectureTypeCd());
        lectureDto.setLectureDuration(request.getLectureDuration());
        lectureDto.setLectureExplnCn(request.getLectureExplnCn());
        lectureDto.setOpnnYn(request.getOpnnYn());
        lectureDto.setLockYn(request.getLockYn());
        lectureDto.setSortOrd(request.getSortOrd());
        lectureDto.setPrereqLectureSn(request.getPrereqLectureSn());

        lectureService.modifyLecture(lectureDto, loginInstructorId);
        return ResponseEntity.ok("SUCCESS");
    }

    @DeleteMapping("/courses/lectures/{lectureSn}")
    @ResponseBody
    public ResponseEntity<String> deleteLecture(@PathVariable Long lectureSn,
            Authentication authentication) {
        String loginInstructorId = authentication.getName();

        lectureService.removeLecture(lectureSn, loginInstructorId);
        return ResponseEntity.ok("SUCCESS");
    }

    // =====================================================================
    // 공통
    // =====================================================================
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseBody
    public ResponseEntity<String> handleNotFound(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler(SecurityException.class)
    @ResponseBody
    public ResponseEntity<String> handleForbidden(SecurityException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
    }

}
