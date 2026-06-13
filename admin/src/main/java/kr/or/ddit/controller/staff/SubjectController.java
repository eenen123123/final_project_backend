package kr.or.ddit.controller.staff;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.or.ddit.finalProject.dto.subject.SubjectClassificationDto;
import kr.or.ddit.finalProject.dto.subject.SubjectDto;
import kr.or.ddit.finalProject.service.subject.SubjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/admin/subject")
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectService subjectService;

    // ── 페이지 렌더링 ────────────────────────────────────────────────

    @GetMapping
    public String list(Model model) {
        model.addAttribute("classificationList", subjectService.retrieveClassificationList());
        return "admin:/staff/subject-list";
    }

    // ── 대분류 CRUD (AJAX) ───────────────────────────────────────────

    @PostMapping("/classification/create")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createClassification(
            @ModelAttribute SubjectClassificationDto dto,
            Authentication authentication) {
        try {
            subjectService.createClassification(dto, authentication.getName());
            return ResponseEntity.ok(Map.of("success", true, "subjClId", dto.getSubjClId()));
        } catch (Exception e) {
            log.error("대분류 생성 오류", e);
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/classification/{subjClId}/update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateClassification(
            @PathVariable Long subjClId,
            @ModelAttribute SubjectClassificationDto dto,
            Authentication authentication) {
        dto.setSubjClId(subjClId);
        try {
            subjectService.modifyClassification(dto, authentication.getName());
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("대분류 수정 오류", e);
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/classification/{subjClId}/delete")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteClassification(
            @PathVariable Long subjClId,
            Authentication authentication) {
        try {
            subjectService.removeClassification(subjClId, authentication.getName());
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("대분류 삭제 오류", e);
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // ── 과목 CRUD (AJAX) ─────────────────────────────────────────────

    @GetMapping("/classification/{subjClId}/subjects")
    @ResponseBody
    public ResponseEntity<List<SubjectDto>> subjectList(@PathVariable Long subjClId) {
        return ResponseEntity.ok(subjectService.retrieveSubjectList(subjClId));
    }

    @PostMapping("/classification/{subjClId}/create")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createSubject(
            @PathVariable Long subjClId,
            @ModelAttribute SubjectDto dto,
            Authentication authentication) {
        dto.setSubjClId(subjClId);
        try {
            subjectService.createSubject(dto, authentication.getName());
            return ResponseEntity.ok(Map.of("success", true, "subjId", dto.getSubjId()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("과목 생성 오류", e);
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/classification/{subjClId}/{subjId}/update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateSubject(
            @PathVariable Long subjClId,
            @PathVariable Long subjId,
            @ModelAttribute SubjectDto dto,
            Authentication authentication) {
        dto.setSubjClId(subjClId);
        dto.setSubjId(subjId);
        try {
            subjectService.modifySubject(dto, authentication.getName());
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("과목 수정 오류", e);
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/classification/{subjClId}/{subjId}/delete")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteSubject(
            @PathVariable Long subjClId,
            @PathVariable Long subjId,
            Authentication authentication) {
        try {
            subjectService.removeSubject(subjId, subjClId, authentication.getName());
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("과목 삭제 오류", e);
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
