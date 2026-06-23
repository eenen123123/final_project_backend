package kr.or.ddit.controller.instructor;

import kr.or.ddit.finalProject.dto.exam.GeminiQuestionRequest;
import kr.or.ddit.finalProject.dto.exam.QuestionDto;
import kr.or.ddit.finalProject.dto.exam.QuestionSaveRequest;
import kr.or.ddit.finalProject.dto.exam.WeakPointDto;
import kr.or.ddit.finalProject.dto.subject.SubjectClassificationDto;
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
import java.util.Map;

/**
 * 강사 문항 관리 컨트롤러 (시험 관리는 클래스룸으로 분리됨)
 *
 * [URL 구조]
 *   GET  /instructor/questions                      → 문항 목록 (필터 + 페이징)
 *   GET  /instructor/questions/{sn}                 → 문항 상세 페이지
 *   POST /instructor/questions                      → 문항 등록
 *   POST /instructor/questions/{sn}/edit            → 문항 수정
 *   POST /instructor/questions/{sn}/delete          → 문항 논리 삭제
 *
 *   [AI 문항 생성 REST — questions.html AI 탭에서 사용]
 *   GET  /instructor/questions/ai/subjects          → 과목 목록 (대분류 기준)
 *   GET  /instructor/questions/ai/weak-points       → 약점 과목 목록
 *   POST /instructor/questions/ai/generate          → AI 문항 생성
 *   POST /instructor/questions/ai/save              → AI 생성 문항 저장
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
    // 문항 목록 (필터 + 페이징)
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

        List<SubjectClassificationDto> classifications = subjectMapper.selectClassificationList();

        model.addAttribute("questions", questions);
        model.addAttribute("classifications", classifications);
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
    // 문항 상세 페이지
    // ──────────────────────────────────────────────

    @GetMapping("/{qstnSn}")
    public String questionDetail(@PathVariable Long qstnSn, Model model, Authentication auth) {
        QuestionDto question = questionService.retrieveQuestion(qstnSn, auth.getName());
        List<SubjectClassificationDto> classifications = subjectMapper.selectClassificationList();

        Long currentSubjClId = null;
        if (question.getSubjId() != null) {
            SubjectDto subject = subjectMapper.selectSubjectBySn(question.getSubjId());
            if (subject != null) currentSubjClId = subject.getSubjClId();
        }

        model.addAttribute("question", question);
        model.addAttribute("classifications", classifications);
        model.addAttribute("currentSubjClId", currentSubjClId);
        return "admin:/instructor/detail-question";
    }

    // ──────────────────────────────────────────────
    // 문항 등록
    // ──────────────────────────────────────────────

    @PostMapping
    public String addQuestion(QuestionSaveRequest request, Authentication auth) {
        questionService.addQuestion(auth.getName(), request);
        return "redirect:/instructor/questions";
    }

    // ──────────────────────────────────────────────
    // 문항 수정
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

    @PostMapping("/ai/save")
    @ResponseBody
    public ResponseEntity<Map<String, String>> saveAiQuestion(
            @RequestBody QuestionSaveRequest request,
            Authentication auth) {
        questionService.addQuestion(auth.getName(), request);
        return ResponseEntity.ok(Map.of("result", "success"));
    }
}
