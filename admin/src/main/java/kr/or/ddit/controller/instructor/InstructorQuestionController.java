package kr.or.ddit.controller.instructor;

import kr.or.ddit.finalProject.dto.exam.GeminiQuestionRequest;
import kr.or.ddit.finalProject.dto.exam.QuestionDto;
import kr.or.ddit.finalProject.dto.exam.QuestionSaveRequest;
import kr.or.ddit.finalProject.dto.exam.WeakPointDto;
import kr.or.ddit.finalProject.dto.subject.SubjectDto;
import kr.or.ddit.finalProject.mapper.subject.SubjectMapper;
import kr.or.ddit.finalProject.service.exam.GeminiQuestionService;
import kr.or.ddit.finalProject.service.exam.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 강사 문항 관리 컨트롤러
 *
 * GET  /instructor/questions                  → 문항 목록 (필터 + 페이징)
 * GET  /instructor/questions/new              → 문항 등록 폼
 * GET  /instructor/questions/{sn}             → 문항 상세
 * GET  /instructor/questions/{sn}/edit        → 문항 수정 폼
 * POST /instructor/questions                  → 문항 등록 처리
 * POST /instructor/questions/{sn}/edit        → 문항 수정 처리
 * POST /instructor/questions/{sn}/delete      → 문항 논리 삭제
 *
 * GET  /instructor/questions/ai/subjects      → 과목 목록 REST
 * GET  /instructor/questions/ai/weak-points   → 약점 과목 목록 REST
 * POST /instructor/questions/ai/generate      → AI 문항 생성 REST
 */
@Controller
@RequestMapping("/instructor/questions")
@RequiredArgsConstructor
public class InstructorQuestionController {

    private static final int PAGE_SIZE = 10;

    private final QuestionService questionService;
    private final GeminiQuestionService geminiQuestionService;
    private final SubjectMapper subjectMapper;

    // ──────────────────────────────────────────────
    // 문항 목록
    // ──────────────────────────────────────────────

    @GetMapping
    public String questionsPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) Long subjId,
            @RequestParam(required = false) Long subjClId,
            @RequestParam(required = false) String diffCd,
            Model model, Authentication auth) {

        String instrUserId = auth.getName();

        List<QuestionDto> questions =
                questionService.retrieveQuestionPage(instrUserId, subjId, diffCd, page, PAGE_SIZE);
        int totalCount = questionService.countQuestions(instrUserId, subjId, diffCd);
        int totalPages = (int) Math.ceil((double) totalCount / PAGE_SIZE);

        model.addAttribute("questions", questions);
        model.addAttribute("classifications", subjectMapper.selectClassificationList());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("pageSize", PAGE_SIZE);
        model.addAttribute("selectedSubjId", subjId);
        model.addAttribute("selectedSubjClId", subjClId);
        model.addAttribute("selectedDiffCd", diffCd);

        return "admin:/instructor/list-questions";
    }

    // ──────────────────────────────────────────────
    // 문항 등록 폼
    // ──────────────────────────────────────────────

    @GetMapping("/new")
    public String newQuestionForm(Model model) {
        model.addAttribute("classifications", subjectMapper.selectClassificationList());
        return "admin:/instructor/form-question";
    }

    // ──────────────────────────────────────────────
    // 문항 상세
    // ──────────────────────────────────────────────

    @GetMapping("/{qstnSn}")
    public String questionDetail(@PathVariable Long qstnSn, Model model, Authentication auth) {
        model.addAttribute("question", questionService.retrieveQuestion(qstnSn, auth.getName()));
        return "admin:/instructor/detail-question";
    }

    // ──────────────────────────────────────────────
    // 문항 수정 폼
    // ──────────────────────────────────────────────

    @GetMapping("/{qstnSn}/edit")
    public String editQuestionForm(@PathVariable Long qstnSn, Model model, Authentication auth) {
        QuestionDto question = questionService.retrieveQuestion(qstnSn, auth.getName());

        Long currentSubjClId = null;
        if (question.getSubjId() != null) {
            SubjectDto subject = subjectMapper.selectSubjectBySn(question.getSubjId());
            if (subject != null) currentSubjClId = subject.getSubjClId();
        }

        model.addAttribute("question", question);
        model.addAttribute("currentSubjClId", currentSubjClId);
        model.addAttribute("classifications", subjectMapper.selectClassificationList());
        return "admin:/instructor/form-question";
    }

    // ──────────────────────────────────────────────
    // 문항 등록 처리
    // ──────────────────────────────────────────────

    @PostMapping
    public String addQuestion(QuestionSaveRequest request, Authentication auth) {
        questionService.addQuestion(auth.getName(), request);
        return "redirect:/instructor/questions";
    }

    // ──────────────────────────────────────────────
    // 문항 수정 처리
    // ──────────────────────────────────────────────

    @PostMapping("/{qstnSn}/edit")
    public String updateQuestion(@PathVariable Long qstnSn,
                                 QuestionSaveRequest request,
                                 Authentication auth) {
        questionService.modifyQuestion(qstnSn, auth.getName(), request);
        return "redirect:/instructor/questions/" + qstnSn;
    }

    // ──────────────────────────────────────────────
    // 문항 논리 삭제
    // ──────────────────────────────────────────────

    @PostMapping("/{qstnSn}/delete")
    public String deleteQuestion(@PathVariable Long qstnSn, Authentication auth) {
        questionService.removeQuestion(qstnSn, auth.getName());
        return "redirect:/instructor/questions";
    }

    // ──────────────────────────────────────────────
    // AI 문항 생성 REST
    // ──────────────────────────────────────────────

    @GetMapping("/ai/subjects")
    @ResponseBody
    public ResponseEntity<List<SubjectDto>> getSubjects(@RequestParam Long subjClId) {
        return ResponseEntity.ok(subjectMapper.selectSubjectList(subjClId));
    }

    @GetMapping("/ai/weak-points")
    @ResponseBody
    public ResponseEntity<List<WeakPointDto>> getWeakPoints(
            @RequestParam(required = false) Long classSn) {
        return ResponseEntity.ok(geminiQuestionService.retrieveWeakPoints(classSn));
    }

    @PostMapping("/ai/generate")
    @ResponseBody
    public ResponseEntity<QuestionDto> generateQuestion(
            @RequestBody GeminiQuestionRequest request) {
        return ResponseEntity.ok(geminiQuestionService.generateQuestion(request));
    }
}
