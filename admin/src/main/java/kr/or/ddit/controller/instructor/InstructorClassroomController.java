package kr.or.ddit.controller.instructor;

import java.time.LocalDate;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

import kr.or.ddit.finalProject.dto.assignment.AssignmentBoardDto;
import kr.or.ddit.finalProject.dto.classroom.ClassroomDetailResponse;
import kr.or.ddit.finalProject.dto.classroom.ClassroomListResponse;
import kr.or.ddit.finalProject.dto.instructor.InstructorBoardDto;
import kr.or.ddit.finalProject.service.assignment.AssignmentBoardService;
import kr.or.ddit.finalProject.service.classroom.ClassroomHomeService;
import kr.or.ddit.finalProject.service.classroom.ClassroomService;
import kr.or.ddit.finalProject.service.instructor.InstructorBoardService;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/instructor/classroom")
@RequiredArgsConstructor
public class InstructorClassroomController {

    private final ClassroomService classroomService;
    private final ClassroomHomeService classroomHomeService;
    private final InstructorBoardService instructorBoardService;
    private final AssignmentBoardService assignmentBoardService;

    @GetMapping("/list")
    public String classroomList(Model model, Authentication authentication) {
        String instrUserId = authentication.getName();
        List<ClassroomListResponse> classroomList = classroomService.retrieveClassroomList(instrUserId);
        model.addAttribute("classroomList", classroomList);
        return "admin:/instructor/classroomList";
    }

    @GetMapping("/detail/{classSn}")
    public String classroomDetail(@PathVariable Long classSn, Model model) {
        ClassroomDetailResponse classroom = classroomService.retrieveClassroomDetail(classSn);
        model.addAttribute("classroom", classroom);

        LocalDate now = LocalDate.now();
        int year  = now.getYear();
        int month = now.getMonthValue();

        model.addAttribute("calendarYear",    year);
        model.addAttribute("calendarMonth",   month);
        model.addAttribute("calendarPadding", classroomHomeService.retrieveCalendarPadding(year, month));
        model.addAttribute("calendarDays",    classroomHomeService.retrieveCalendarDays(classSn, year, month));
        model.addAttribute("weeklyData",      classroomHomeService.retrieveWeeklyData(classSn));
        model.addAttribute("weeklyCompareText", classroomHomeService.retrieveWeeklyCompareText(classSn));
        model.addAttribute("achievements",    classroomHomeService.retrieveAchievements(classSn));
        model.addAttribute("assignmentCount", classroomHomeService.retrieveUpcomingAssignmentCount(classSn));
        model.addAttribute("todayQuestion",   classroomHomeService.retrieveTodayQuestion(classSn));
        model.addAttribute("noticeCount",     0);  // 클래스별 공지 테이블 미구현

        return "instructor/classroom-home";
    }

    // ── 공지사항 ──────────────────────────────────────────────────

    @GetMapping("/detail/{classSn}/notice")
    public String noticeList(@PathVariable Long classSn, Model model) {
        model.addAttribute("classroom", classroomService.retrieveClassroomDetail(classSn));
        model.addAttribute("noticeList", instructorBoardService.getClassroomNoticeList(classSn));
        return "instructor/classroom-notice";
    }

    @GetMapping("/detail/{classSn}/notice/{postSn}")
    public String noticeDetail(@PathVariable Long classSn, @PathVariable Long postSn, Model model) {
        model.addAttribute("classroom", classroomService.retrieveClassroomDetail(classSn));
        model.addAttribute("notice", instructorBoardService.getClassroomNoticeDetail(postSn, classSn));
        return "instructor/classroom-notice-detail";
    }

    @PostMapping("/detail/{classSn}/notice/write")
    public String noticeWrite(@PathVariable Long classSn,
                              @ModelAttribute InstructorBoardDto dto,
                              Authentication authentication) {
        dto.setClassSn(classSn);
        dto.setInstrUserId(authentication.getName());
        dto.setWrtrUserId(authentication.getName());
        instructorBoardService.insertClassroomNotice(dto);
        return "redirect:/instructor/classroom/detail/" + classSn + "/notice";
    }

    @PostMapping("/detail/{classSn}/notice/{postSn}/delete")
    public String noticeDelete(@PathVariable Long classSn, @PathVariable Long postSn) {
        instructorBoardService.deleteClassroomNotice(postSn, classSn);
        return "redirect:/instructor/classroom/detail/" + classSn + "/notice";
    }

    // ── 과제 제출 ────────────────────────────────────────────────

    @GetMapping("/detail/{classSn}/assignments")
    public String assignmentList(@PathVariable Long classSn, Model model) {
        model.addAttribute("classroom", classroomService.retrieveClassroomDetail(classSn));
        model.addAttribute("assignmentList", assignmentBoardService.getAssignmentList(classSn));
        return "instructor/classroom-assignments";
    }

    @PostMapping("/detail/{classSn}/assignments/write")
    public String assignmentWrite(@PathVariable Long classSn,
                                  @ModelAttribute AssignmentBoardDto dto,
                                  Authentication authentication) {
        dto.setClassSn(classSn);
        dto.setRgtrUserId(authentication.getName());
        assignmentBoardService.insertAssignment(dto);
        return "redirect:/instructor/classroom/detail/" + classSn + "/assignments";
    }

    @GetMapping("/detail/{classSn}/assignments/{asgmtSn}")
    public String assignmentDetail(@PathVariable Long classSn, @PathVariable Long asgmtSn, Model model) {
        model.addAttribute("classroom", classroomService.retrieveClassroomDetail(classSn));
        model.addAttribute("assignment", assignmentBoardService.getAssignmentDetail(asgmtSn));
        model.addAttribute("submitList", assignmentBoardService.getSubmitList(asgmtSn, classSn));
        return "instructor/classroom-assignment-detail";
    }

    @PostMapping("/detail/{classSn}/assignments/{asgmtSn}/grade/{sbmtSn}")
    public String assignmentGrade(@PathVariable Long classSn, @PathVariable Long asgmtSn,
                                  @PathVariable Long sbmtSn,
                                  @RequestParam BigDecimal score,
                                  Authentication authentication) {
        assignmentBoardService.gradeSubmit(sbmtSn, score, authentication.getName());
        return "redirect:/instructor/classroom/detail/" + classSn + "/assignments/" + asgmtSn;
    }

    // ── 성적 관리 ────────────────────────────────────────────────

    @GetMapping("/detail/{classSn}/grades")
    public String gradeList(@PathVariable Long classSn, Model model) {
        model.addAttribute("classroom", classroomService.retrieveClassroomDetail(classSn));
        model.addAttribute("gradeList", classroomService.retrieveGradeList(classSn));
        return "instructor/classroom-grades";
    }

    // ── 수강생 목록 ──────────────────────────────────────────────

    @GetMapping("/detail/{classSn}/members")
    public String memberList(@PathVariable Long classSn, Model model) {
        model.addAttribute("classroom", classroomService.retrieveClassroomDetail(classSn));
        return "instructor/classroom-members";
    }

    // ── Q&A ──────────────────────────────────────────────────────

    @GetMapping("/detail/{classSn}/qna")
    public String qnaList(@PathVariable Long classSn, Model model) {
        model.addAttribute("classroom", classroomService.retrieveClassroomDetail(classSn));
        model.addAttribute("qnaList", instructorBoardService.getClassroomQnaList(classSn));
        return "instructor/classroom-qna";
    }

    @GetMapping("/detail/{classSn}/qna/{postSn}")
    public String qnaDetail(@PathVariable Long classSn, @PathVariable Long postSn, Model model) {
        model.addAttribute("classroom", classroomService.retrieveClassroomDetail(classSn));
        model.addAttribute("qna", instructorBoardService.getClassroomQnaDetail(postSn, classSn));
        return "instructor/classroom-qna-detail";
    }

    @PostMapping("/detail/{classSn}/qna/write")
    public String qnaWrite(@PathVariable Long classSn,
                           @ModelAttribute InstructorBoardDto dto,
                           Authentication authentication) {
        dto.setClassSn(classSn);
        dto.setInstrUserId(authentication.getName());
        dto.setWrtrUserId(authentication.getName());
        instructorBoardService.insertClassroomQna(dto);
        return "redirect:/instructor/classroom/detail/" + classSn + "/qna";
    }

    @PostMapping("/detail/{classSn}/qna/{postSn}/answer")
    public String qnaAnswer(@PathVariable Long classSn, @PathVariable Long postSn,
                            @RequestParam String answCn,
                            Authentication authentication) {
        instructorBoardService.answerClassroomQna(postSn, authentication.getName(), answCn);
        return "redirect:/instructor/classroom/detail/" + classSn + "/qna/" + postSn;
    }
}
