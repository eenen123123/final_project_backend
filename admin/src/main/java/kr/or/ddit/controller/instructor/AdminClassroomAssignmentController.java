package kr.or.ddit.controller.instructor;

import java.beans.PropertyEditorSupport;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/classroom")
@RequiredArgsConstructor
public class AdminClassroomAssignmentController {

    private final ClassroomService classroomService;
    private final AssignmentBoardService assignmentBoardService;
    private final InstructorBoardService instructorBoardService;

    @ModelAttribute
    public void addTabBadges(@PathVariable(required = false) Long classSn, Model model) {
        if (classSn != null) {
            model.addAttribute("assignmentCount",
                    classroomService.retrieveUpcomingAssignmentCount(classSn));
            model.addAttribute("unansweredQnaCount",
                    instructorBoardService.getUnansweredQnaCount(classSn));
        }
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

    private ClassroomDetailResponse getOwnedClassroom(Long classSn, String userId) {
        try {
            ClassroomDetailResponse classroom = classroomService.retrieveClassroomDetail(classSn);
            return userId.equals(classroom.getInstrUserId()) ? classroom : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // 과제 목록 + 제출 현황 집계
    @GetMapping("/detail/{classSn}/assignments")
    public String assignmentList(@PathVariable Long classSn, Model model) {
        model.addAttribute("classroom", classroomService.retrieveClassroomDetail(classSn));
        model.addAttribute("assignmentList", assignmentBoardService.getAssignmentList(classSn));
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
            Model model) {
        AssignmentBoardDto assignment = assignmentBoardService.getAssignmentDetail(asgmtSn);
        if (assignment == null || !classSn.equals(assignment.getClassSn()))
            return "redirect:/classroom/detail/" + classSn + "/assignments";
        model.addAttribute("classroom", classroomService.retrieveClassroomDetail(classSn));
        model.addAttribute("assignment", assignment);
        model.addAttribute("submitList", assignmentBoardService.getSubmitList(asgmtSn, classSn));
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
        AssignmentBoardDto assignment = assignmentBoardService.getAssignmentDetail(asgmtSn);
        if (assignment == null || !classSn.equals(assignment.getClassSn()))
            return "redirect:/classroom/detail/" + classSn + "/assignments";
        int updated = assignmentBoardService.gradeSubmit(sbmtSn, asgmtSn, score, authentication.getName());
        if (updated == 0) log.warn("gradeSubmit 0 rows: classSn={} asgmtSn={} sbmtSn={}", classSn, asgmtSn, sbmtSn);
        return "redirect:/classroom/detail/" + classSn + "/assignments/" + asgmtSn;
    }
}
