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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.or.ddit.finalProject.dto.classroom.ClassroomDetailResponse;
import kr.or.ddit.finalProject.service.assignment.AssignmentBoardService;
import kr.or.ddit.finalProject.service.classroom.ClassroomService;
import kr.or.ddit.finalProject.service.file.FileUploadService;
import kr.or.ddit.finalProject.service.instructor.InstructorBoardService;
import kr.or.ddit.finalProject.service.lecture.LectureService;
import kr.or.ddit.finalProject.util.TipTapSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/classroom")
@RequiredArgsConstructor
public class AdminClassroomController {

    private final ClassroomService classroomService;
    private final LectureService lectureService;
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
        model.addAttribute("weeklyData", classroomService.retrieveWeeklyData(classSn));
        model.addAttribute("weeklyCompareText",
                classroomService.retrieveWeeklyCompareText(classSn));
        model.addAttribute("achievements", classroomService.retrieveAchievements(classSn));
        model.addAttribute("assignmentCount",
                classroomService.retrieveUpcomingAssignmentCount(classSn));
        model.addAttribute("todayQuestion", classroomService.retrieveTodayQuestion(classSn));
        model.addAttribute("unansweredQnaCount",
                instructorBoardService.getUnansweredQnaCount(classSn));
        model.addAttribute("pendingGradeCount",
                assignmentBoardService.getPendingGradeCount(classSn));
        model.addAttribute("inactiveStudentCount",
                classroomService.retrieveInactiveStudentCount(classSn));
        model.addAttribute("calendarYear", year);
        model.addAttribute("calendarMonth", month);
        model.addAttribute("calendarPadding",
                classroomService.retrieveCalendarPadding(year, month));
        model.addAttribute("calendarDays",
                classroomService.retrieveCalendarDays(classSn, year, month));

        List<kr.or.ddit.finalProject.dto.instructor.board.InstructorBoardDto> notices =
                instructorBoardService.getClassroomNoticeList(classSn);
        model.addAttribute("recentNotice", notices.isEmpty() ? null : notices.get(0));

        return "classroom/home-classroom";
    }

    // ── 온라인 강의 ──────────────────────────────────────────────

    @GetMapping("/detail/{classSn}/lectures")
    public String lectureList(@PathVariable Long classSn, Model model) {
        model.addAttribute("classroom", classroomService.retrieveClassroomDetail(classSn));
        model.addAttribute("lectureList", classroomService.retrieveLecturesWithProgress(classSn));
        return "classroom/list-classroom-lectures";
    }

    @GetMapping("/detail/{classSn}/lectures/{lectureSn}")
    public String lectureDetail(@PathVariable Long classSn, @PathVariable Long lectureSn, Model model) {
        ClassroomDetailResponse classroom = classroomService.retrieveClassroomDetail(classSn);
        kr.or.ddit.finalProject.dto.lecture.LectureDto lecture = lectureService.retrieveLectureBySn(lectureSn);
        if (lecture == null || !lecture.getCourseSn().equals(classroom.getCourseSn())) {
            return "redirect:/classroom/detail/" + classSn + "/lectures";
        }
        model.addAttribute("classroom", classroom);
        model.addAttribute("lecture", lecture);
        model.addAttribute("studentProgress",
                lectureService.retrieveStudentProgressByLecture(classSn, lectureSn));
        return "classroom/detail-classroom-lecture";
    }

    @PostMapping("/detail/{classSn}/lectures/{lectureSn}/toggle-opnn")
    @ResponseBody
    public ResponseEntity<String> toggleOpnn(@PathVariable Long classSn, @PathVariable Long lectureSn,
            Authentication authentication) {
        ClassroomDetailResponse classroom = classroomService.retrieveClassroomDetail(classSn);
        kr.or.ddit.finalProject.dto.lecture.LectureDto lecture = lectureService.retrieveLectureBySn(lectureSn);
        if (lecture == null || !lecture.getCourseSn().equals(classroom.getCourseSn())) {
            return ResponseEntity.badRequest().body("invalid");
        }
        lectureService.toggleOpnnYn(lectureSn, authentication.getName());
        return ResponseEntity.ok("ok");
    }

    @PostMapping("/detail/{classSn}/lectures/{lectureSn}/toggle-lock")
    @ResponseBody
    public ResponseEntity<String> toggleLock(@PathVariable Long classSn, @PathVariable Long lectureSn,
            Authentication authentication) {
        ClassroomDetailResponse classroom = classroomService.retrieveClassroomDetail(classSn);
        kr.or.ddit.finalProject.dto.lecture.LectureDto lecture = lectureService.retrieveLectureBySn(lectureSn);
        if (lecture == null || !lecture.getCourseSn().equals(classroom.getCourseSn())) {
            return ResponseEntity.badRequest().body("invalid");
        }
        lectureService.toggleLockYn(lectureSn, authentication.getName());
        return ResponseEntity.ok("ok");
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
        List<Integer> addedFileSns = Collections.emptyList();
        String editUrl = "redirect:/classroom/detail/" + classSn + "/notice/" + postSn;
        String editErrorUrl = editUrl + "/edit?error=fileUploadFailed";
        if (hasFiles(attachFiles)) {
            Long existingGroupId = dto.getAtchFileId();
            if (existingGroupId != null) {
                List<Integer> before = fileUploadService.retrieveFilesByGroupId(existingGroupId.intValue())
                        .stream().map(f -> f.getAtchFileDtlSn()).toList();
                try {
                    for (MultipartFile f : attachFiles) {
                        if (!f.isEmpty()) {
                            fileUploadService.uploadFile(f, userId, existingGroupId.intValue(),
                                    FileCtxType.INSTRUCTOR, String.valueOf(existingGroupId));
                        }
                    }
                } catch (Exception e) {
                    log.error("공지 수정 파일 업로드 실패 (postSn={}, groupId={})", postSn, existingGroupId, e);
                    fileUploadService.retrieveFilesByGroupId(existingGroupId.intValue())
                            .stream().map(f -> f.getAtchFileDtlSn())
                            .filter(sn -> !before.contains(sn))
                            .forEach(sn -> fileUploadService.removeFile(sn, userId));
                    return editErrorUrl;
                }
                addedFileSns = fileUploadService.retrieveFilesByGroupId(existingGroupId.intValue())
                        .stream().map(f -> f.getAtchFileDtlSn())
                        .filter(sn -> !before.contains(sn)).toList();
            } else {
                try {
                    newGroupId = fileUploadService.createFileGroup();
                    dto.setAtchFileId((long) newGroupId);
                    for (MultipartFile f : attachFiles) {
                        if (!f.isEmpty()) {
                            fileUploadService.uploadFile(f, userId, newGroupId,
                                    FileCtxType.INSTRUCTOR, String.valueOf(newGroupId));
                        }
                    }
                } catch (Exception e) {
                    log.error("공지 수정 파일 업로드 실패 — 새 그룹 정리 (postSn={}, groupId={})", postSn, newGroupId, e);
                    cleanupFileGroup(newGroupId, userId);
                    return editErrorUrl;
                }
            }
        }

        try {
            instructorBoardService.updateClassroomNotice(dto);
        } catch (Exception e) {
            log.error("공지 수정 DB 반영 실패 (postSn={}, groupId={})", postSn, newGroupId, e);
            addedFileSns.forEach(sn -> fileUploadService.removeFile(sn, userId));
            cleanupFileGroup(newGroupId, userId);
            return editErrorUrl;
        }

        return editUrl;
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

    @PostMapping("/detail/{classSn}/notice/{postSn}/file/{fileDtlSn}/delete")
    public String noticeFileDelete(@PathVariable Long classSn, @PathVariable Long postSn,
            @PathVariable Integer fileDtlSn, Authentication authentication) {
        String editUrl = "redirect:/classroom/detail/" + classSn + "/notice/" + postSn + "/edit";
        InstructorBoardDto notice = instructorBoardService.getClassroomNoticeDetail(postSn, classSn);
        if (notice == null || notice.getAtchFileId() == null) {
            return editUrl;
        }
        boolean owned = fileUploadService.retrieveFilesByGroupId(notice.getAtchFileId().intValue())
                .stream().anyMatch(f -> fileDtlSn.equals(f.getAtchFileDtlSn()));
        if (!owned) {
            log.warn("파일 소유권 불일치 — 삭제 거부 (postSn={}, fileDtlSn={})", postSn, fileDtlSn);
            return editUrl;
        }
        fileUploadService.removeFile(fileDtlSn, authentication.getName());
        return editUrl;
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
        return "classroom/list-classroom-assignments";
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
        return "classroom/list-classroom-grades";
    }

    // ── 수강생 목록 ──────────────────────────────────────────────

    @GetMapping("/detail/{classSn}/members")
    public String memberList(@PathVariable Long classSn, Model model) {
        model.addAttribute("classroom", classroomService.retrieveClassroomDetail(classSn));
        return "classroom/list-classroom-members";
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
