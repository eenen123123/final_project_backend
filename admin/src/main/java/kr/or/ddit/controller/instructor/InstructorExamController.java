package kr.or.ddit.controller.instructor;

import kr.or.ddit.finalProject.dto.exam.ExamDto;
import kr.or.ddit.finalProject.dto.exam.ExamSaveRequest;
import kr.or.ddit.finalProject.dto.exam.GeminiQuestionRequest;
import kr.or.ddit.finalProject.dto.exam.QuestionDto;
import kr.or.ddit.finalProject.dto.exam.QuestionSaveRequest;
import kr.or.ddit.finalProject.dto.exam.WeakPointDto;
import kr.or.ddit.finalProject.dto.subject.SubjectClassificationDto;
import kr.or.ddit.finalProject.dto.subject.SubjectDto;
import kr.or.ddit.finalProject.mapper.subject.SubjectMapper;
import kr.or.ddit.finalProject.service.exam.ExamService;
import kr.or.ddit.finalProject.service.exam.GeminiQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 강사 문항·시험 관리 컨트롤러
 *
 * [URL 구조]
 *   GET  /instructor/exams                             → 문항·시험 통합 관리 페이지
 *   POST /instructor/exams/questions                   → 문항 등록
 *   POST /instructor/exams/questions/{sn}/update       → 문항 수정
 *   POST /instructor/exams/questions/{sn}/delete       → 문항 논리 삭제
 *   POST /instructor/exams/create                      → 시험 등록
 *   POST /instructor/exams/{examSn}/update             → 시험 수정
 *   POST /instructor/exams/{examSn}/delete             → 시험 논리 삭제
 *
 *   [AI 문항 생성 REST — exam.html AI 탭에서 사용]
 *   GET  /instructor/exams/ai/subjects                 → 과목 목록 (대분류 기준)
 *   GET  /instructor/exams/ai/weak-points              → 약점 과목 목록
 *   POST /instructor/exams/ai/generate                 → AI 문항 생성
 *
 * [접근 제어]
 *   instrUserId는 항상 Authentication에서 추출하며, 요청 파라미터로 받지 않습니다.
 */
@Controller
@RequestMapping("/instructor/exams")
@RequiredArgsConstructor
public class InstructorExamController {

    private final ExamService examService;
    private final GeminiQuestionService geminiQuestionService;
    private final SubjectMapper subjectMapper;

    // ──────────────────────────────────────────────
    // 문항·시험 통합 관리 페이지
    // ──────────────────────────────────────────────

    @GetMapping
    public String examPage(Model model, Authentication auth) {
        String instrUserId = auth.getName();

        List<QuestionDto> questions = examService.retrieveMyQuestions(instrUserId);
        List<ExamDto> exams = examService.retrieveMyExams(instrUserId);
        List<SubjectClassificationDto> classifications = subjectMapper.selectClassificationList();

        model.addAttribute("questions", questions);
        model.addAttribute("exams", exams);
        model.addAttribute("classifications", classifications);

        return "admin:/instructor/exam";
    }

    // ──────────────────────────────────────────────
    // 문항 등록
    // ──────────────────────────────────────────────

    @PostMapping("/questions")
    public String addQuestion(QuestionSaveRequest request, Authentication auth) {
        examService.addQuestion(auth.getName(), request);
        return "redirect:/instructor/exams";
    }

    // ──────────────────────────────────────────────
    // 문항 수정
    // ──────────────────────────────────────────────

    @PostMapping("/questions/{qstnSn}/update")
    public String updateQuestion(@PathVariable Long qstnSn,
                                 QuestionSaveRequest request,
                                 Authentication auth) {
        examService.modifyQuestion(qstnSn, auth.getName(), request);
        return "redirect:/instructor/exams";
    }

    // ──────────────────────────────────────────────
    // 문항 논리 삭제
    // ──────────────────────────────────────────────

    @PostMapping("/questions/{qstnSn}/delete")
    public String deleteQuestion(@PathVariable Long qstnSn, Authentication auth) {
        examService.removeQuestion(qstnSn, auth.getName());
        return "redirect:/instructor/exams";
    }

    // ──────────────────────────────────────────────
    // 시험 등록
    // ──────────────────────────────────────────────

    @PostMapping("/create")
    public String createExam(ExamSaveRequest request, Authentication auth) {
        examService.addExam(auth.getName(), request);
        return "redirect:/instructor/exams";
    }

    // ──────────────────────────────────────────────
    // 시험 수정
    // ──────────────────────────────────────────────

    @PostMapping("/{examSn}/update")
    public String updateExam(@PathVariable Long examSn,
                             ExamSaveRequest request,
                             Authentication auth) {
        examService.modifyExam(examSn, auth.getName(), request);
        return "redirect:/instructor/exams";
    }

    // ──────────────────────────────────────────────
    // 시험 논리 삭제
    // ──────────────────────────────────────────────

    @PostMapping("/{examSn}/delete")
    public String deleteExam(@PathVariable Long examSn, Authentication auth) {
        examService.removeExam(examSn, auth.getName());
        return "redirect:/instructor/exams";
    }

    // ──────────────────────────────────────────────
    // AI 문항 생성 — REST (exam.html AI 탭)
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
        examService.addQuestion(auth.getName(), request);
        return ResponseEntity.ok(Map.of("result", "success"));
    }
}
