package kr.or.ddit.controller.instructor;

import kr.or.ddit.finalProject.dto.classroom.ClassroomDetailResponse;
import kr.or.ddit.finalProject.dto.exam.ExamDto;
import kr.or.ddit.finalProject.dto.exam.ExamSaveRequest;
import kr.or.ddit.finalProject.dto.exam.ExamTakerDto;
import kr.or.ddit.finalProject.dto.exam.QuestionDto;
import kr.or.ddit.finalProject.exception.FinalProjectException;
import kr.or.ddit.finalProject.service.assignment.AssignmentBoardService;
import kr.or.ddit.finalProject.service.classroom.ClassroomService;
import kr.or.ddit.finalProject.service.exam.ExamService;
import kr.or.ddit.finalProject.service.exam.QuestionService;
import kr.or.ddit.finalProject.service.instructor.InstructorBoardService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/classroom")
public class AdminClassroomExamController extends AbstractClassroomController {

    private final ExamService examService;
    private final QuestionService questionService;

    public AdminClassroomExamController(ClassroomService classroomService,
                                        AssignmentBoardService assignmentBoardService,
                                        InstructorBoardService instructorBoardService,
                                        ExamService examService,
                                        QuestionService questionService) {
        super(classroomService, assignmentBoardService, instructorBoardService);
        this.examService = examService;
        this.questionService = questionService;
    }

    // 시험 목록
    @GetMapping("/detail/{classSn}/exams")
    public String examList(@PathVariable Long classSn, Model model, Authentication auth) {
        ClassroomDetailResponse classroom = getOwnedClassroom(classSn, auth.getName());
        if (classroom == null) return "redirect:/classroom/list";
        List<ExamDto> exams = examService.retrieveExamsByClassSn(classSn);
        model.addAttribute("classroom", classroom);
        model.addAttribute("exams", exams);
        return "classroom/list-classroom-exams";
    }

    // 시험 등록 폼
    @GetMapping("/detail/{classSn}/exams/write")
    public String examWriteForm(@PathVariable Long classSn, Model model, Authentication auth) {
        ClassroomDetailResponse classroom = getOwnedClassroom(classSn, auth.getName());
        if (classroom == null) return "redirect:/classroom/list";
        List<QuestionDto> myQuestions = questionService.retrieveMyQuestionsBySubjId(auth.getName(), classroom.getSubjId());
        model.addAttribute("classroom", classroom);
        model.addAttribute("myQuestions", myQuestions);
        return "classroom/form-classroom-exam";
    }

    // 시험 등록
    @PostMapping("/detail/{classSn}/exams/write")
    public String examWrite(@PathVariable Long classSn,
                            @ModelAttribute ExamSaveRequest request,
                            Authentication auth) {
        if (getOwnedClassroom(classSn, auth.getName()) == null)
            return "redirect:/classroom/list";
        request.setClassSn(classSn);
        examService.addExam(auth.getName(), request);
        return "redirect:/classroom/detail/" + classSn + "/exams";
    }

    // 시험 상세 (응시자 목록 포함)
    @GetMapping("/detail/{classSn}/exams/{examSn}")
    public String examDetail(@PathVariable Long classSn, @PathVariable Long examSn,
                             Model model, Authentication auth) {
        ClassroomDetailResponse classroom = getOwnedClassroom(classSn, auth.getName());
        if (classroom == null) return "redirect:/classroom/list";
        ExamDto exam;
        List<ExamTakerDto> takers;
        try {
            exam = examService.retrieveExamDetail(examSn, auth.getName());
            takers = examService.retrieveTakersDirectly(examSn);
        } catch (FinalProjectException e) {
            return "redirect:/classroom/detail/" + classSn + "/exams";
        }
        if (!classSn.equals(exam.getClassSn()))
            return "redirect:/classroom/detail/" + classSn + "/exams";
        model.addAttribute("classroom", classroom);
        model.addAttribute("exam", exam);
        model.addAttribute("takers", takers);
        return "classroom/detail-classroom-exam";
    }

    // 시험 수정 폼
    @GetMapping("/detail/{classSn}/exams/{examSn}/edit")
    public String examEditForm(@PathVariable Long classSn, @PathVariable Long examSn,
                               Model model, Authentication auth) {
        ClassroomDetailResponse classroom = getOwnedClassroom(classSn, auth.getName());
        if (classroom == null) return "redirect:/classroom/list";
        ExamDto exam;
        try {
            exam = examService.retrieveExamDetail(examSn, auth.getName());
        } catch (FinalProjectException e) {
            return "redirect:/classroom/detail/" + classSn + "/exams";
        }
        if (!classSn.equals(exam.getClassSn()))
            return "redirect:/classroom/detail/" + classSn + "/exams";
        List<QuestionDto> myQuestions = questionService.retrieveMyQuestionsBySubjId(auth.getName(), classroom.getSubjId());
        List<Long> orderedQstnSns = exam.getQuestions().stream()
                .map(kr.or.ddit.finalProject.dto.exam.ExamQuestionDto::getQstnSn)
                .collect(Collectors.toList());
        model.addAttribute("classroom", classroom);
        model.addAttribute("exam", exam);
        model.addAttribute("myQuestions", myQuestions);
        model.addAttribute("orderedQstnSns", orderedQstnSns);
        return "classroom/form-classroom-exam";
    }

    // 시험 수정 저장
    @PostMapping("/detail/{classSn}/exams/{examSn}/edit")
    public String examEdit(@PathVariable Long classSn, @PathVariable Long examSn,
                           @ModelAttribute ExamSaveRequest request,
                           Authentication auth) {
        if (getOwnedClassroom(classSn, auth.getName()) == null)
            return "redirect:/classroom/list";
        ExamDto existing;
        try {
            existing = examService.retrieveExamDetail(examSn, auth.getName());
        } catch (FinalProjectException e) {
            return "redirect:/classroom/detail/" + classSn + "/exams";
        }
        if (!classSn.equals(existing.getClassSn()))
            return "redirect:/classroom/detail/" + classSn + "/exams";
        request.setClassSn(classSn);
        try {
            examService.modifyExam(examSn, auth.getName(), request);
        } catch (FinalProjectException e) {
            return "redirect:/classroom/detail/" + classSn + "/exams";
        }
        return "redirect:/classroom/detail/" + classSn + "/exams/" + examSn;
    }

    // 시험 삭제
    @PostMapping("/detail/{classSn}/exams/{examSn}/delete")
    public String examDelete(@PathVariable Long classSn, @PathVariable Long examSn,
                             Authentication auth) {
        if (getOwnedClassroom(classSn, auth.getName()) == null)
            return "redirect:/classroom/list";
        ExamDto existing;
        try {
            existing = examService.retrieveExamDetail(examSn, auth.getName());
        } catch (FinalProjectException e) {
            return "redirect:/classroom/detail/" + classSn + "/exams";
        }
        if (!classSn.equals(existing.getClassSn()))
            return "redirect:/classroom/detail/" + classSn + "/exams";
        try {
            examService.removeExam(examSn, auth.getName());
        } catch (FinalProjectException e) {
            return "redirect:/classroom/detail/" + classSn + "/exams";
        }
        return "redirect:/classroom/detail/" + classSn + "/exams";
    }
}
