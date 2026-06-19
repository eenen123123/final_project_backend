package kr.or.ddit.controller.instructor;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import kr.or.ddit.finalProject.service.assignment.AssignmentBoardService;
import kr.or.ddit.finalProject.service.classroom.ClassroomService;
import kr.or.ddit.finalProject.service.instructor.InstructorBoardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/classroom")
@RequiredArgsConstructor
public class AdminClassroomQnaController {

    private final ClassroomService classroomService;
    private final InstructorBoardService instructorBoardService;
    private final AssignmentBoardService assignmentBoardService;

    @ModelAttribute
    public void addTabBadges(@PathVariable(required = false) Long classSn, Model model) {
        if (classSn != null) {
            model.addAttribute("assignmentCount",
                    assignmentBoardService.getPendingGradeCount(classSn));
            model.addAttribute("unansweredQnaCount",
                    instructorBoardService.getUnansweredQnaCount(classSn));
        }
    }

    private kr.or.ddit.finalProject.dto.classroom.ClassroomDetailResponse getOwnedClassroom(Long classSn, String userId) {
        try {
            kr.or.ddit.finalProject.dto.classroom.ClassroomDetailResponse classroom = classroomService.retrieveClassroomDetail(classSn);
            return userId.equals(classroom.getInstrUserId()) ? classroom : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // Q&A 목록
    @GetMapping("/detail/{classSn}/qna")
    public String qnaList(@PathVariable Long classSn, Model model, Authentication authentication) {
        kr.or.ddit.finalProject.dto.classroom.ClassroomDetailResponse classroom = getOwnedClassroom(classSn, authentication.getName());
        if (classroom == null) return "redirect:/classroom/list";
        model.addAttribute("classroom", classroom);
        model.addAttribute("qnaList", instructorBoardService.getClassroomQnaList(classSn));
        return "classroom/list-classroom-qna";
    }

    // Q&A 상세 (editAnswer=true면 답변 수정 모드)
    @GetMapping("/detail/{classSn}/qna/{postSn}")
    public String qnaDetail(@PathVariable Long classSn, @PathVariable Long postSn,
            @RequestParam(required = false) boolean editAnswer, Model model,
            Authentication authentication) {
        kr.or.ddit.finalProject.dto.classroom.ClassroomDetailResponse classroom = getOwnedClassroom(classSn, authentication.getName());
        if (classroom == null) return "redirect:/classroom/list";
        model.addAttribute("classroom", classroom);
        model.addAttribute("qna", instructorBoardService.getClassroomQnaDetail(postSn, classSn));
        model.addAttribute("editAnswer", editAnswer);
        return "classroom/detail-classroom-qna";
    }

    // Q&A 답변 등록/수정
    @PostMapping("/detail/{classSn}/qna/{postSn}/answer")
    public String qnaAnswer(@PathVariable Long classSn, @PathVariable Long postSn,
            @RequestParam String answCn, Authentication authentication) {
        if (getOwnedClassroom(classSn, authentication.getName()) == null) return "redirect:/classroom/list";
        instructorBoardService.answerClassroomQna(postSn, authentication.getName(), answCn);
        return "redirect:/classroom/detail/" + classSn + "/qna/" + postSn;
    }
}
