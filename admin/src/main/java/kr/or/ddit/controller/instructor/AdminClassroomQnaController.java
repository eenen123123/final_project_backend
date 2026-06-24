package kr.or.ddit.controller.instructor;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import kr.or.ddit.finalProject.service.assignment.AssignmentBoardService;
import kr.or.ddit.finalProject.service.classroom.ClassroomService;
import kr.or.ddit.finalProject.service.instructor.InstructorBoardService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/classroom")
public class AdminClassroomQnaController extends AbstractClassroomController {

    private static final int PAGE_SIZE = 10;

    public AdminClassroomQnaController(ClassroomService classroomService,
                                       AssignmentBoardService assignmentBoardService,
                                       InstructorBoardService instructorBoardService) {
        super(classroomService, assignmentBoardService, instructorBoardService);
    }

    // Q&A 목록
    @GetMapping("/detail/{classSn}/qna")
    public String qnaList(@PathVariable Long classSn,
            @RequestParam(defaultValue = "1") int page,
            Model model, Authentication authentication) {
        kr.or.ddit.finalProject.dto.classroom.ClassroomDetailResponse classroom = getOwnedClassroom(classSn, authentication.getName());
        if (classroom == null) return "redirect:/classroom/list";
        kr.or.ddit.finalProject.dto.common.PageResponse<kr.or.ddit.finalProject.dto.classroom.ClassroomQnaDto> qnaPage =
                instructorBoardService.getClassroomQnaList(classSn, page, PAGE_SIZE);
        model.addAttribute("classroom", classroom);
        model.addAttribute("qnaPage", qnaPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", PAGE_SIZE);
        model.addAttribute("totalPages", (int) Math.ceil((double) qnaPage.getTotalCount() / PAGE_SIZE));
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
