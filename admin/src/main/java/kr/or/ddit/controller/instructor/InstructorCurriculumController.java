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

import kr.or.ddit.finalProject.dto.course.CourseMappingRequest;
import kr.or.ddit.finalProject.dto.course.CourseDto;
import kr.or.ddit.finalProject.dto.curriculum.CurriculumDto;
import kr.or.ddit.finalProject.dto.curriculum.CurriculumSaveRequest;
import kr.or.ddit.finalProject.service.course.CourseService;
import kr.or.ddit.finalProject.service.curriculum.CurriculumService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/instructor/curriculum")
@RequiredArgsConstructor
public class InstructorCurriculumController {

    private final CurriculumService curriculumService;
    private final CourseService courseService;

    // =====================================================================
    // 페이지
    // =====================================================================

    @GetMapping
    public String curriculumMainPage(Model model, Authentication authentication) {
        String loginInstructorId = authentication.getName();
        model.addAttribute("curriculumList", curriculumService.retrieveList(loginInstructorId));
        return "admin:/instructor/curriculum";
    }

    // =====================================================================
    // 커리큘럼 CRUD
    // =====================================================================

    @PostMapping("/save")
    @ResponseBody
    public ResponseEntity<String> saveCurriculum(@RequestBody CurriculumSaveRequest request,
            Authentication authentication) {
        String loginInstructorId = authentication.getName();

        CurriculumDto curriculumDto = new CurriculumDto();
        curriculumDto.setTitle(request.getTitle());
        curriculumDto.setStrtDt(request.getStrtDt());
        curriculumDto.setEndDt(request.getEndDt());
        curriculumDto.setExplnCn(request.getExplnCn());
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
        curriculumDto.setStrtDt(request.getStrtDt());
        curriculumDto.setEndDt(request.getEndDt());
        curriculumDto.setExplnCn(request.getExplnCn());
        curriculumService.modifyCurriculum(curriculumDto, loginInstructorId);
        return ResponseEntity.ok("SUCCESS");
    }

    @DeleteMapping("/delete/{curriculumId}")
    @ResponseBody
    public ResponseEntity<String> deleteCurriculum(@PathVariable Long curriculumId,
            Authentication authentication) {
        curriculumService.removeCurriculumLogically(curriculumId, authentication.getName());
        return ResponseEntity.ok("SUCCESS");
    }

    // =====================================================================
    // 커리큘럼-강좌 매핑
    // =====================================================================

    /** 커리큘럼에 속한 강좌 목록 조회 */
    @GetMapping("/{curriculumId}/courses")
    @ResponseBody
    public ResponseEntity<List<CourseDto>> getCourseList(@PathVariable Long curriculumId) {
        return ResponseEntity.ok(courseService.retrieveCourseByCurriculumId(curriculumId));
    }

    /** 아직 커리큘럼에 등록되지 않은 강좌 목록 (추가 선택용) */
    @GetMapping("/available-courses")
    @ResponseBody
    public ResponseEntity<List<CourseDto>> getAvailableCourses() {
        return ResponseEntity.ok(curriculumService.retrieveAvailableCourses());
    }

    /** 기존 강좌를 커리큘럼에 추가 */
    @PostMapping("/{curriculumId}/courses")
    @ResponseBody
    public ResponseEntity<String> addCourseMapping(@PathVariable Long curriculumId,
            @RequestBody CourseMappingRequest request,
            Authentication authentication) {
        curriculumService.addCourseMapping(curriculumId, request.getCourseSn(),
                authentication.getName());
        return ResponseEntity.ok("SUCCESS");
    }

    /** 커리큘럼에서 강좌 제거 (강좌 자체는 삭제되지 않음) */
    @DeleteMapping("/{curriculumId}/courses/{courseSn}")
    @ResponseBody
    public ResponseEntity<String> removeCourseMapping(@PathVariable Long curriculumId,
            @PathVariable Long courseSn,
            Authentication authentication) {
        curriculumService.removeCourseMapping(curriculumId, courseSn, authentication.getName());
        return ResponseEntity.ok("SUCCESS");
    }

    /** 강좌 순서 위로 이동 */
    @PutMapping("/{curriculumId}/courses/{courseSn}/move-up")
    @ResponseBody
    public ResponseEntity<String> moveCourseUp(@PathVariable Long curriculumId,
            @PathVariable Long courseSn,
            Authentication authentication) {
        curriculumService.moveCourseUp(curriculumId, courseSn, authentication.getName());
        return ResponseEntity.ok("SUCCESS");
    }

    /** 강좌 순서 아래로 이동 */
    @PutMapping("/{curriculumId}/courses/{courseSn}/move-down")
    @ResponseBody
    public ResponseEntity<String> moveCourseDown(@PathVariable Long curriculumId,
            @PathVariable Long courseSn,
            Authentication authentication) {
        curriculumService.moveCourseDown(curriculumId, courseSn, authentication.getName());
        return ResponseEntity.ok("SUCCESS");
    }

    // =====================================================================
    // 공통 예외 처리
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
