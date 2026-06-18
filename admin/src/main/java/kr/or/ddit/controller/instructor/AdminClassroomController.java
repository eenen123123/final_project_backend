package kr.or.ddit.controller.instructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
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
import org.springframework.web.multipart.MultipartFile;

import kr.or.ddit.finalProject.dto.assignment.AssignmentBoardDto;
import kr.or.ddit.finalProject.dto.classroom.ClassroomListResponse;
import kr.or.ddit.finalProject.dto.file.FileCtxType;
import kr.or.ddit.finalProject.dto.instructor.board.InstructorBoardDto;
import kr.or.ddit.finalProject.service.assignment.AssignmentBoardService;
import kr.or.ddit.finalProject.service.classroom.ClassroomHomeService;
import kr.or.ddit.finalProject.service.classroom.ClassroomService;
import kr.or.ddit.finalProject.service.file.FileUploadService;
import kr.or.ddit.finalProject.service.instructor.InstructorBoardService;
import kr.or.ddit.finalProject.util.TipTapSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/classroom")
@RequiredArgsConstructor
public class AdminClassroomController {

    private final ClassroomService classroomService;
    private final ClassroomHomeService classroomHomeService;
    private final InstructorBoardService instructorBoardService;
    private final AssignmentBoardService assignmentBoardService;
    private final FileUploadService fileUploadService;

    @GetMapping("/list")
    public String classroomList(Model model, Authentication authentication) {
        String instrUserId = authentication.getName();
        List<ClassroomListResponse> classroomList =
                classroomService.retrieveClassroomList(instrUserId);
        model.addAttribute("classroomList", classroomList);
        return "admin:/instructor/classroomList";
    }

    @GetMapping("/detail/{classSn}")
    public String classroomDetail(@PathVariable Long classSn, Model model) {
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();

        model.addAttribute("classroom", classroomService.retrieveClassroomDetail(classSn));
        model.addAttribute("weeklyData", classroomHomeService.retrieveWeeklyData(classSn));
        model.addAttribute("weeklyCompareText",
                classroomHomeService.retrieveWeeklyCompareText(classSn));
        model.addAttribute("achievements", classroomHomeService.retrieveAchievements(classSn));
        model.addAttribute("assignmentCount",
                classroomHomeService.retrieveUpcomingAssignmentCount(classSn));
        model.addAttribute("todayQuestion", classroomHomeService.retrieveTodayQuestion(classSn));
        model.addAttribute("unansweredQnaCount",
                instructorBoardService.getUnansweredQnaCount(classSn));
        model.addAttribute("pendingGradeCount",
                assignmentBoardService.getPendingGradeCount(classSn));
        model.addAttribute("inactiveStudentCount",
                classroomHomeService.retrieveInactiveStudentCount(classSn));
        model.addAttribute("calendarYear", year);
        model.addAttribute("calendarMonth", month);
        model.addAttribute("calendarPadding",
                classroomHomeService.retrieveCalendarPadding(year, month));
        model.addAttribute("calendarDays",
                classroomHomeService.retrieveCalendarDays(classSn, year, month));

        List<kr.or.ddit.finalProject.dto.instructor.board.InstructorBoardDto> notices =
                instructorBoardService.getClassroomNoticeList(classSn);
        model.addAttribute("recentNotice", notices.isEmpty() ? null : notices.get(0));

        return "classroom/home";
    }

    // ── 온라인 강의 ──────────────────────────────────────────────

    @GetMapping("/detail/{classSn}/lectures")
    public String lectureList(@PathVariable Long classSn, Model model) {
        model.addAttribute("classroom", classroomService.retrieveClassroomDetail(classSn));
        model.addAttribute("lectureList", classroomService.retrieveLecturesWithProgress(classSn));
        return "classroom/lectures";
    }

    // ── 공지사항 ──────────────────────────────────────────────────

    @GetMapping("/detail/{classSn}/notice")
    public String noticeList(@PathVariable Long classSn, Model model) {
        model.addAttribute("classroom", classroomService.retrieveClassroomDetail(classSn));
        model.addAttribute("noticeList", instructorBoardService.getClassroomNoticeList(classSn));
        return "classroom/list-classroom-notices";
    }

    @GetMapping("/detail/{classSn}/notice/{postSn}")
    public String noticeDetail(@PathVariable Long classSn, @PathVariable Long postSn, Model model) {
        model.addAttribute("classroom", classroomService.retrieveClassroomDetail(classSn));
        InstructorBoardDto notice = instructorBoardService.getClassroomNoticeDetail(postSn, classSn);
        model.addAttribute("notice", notice);
        if (notice != null && notice.getAtchFileId() != null) {
            model.addAttribute("attachedFiles",
                    fileUploadService.retrieveFilesByGroupId(notice.getAtchFileId().intValue()));
        } else {
            model.addAttribute("attachedFiles", Collections.emptyList());
        }
        return "classroom/detail-classroom-notice";
    }

    @GetMapping("/detail/{classSn}/notice/write")
    public String noticeWriteForm(@PathVariable Long classSn, Model model) {
        model.addAttribute("classroom", classroomService.retrieveClassroomDetail(classSn));
        return "classroom/form-classroom-notice";
    }

    @PostMapping("/detail/{classSn}/notice/write")
    public String noticeWrite(@PathVariable Long classSn, @ModelAttribute InstructorBoardDto dto,
            @RequestParam(required = false) List<MultipartFile> attachFiles,
            Authentication authentication) {
        String userId = authentication.getName();
        dto.setClassSn(classSn);
        dto.setInstrUserId(userId);
        dto.setWrtrUserId(userId);
        dto.setPostCn(TipTapSanitizer.clean(dto.getPostCn()));
        instructorBoardService.insertClassroomNotice(dto);

        if (hasFiles(attachFiles)) {
            int groupId = -1;
            try {
                groupId = fileUploadService.createFileGroup();
                for (MultipartFile f : attachFiles) {
                    if (!f.isEmpty()) {
                        fileUploadService.uploadFile(f, userId, groupId, FileCtxType.INSTRUCTOR, String.valueOf(groupId));
                    }
                }
                dto.setAtchFileId((long) groupId);
                instructorBoardService.updateClassroomNotice(dto);
            } catch (Exception e) {
                log.error("공지 파일 업로드 실패 — 등록된 공지 보상 삭제 (postSn={}, groupId={})", dto.getPostSn(), groupId, e);
                cleanupFileGroup(groupId, userId);
                instructorBoardService.deleteClassroomNotice(dto.getPostSn(), classSn);
                return "redirect:/classroom/detail/" + classSn + "/notice/write?error=fileUploadFailed";
            }
        }

        return "redirect:/classroom/detail/" + classSn + "/notice/" + dto.getPostSn();
    }

    @GetMapping("/detail/{classSn}/notice/{postSn}/edit")
    public String noticeEditForm(@PathVariable Long classSn, @PathVariable Long postSn, Model model) {
        model.addAttribute("classroom", classroomService.retrieveClassroomDetail(classSn));
        InstructorBoardDto editNotice = instructorBoardService.getClassroomNoticeDetail(postSn, classSn);
        model.addAttribute("editNotice", editNotice);
        if (editNotice != null && editNotice.getAtchFileId() != null) {
            model.addAttribute("existingFiles",
                    fileUploadService.retrieveFilesByGroupId(editNotice.getAtchFileId().intValue()));
        } else {
            model.addAttribute("existingFiles", Collections.emptyList());
        }
        return "classroom/form-classroom-notice";
    }

    @PostMapping("/detail/{classSn}/notice/{postSn}/edit")
    public String noticeEdit(@PathVariable Long classSn, @PathVariable Long postSn,
            @ModelAttribute InstructorBoardDto dto,
            @RequestParam(required = false) List<MultipartFile> attachFiles,
            Authentication authentication) {
        String userId = authentication.getName();
        dto.setPostSn(postSn);
        dto.setClassSn(classSn);
        dto.setWrtrUserId(userId);
        dto.setInstrUserId(userId);
        dto.setPostCn(TipTapSanitizer.clean(dto.getPostCn()));

        int newGroupId = -1;
        if (hasFiles(attachFiles)) {
            Long existingGroupId = dto.getAtchFileId();
            if (existingGroupId != null) {
                for (MultipartFile f : attachFiles) {
                    if (!f.isEmpty()) {
                        fileUploadService.uploadFile(f, userId, existingGroupId.intValue(),
                                FileCtxType.INSTRUCTOR, String.valueOf(existingGroupId));
                    }
                }
            } else {
                newGroupId = fileUploadService.createFileGroup();
                dto.setAtchFileId((long) newGroupId);
                for (MultipartFile f : attachFiles) {
                    if (!f.isEmpty()) {
                        fileUploadService.uploadFile(f, userId, newGroupId,
                                FileCtxType.INSTRUCTOR, String.valueOf(newGroupId));
                    }
                }
            }
        }

        try {
            instructorBoardService.updateClassroomNotice(dto);
        } catch (Exception e) {
            log.warn("공지 수정 실패 (groupId={}): {}", newGroupId, e.getMessage());
            cleanupFileGroup(newGroupId, userId);
        }

        return "redirect:/classroom/detail/" + classSn + "/notice/" + postSn;
    }

    private void cleanupFileGroup(int groupId, String userId) {
        if (groupId <= 0) return;
        try {
            fileUploadService.retrieveFilesByGroupId(groupId)
                    .forEach(f -> fileUploadService.removeFile(f.getAtchFileDtlSn(), userId));
        } catch (Exception ex) {
            log.warn("파일 그룹 정리 실패 (groupId={}): {}", groupId, ex.getMessage());
        }
    }

    @PostMapping("/detail/{classSn}/notice/{postSn}/delete")
    public String noticeDelete(@PathVariable Long classSn, @PathVariable Long postSn) {
        instructorBoardService.deleteClassroomNotice(postSn, classSn);
        return "redirect:/classroom/detail/" + classSn + "/notice";
    }

    // ── 과제 제출 ────────────────────────────────────────────────

    @GetMapping("/detail/{classSn}/assignments")
    public String assignmentList(@PathVariable Long classSn, Model model) {
        model.addAttribute("classroom", classroomService.retrieveClassroomDetail(classSn));
        model.addAttribute("assignmentList", assignmentBoardService.getAssignmentList(classSn));
        return "classroom/assignments";
    }

    @PostMapping("/detail/{classSn}/assignments/write")
    public String assignmentWrite(@PathVariable Long classSn,
            @ModelAttribute AssignmentBoardDto dto, Authentication authentication) {
        dto.setClassSn(classSn);
        dto.setRgtrUserId(authentication.getName());
        assignmentBoardService.insertAssignment(dto);
        return "redirect:/classroom/detail/" + classSn + "/assignments";
    }

    @GetMapping("/detail/{classSn}/assignments/{asgmtSn}")
    public String assignmentDetail(@PathVariable Long classSn, @PathVariable Long asgmtSn,
            Model model) {
        model.addAttribute("classroom", classroomService.retrieveClassroomDetail(classSn));
        model.addAttribute("assignment", assignmentBoardService.getAssignmentDetail(asgmtSn));
        model.addAttribute("submitList", assignmentBoardService.getSubmitList(asgmtSn, classSn));
        return "classroom/assignment-detail";
    }

    @PostMapping("/detail/{classSn}/assignments/{asgmtSn}/grade/{sbmtSn}")
    public String assignmentGrade(@PathVariable Long classSn, @PathVariable Long asgmtSn,
            @PathVariable Long sbmtSn, @RequestParam BigDecimal score,
            Authentication authentication) {
        assignmentBoardService.gradeSubmit(sbmtSn, score, authentication.getName());
        return "redirect:/classroom/detail/" + classSn + "/assignments/" + asgmtSn;
    }

    // ── 성적 관리 ────────────────────────────────────────────────

    @GetMapping("/detail/{classSn}/grades")
    public String gradeList(@PathVariable Long classSn, Model model) {
        model.addAttribute("classroom", classroomService.retrieveClassroomDetail(classSn));
        model.addAttribute("gradeList", classroomService.retrieveGradeList(classSn));
        return "classroom/grades";
    }

    // ── 수강생 목록 ──────────────────────────────────────────────

    @GetMapping("/detail/{classSn}/members")
    public String memberList(@PathVariable Long classSn, Model model) {
        model.addAttribute("classroom", classroomService.retrieveClassroomDetail(classSn));
        return "classroom/members";
    }

    // ── Q&A ──────────────────────────────────────────────────────

    @GetMapping("/detail/{classSn}/qna")
    public String qnaList(@PathVariable Long classSn, Model model) {
        model.addAttribute("classroom", classroomService.retrieveClassroomDetail(classSn));
        model.addAttribute("qnaList", instructorBoardService.getClassroomQnaList(classSn));
        return "classroom/list-classroom-qna";
    }

    @GetMapping("/detail/{classSn}/qna/{postSn}")
    public String qnaDetail(@PathVariable Long classSn, @PathVariable Long postSn,
            @RequestParam(required = false) boolean editAnswer, Model model) {
        model.addAttribute("classroom", classroomService.retrieveClassroomDetail(classSn));
        model.addAttribute("qna", instructorBoardService.getClassroomQnaDetail(postSn, classSn));
        model.addAttribute("editAnswer", editAnswer);
        return "classroom/detail-classroom-qna";
    }

    private boolean hasFiles(List<MultipartFile> files) {
        return files != null && files.stream().anyMatch(f -> !f.isEmpty());
    }

    @PostMapping("/detail/{classSn}/qna/{postSn}/answer")
    public String qnaAnswer(@PathVariable Long classSn, @PathVariable Long postSn,
            @RequestParam String answCn, Authentication authentication) {
        instructorBoardService.answerClassroomQna(postSn, authentication.getName(), answCn);
        return "redirect:/classroom/detail/" + classSn + "/qna/" + postSn;
    }
}
