package kr.or.ddit.controller.instructor;

import kr.or.ddit.finalProject.dto.exam.ExamDto;
import kr.or.ddit.finalProject.dto.exam.ExamSaveRequest;
import kr.or.ddit.finalProject.dto.exam.QuestionDto;
import kr.or.ddit.finalProject.dto.exam.QuestionSaveRequest;
import kr.or.ddit.finalProject.service.exam.ExamService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

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
 * [접근 제어]
 *   instrUserId는 항상 Authentication에서 추출하며, 요청 파라미터로 받지 않습니다.
 *   문항·시험의 소유권 확인은 서비스 레이어에서 처리합니다.
 */
@Controller
@RequestMapping("/instructor/exams")
@RequiredArgsConstructor
public class InstructorExamController {

    private final ExamService examService;

    // ──────────────────────────────────────────────
    // 문항·시험 통합 관리 페이지
    // ──────────────────────────────────────────────

    @GetMapping
    public String examPage(Model model, Authentication auth) {
        String instrUserId = auth.getName();

        // 내 문항 목록 (STAT_CD != '99', stem/choices 파싱 포함)
        List<QuestionDto> questions = examService.retrieveMyQuestions(instrUserId);

        // 내 시험 목록 (EXAM_STAT_CD != '99')
        List<ExamDto> exams = examService.retrieveMyExams(instrUserId);

        model.addAttribute("questions", questions);
        model.addAttribute("exams", exams);

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
        // 소유권 확인은 서비스에서 처리
        examService.modifyQuestion(qstnSn, auth.getName(), request);
        return "redirect:/instructor/exams";
    }

    // ──────────────────────────────────────────────
    // 문항 논리 삭제
    // ──────────────────────────────────────────────

    @PostMapping("/questions/{qstnSn}/delete")
    public String deleteQuestion(@PathVariable Long qstnSn, Authentication auth) {
        // 소유권 확인 및 STAT_CD='99' 처리는 서비스에서 처리
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
        // 소유권 확인은 서비스에서 처리
        examService.modifyExam(examSn, auth.getName(), request);
        return "redirect:/instructor/exams";
    }

    // ──────────────────────────────────────────────
    // 시험 논리 삭제
    // ──────────────────────────────────────────────

    @PostMapping("/{examSn}/delete")
    public String deleteExam(@PathVariable Long examSn, Authentication auth) {
        // 소유권 확인 및 EXAM_STAT_CD='99' 처리는 서비스에서 처리
        examService.removeExam(examSn, auth.getName());
        return "redirect:/instructor/exams";
    }
}
