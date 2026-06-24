package kr.or.ddit.controller.instructor;

import java.beans.PropertyEditorSupport;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import kr.or.ddit.finalProject.dto.assignment.AssignmentBoardDto;
import kr.or.ddit.finalProject.dto.classroom.ClassroomDetailResponse;
import kr.or.ddit.finalProject.service.assignment.AssignmentBoardService;
import kr.or.ddit.finalProject.service.classroom.ClassroomService;
import kr.or.ddit.finalProject.service.instructor.InstructorBoardService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/classroom")
public class AdminClassroomAssignmentController extends AbstractClassroomController {

    private static final int PAGE_SIZE = 10;

    public AdminClassroomAssignmentController(ClassroomService classroomService,
                                              AssignmentBoardService assignmentBoardService,
                                              InstructorBoardService instructorBoardService) {
        super(classroomService, assignmentBoardService, instructorBoardService);
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        binder.registerCustomEditor(LocalDateTime.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                setValue((text == null || text.trim().isEmpty()) ? null
                        : LocalDateTime.parse(text, fmt));
            }
        });
    }

    // 과제 목록 + 제출 현황 집계
    @GetMapping("/detail/{classSn}/assignments")
    public String assignmentList(@PathVariable Long classSn,
            @RequestParam(defaultValue = "1") int page,
            Model model, Authentication authentication) {
        ClassroomDetailResponse classroom = getOwnedClassroom(classSn, authentication.getName());
        if (classroom == null) return "redirect:/classroom/list";
        kr.or.ddit.finalProject.dto.common.PageResponse<AssignmentBoardDto> assignmentPage =
                assignmentBoardService.getAssignmentList(classSn, page, PAGE_SIZE);
        model.addAttribute("classroom", classroom);
        model.addAttribute("assignmentPage", assignmentPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", PAGE_SIZE);
        model.addAttribute("totalPages", (int) Math.ceil((double) assignmentPage.getTotalCount() / PAGE_SIZE));
        return "classroom/list-classroom-assignments";
    }

    // 과제 등록 폼
    @GetMapping("/detail/{classSn}/assignments/write")
    public String assignmentWriteForm(@PathVariable Long classSn, Model model,
            Authentication authentication) {
        ClassroomDetailResponse classroom = getOwnedClassroom(classSn, authentication.getName());
        if (classroom == null) return "redirect:/classroom/list";
        model.addAttribute("classroom", classroom);
        return "classroom/form-classroom-assignment";
    }

    // 과제 등록
    @PostMapping("/detail/{classSn}/assignments/write")
    public String assignmentWrite(@PathVariable Long classSn,
            @ModelAttribute AssignmentBoardDto dto, Authentication authentication) {
        if (getOwnedClassroom(classSn, authentication.getName()) == null)
            return "redirect:/classroom/list";
        dto.setClassSn(classSn);
        dto.setRgtrUserId(authentication.getName());
        assignmentBoardService.insertAssignment(dto);
        return "redirect:/classroom/detail/" + classSn + "/assignments";
    }

    // 과제 상세 + 수강생 제출 목록 (타 클래스 과제 접근 차단)
    @GetMapping("/detail/{classSn}/assignments/{asgmtSn}")
    public String assignmentDetail(@PathVariable Long classSn, @PathVariable Long asgmtSn,
            Model model, Authentication authentication) {
        ClassroomDetailResponse classroom = getOwnedClassroom(classSn, authentication.getName());
        if (classroom == null) return "redirect:/classroom/list";
        AssignmentBoardDto assignment = assignmentBoardService.getAssignmentDetail(asgmtSn);
        if (assignment == null || !classSn.equals(assignment.getClassSn()))
            return "redirect:/classroom/detail/" + classSn + "/assignments";
        model.addAttribute("classroom", classroom);
        model.addAttribute("assignment", assignment);
        List<kr.or.ddit.finalProject.dto.assignment.AssignmentSubmitDto> submitList =
                assignmentBoardService.getSubmitList(asgmtSn, classSn);
        long pendingCnt = submitList.stream()
                .filter(s -> s.getSbmtSn() != null && !"Y".equals(s.getGrddYn()))
                .count();
        long submittedCnt = submitList.stream()
                .filter(s -> s.getSbmtSn() != null)
                .count();
        model.addAttribute("submitList", submitList);
        model.addAttribute("pendingCnt", pendingCnt);
        model.addAttribute("submittedCnt", submittedCnt);
        model.addAttribute("now", LocalDateTime.now());
        return "classroom/detail-classroom-assignment";
    }

    // 과제 수정 폼
    @GetMapping("/detail/{classSn}/assignments/{asgmtSn}/edit")
    public String assignmentEditForm(@PathVariable Long classSn, @PathVariable Long asgmtSn,
            Model model, Authentication authentication) {
        ClassroomDetailResponse classroom = getOwnedClassroom(classSn, authentication.getName());
        if (classroom == null) return "redirect:/classroom/list";
        AssignmentBoardDto assignment = assignmentBoardService.getAssignmentDetail(asgmtSn);
        if (assignment == null || !classSn.equals(assignment.getClassSn()))
            return "redirect:/classroom/detail/" + classSn + "/assignments";
        model.addAttribute("classroom", classroom);
        model.addAttribute("editAssignment", assignment);
        return "classroom/form-classroom-assignment";
    }

    // 과제 수정 저장
    @PostMapping("/detail/{classSn}/assignments/{asgmtSn}/edit")
    public String assignmentEdit(@PathVariable Long classSn, @PathVariable Long asgmtSn,
            @ModelAttribute AssignmentBoardDto dto, Authentication authentication) {
        if (getOwnedClassroom(classSn, authentication.getName()) == null)
            return "redirect:/classroom/list";
        dto.setAsgmtSn(asgmtSn);
        dto.setClassSn(classSn);
        dto.setLastMdfrId(authentication.getName());
        assignmentBoardService.updateAssignment(dto);
        return "redirect:/classroom/detail/" + classSn + "/assignments/" + asgmtSn;
    }

    // 과제 삭제
    @PostMapping("/detail/{classSn}/assignments/{asgmtSn}/delete")
    public String assignmentDelete(@PathVariable Long classSn, @PathVariable Long asgmtSn,
            Authentication authentication) {
        if (getOwnedClassroom(classSn, authentication.getName()) == null)
            return "redirect:/classroom/list";
        AssignmentBoardDto assignment = assignmentBoardService.getAssignmentDetail(asgmtSn);
        if (assignment == null || !classSn.equals(assignment.getClassSn()))
            return "redirect:/classroom/detail/" + classSn + "/assignments";
        assignmentBoardService.deleteAssignment(asgmtSn, classSn);
        return "redirect:/classroom/detail/" + classSn + "/assignments";
    }

    // 과제 채점
    @PostMapping("/detail/{classSn}/assignments/{asgmtSn}/grade/{sbmtSn}")
    public String assignmentGrade(@PathVariable Long classSn, @PathVariable Long asgmtSn,
            @PathVariable Long sbmtSn, @RequestParam BigDecimal score,
            Authentication authentication) {
        if (getOwnedClassroom(classSn, authentication.getName()) == null)
            return "redirect:/classroom/list";
        if (score == null || score.compareTo(BigDecimal.ZERO) < 0 || score.compareTo(new BigDecimal(100)) > 0)
            return "redirect:/classroom/detail/" + classSn + "/assignments/" + asgmtSn + "?error=invalidScore";
        AssignmentBoardDto assignment = assignmentBoardService.getAssignmentDetail(asgmtSn);
        if (assignment == null || !classSn.equals(assignment.getClassSn()))
            return "redirect:/classroom/detail/" + classSn + "/assignments";
        int updated = assignmentBoardService.gradeSubmit(sbmtSn, asgmtSn, score, authentication.getName());
        if (updated == 0) log.warn("gradeSubmit 0 rows: classSn={} asgmtSn={} sbmtSn={}", classSn, asgmtSn, sbmtSn);
        return "redirect:/classroom/detail/" + classSn + "/assignments/" + asgmtSn;
    }
}
