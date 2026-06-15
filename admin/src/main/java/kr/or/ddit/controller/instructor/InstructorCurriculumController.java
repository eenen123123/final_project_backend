package kr.or.ddit.controller.instructor;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;

import kr.or.ddit.finalProject.dto.course.CourseDto;
import kr.or.ddit.finalProject.dto.curriculum.CurriculumDto;
import kr.or.ddit.finalProject.dto.staff.AdminActivityType;
import kr.or.ddit.finalProject.service.curriculum.CurriculumService;
import kr.or.ddit.service.AdminActivityApprovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/instructor/curriculum")
@RequiredArgsConstructor
public class InstructorCurriculumController {

    private final CurriculumService curriculumService;
    private final AdminActivityApprovalService adminActivityApprovalService;

    // ── 페이지 렌더링 ────────────────────────────────────────────────

    /** 커리큘럼 목록 페이지. 로그인한 강사의 커리큘럼만 표시한다. */
    @GetMapping
    public String list(Model model, Authentication authentication) {
        String userId = authentication.getName();
        model.addAttribute("curriculumList", curriculumService.retrieveList(userId));
        return "admin:/instructor/curriculum-list";
    }

    // ── 커리큘럼 CRUD (AJAX) ─────────────────────────────────────────

    /** 커리큘럼 생성 결재 요청. */
    @PostMapping("/create")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> create(
            @ModelAttribute CurriculumDto curriculumDto,
            Authentication authentication) {
        String userId = authentication.getName();
        curriculumDto.setInstructorId(userId);
        curriculumDto.setRgtrId(userId);
        curriculumDto.setLastMdfrId(userId);
        try {
            // boolean created = curriculumService.createCurriculum(curriculumDto);
            // if (!created) return ResponseEntity.ok(Map.of("success", false, "message", "커리큘럼 생성에 실패했습니다."));
            // return ResponseEntity.ok(Map.of("success", true, "curriculumId", curriculumDto.getCurriculumId()));
            Map<String, Object> payload = new HashMap<>();
            payload.put("curriculumDto", curriculumDto);
            adminActivityApprovalService.submitForApproval(
                userId,
                AdminActivityType.CURRICULUM_CREATE,
                curriculumDto.getTitle(),
                payload
            );
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            log.error("커리큘럼 생성 결재 요청 오류", e);
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /** 커리큘럼 수정 결재 요청. */
    @PostMapping("/{curriculumId}/update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable Long curriculumId,
            @ModelAttribute CurriculumDto curriculumDto,
            Authentication authentication) {
        curriculumDto.setCurriculumId(curriculumId);
        try {
            // curriculumService.modifyCurriculum(curriculumDto, authentication.getName());
            // return ResponseEntity.ok(Map.of("success", true));
            Map<String, Object> payload = new HashMap<>();
            payload.put("curriculumDto", curriculumDto);
            adminActivityApprovalService.submitForApproval(
                authentication.getName(),
                AdminActivityType.CURRICULUM_UPDATE,
                curriculumDto.getTitle(),
                payload
            );
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("커리큘럼 수정 결재 요청 오류", e);
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /** 커리큘럼 삭제 결재 요청 (승인 후 USE_YN = 'N' 처리). */
    @PostMapping("/{curriculumId}/delete")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> delete(
            @PathVariable Long curriculumId,
            Authentication authentication) {
        try {
            // curriculumService.removeCurriculumLogically(curriculumId, authentication.getName());
            // return ResponseEntity.ok(Map.of("success", true));
            Map<String, Object> payload = new HashMap<>();
            payload.put("curriculumId", curriculumId);
            adminActivityApprovalService.submitForApproval(
                authentication.getName(),
                AdminActivityType.CURRICULUM_DELETE,
                "커리큘럼 ID: " + curriculumId,
                payload
            );
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException | SecurityException e) {
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("커리큘럼 삭제 결재 요청 오류", e);
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // ── 강좌 매핑 (AJAX) ─────────────────────────────────────────────

    /** 커리큘럼에 속한 강좌 목록을 SORT_ORD 오름차순으로 반환한다. 타 강사 커리큘럼 접근 시 403. */
    @GetMapping("/{curriculumId}/courses")
    @ResponseBody
    public ResponseEntity<List<CourseDto>> mappedCourses(
            @PathVariable Long curriculumId,
            Authentication authentication) {
        CurriculumDto curriculum = curriculumService.retrieveById(curriculumId);
        if (curriculum == null || !curriculum.getInstructorId().equals(authentication.getName())) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(curriculumService.retrieveMappedCourses(curriculumId));
    }

    /** 로그인한 강사의 강좌 중 아직 커리큘럼에 배정되지 않은 강좌 목록을 반환한다. */
    @GetMapping("/available-courses")
    @ResponseBody
    public ResponseEntity<List<CourseDto>> availableCourses(Authentication authentication) {
        return ResponseEntity.ok(curriculumService.retrieveAvailableCourses(authentication.getName()));
    }

    /** 강좌를 커리큘럼에 추가한다. body: { "courseSn": 1 } */
    @PostMapping("/{curriculumId}/courses/add")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addCourse(
            @PathVariable Long curriculumId,
            @RequestBody Map<String, Long> body,
            Authentication authentication) {
        Long courseSn = body.get("courseSn");
        if (courseSn == null) {
            return ResponseEntity.ok(Map.of("success", false, "message", "courseSn이 필요합니다."));
        }
        try {
            curriculumService.addCourseMapping(curriculumId, courseSn, authentication.getName());
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException | SecurityException e) {
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("강좌 매핑 추가 오류", e);
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /** 강좌의 커리큘럼 매핑을 해제한다. body: { "courseSn": 1 } */
    @PostMapping("/{curriculumId}/courses/remove")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> removeCourse(
            @PathVariable Long curriculumId,
            @RequestBody Map<String, Long> body,
            Authentication authentication) {
        Long courseSn = body.get("courseSn");
        if (courseSn == null) {
            return ResponseEntity.ok(Map.of("success", false, "message", "courseSn이 필요합니다."));
        }
        try {
            curriculumService.removeCourseMapping(curriculumId, courseSn, authentication.getName());
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException | SecurityException e) {
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("강좌 매핑 해제 오류", e);
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * 커리큘럼 내 강좌 순서를 재배치한다.
     * body: { "courseSnList": [3, 1, 2] } — 배열 순서대로 SORT_ORD 1-based 재부여.
     */
    @PostMapping("/{curriculumId}/courses/reorder")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> reorderCourses(
            @PathVariable Long curriculumId,
            @RequestBody Map<String, List<Long>> body,
            Authentication authentication) {
        List<Long> courseSnList = body.get("courseSnList");
        if (courseSnList == null || courseSnList.isEmpty()) {
            return ResponseEntity.ok(Map.of("success", false, "message", "courseSnList가 필요합니다."));
        }
        try {
            curriculumService.reorderCourses(curriculumId, courseSnList, authentication.getName());
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException | SecurityException e) {
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("강좌 순서 변경 오류", e);
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
