package kr.or.ddit.controller.instructor;

import java.beans.PropertyEditorSupport;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
import org.springframework.web.multipart.MultipartFile;

import kr.or.ddit.finalProject.dto.assignment.AssignmentBoardDto;
import kr.or.ddit.finalProject.dto.assignment.AssignmentSubmitDto;
import kr.or.ddit.finalProject.dto.classroom.ClassroomListResponse;
import kr.or.ddit.finalProject.dto.lecture.ClassroomLectureResponse;
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

    /** 클래스룸 상세 탭 nav 뱃지 — classSn이 있는 모든 요청에 자동 주입 */
    @ModelAttribute
    public void addTabBadges(@PathVariable(required = false) Long classSn, Model model) {
        if (classSn != null) {
            model.addAttribute("assignmentCount",
                    classroomService.retrieveUpcomingAssignmentCount(classSn));
            model.addAttribute("unansweredQnaCount",
                    instructorBoardService.getUnansweredQnaCount(classSn));
        }
    }

    /** datetime-local 빈 문자열 → null 바인딩 (마감일 미입력 허용) */
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

    /** classSn이 현재 인증 사용자의 클래스룸인지 확인 — 없거나 소유자 불일치 시 null 반환 */
    private ClassroomDetailResponse getOwnedClassroom(Long classSn, String userId) {
        try {
            ClassroomDetailResponse classroom = classroomService.retrieveClassroomDetail(classSn);
            return userId.equals(classroom.getInstrUserId()) ? classroom : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // 내 클래스룸 목록 조회
    @GetMapping("/list")
    public String classroomList(Model model, Authentication authentication) {
        String instrUserId = authentication.getName();
        List<ClassroomListResponse> classroomList =
                classroomService.retrieveClassroomList(instrUserId);
        model.addAttribute("classroomList", classroomList);
        return "admin:/instructor/classroomList";
    }

    // 클래스룸 홈 — 진도율·마감과제·최근제출·캘린더 등 대시보드
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
        model.addAttribute("todayQuestion", classroomService.retrieveTodayQuestion(classSn));
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

        // 강좌 진도율 요약
        List<ClassroomLectureResponse> lectures = classroomService.retrieveLecturesWithProgress(classSn);
        int totalLectures = lectures.size();
        int avgCompletionPct = 0;
        if (totalLectures > 0) {
            long validCount = lectures.stream().filter(l -> l.getTotMemberCnt() > 0).count();
            if (validCount > 0) {
                avgCompletionPct = (int) lectures.stream()
                        .filter(l -> l.getTotMemberCnt() > 0)
                        .mapToLong(l -> (long) l.getCmplCnt() * 100 / l.getTotMemberCnt())
                        .average()
                        .orElse(0);
            }
        }
        model.addAttribute("totalLectures", totalLectures);
        model.addAttribute("avgCompletionPct", avgCompletionPct);

        // 마감 임박 과제 (오늘~모레) — getAssignmentList 한 번 호출로 count와 필터 모두 처리
        List<AssignmentBoardDto> allAssignments = assignmentBoardService.getAssignmentList(classSn);
        LocalDateTime nowDt = LocalDateTime.now();
        LocalDateTime threshold = now.plusDays(2).atTime(23, 59, 59);
        List<AssignmentBoardDto> deadlineSoonList = allAssignments.stream()
                .filter(a -> a.getSbmtDdlnDt() != null)
                .filter(a -> !a.getSbmtDdlnDt().isBefore(nowDt))
                .filter(a -> !a.getSbmtDdlnDt().isAfter(threshold))
                .sorted(Comparator.comparing(AssignmentBoardDto::getSbmtDdlnDt))
                .map(a -> {
                    a.setDaysUntil((int) ChronoUnit.DAYS.between(now, a.getSbmtDdlnDt().toLocalDate()));
                    return a;
                })
                .collect(Collectors.toList());
        model.addAttribute("totalAssignmentCount", allAssignments.size());
        model.addAttribute("deadlineSoonList", deadlineSoonList);

        // 최근 제출된 과제 (최대 5건)
        List<AssignmentSubmitDto> recentSubmits = assignmentBoardService.getRecentSubmits(classSn, 5);
        model.addAttribute("recentSubmits", recentSubmits);

        return "classroom/home-classroom";
    }

    // ── 온라인 강의 ──────────────────────────────────────────────

    // 강의 목록 + 수강생별 진도 현황
    @GetMapping("/detail/{classSn}/lectures")
    public String lectureList(@PathVariable Long classSn, Model model) {
        model.addAttribute("classroom", classroomService.retrieveClassroomDetail(classSn));
        model.addAttribute("lectureList", classroomService.retrieveLecturesWithProgress(classSn));
        return "classroom/list-classroom-lectures";
    }

    // 강의 상세 + 수강생 개인별 진도율 (강좌 소속 검증 포함)
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

    // 강의 공개/비공개 토글
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

    // 강의 잠금/잠금해제 토글
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

    // 공지사항 목록
    @GetMapping("/detail/{classSn}/notice")
    public String noticeList(@PathVariable Long classSn, Model model) {
        model.addAttribute("classroom", classroomService.retrieveClassroomDetail(classSn));
        model.addAttribute("noticeList", instructorBoardService.getClassroomNoticeList(classSn));
        return "classroom/list-classroom-notices";
    }

    // 공지사항 상세 (첨부파일 포함)
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

    // 공지사항 작성 폼
    @GetMapping("/detail/{classSn}/notice/write")
    public String noticeWriteForm(@PathVariable Long classSn, Model model) {
        model.addAttribute("classroom", classroomService.retrieveClassroomDetail(classSn));
        return "classroom/form-classroom-notice";
    }

    // 공지사항 등록 (파일 업로드 실패 시 공지 롤백)
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

    // 공지사항 수정 폼 (기존 첨부파일 목록 포함)
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

    // 공지사항 수정 저장 (파일 추가·그룹 신규 생성 모두 처리, 실패 시 신규 파일 롤백)
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

    // 공지사항 첨부파일 단건 삭제 (공지 소유권 검증 후 제거)
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

    // 공지사항 삭제
    @PostMapping("/detail/{classSn}/notice/{postSn}/delete")
    public String noticeDelete(@PathVariable Long classSn, @PathVariable Long postSn) {
        instructorBoardService.deleteClassroomNotice(postSn, classSn);
        return "redirect:/classroom/detail/" + classSn + "/notice";
    }

    // ── 과제 제출 ────────────────────────────────────────────────

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
        if (assignment == null || !classSn.equals(assignment.getClassSn())) {
            return "redirect:/classroom/detail/" + classSn + "/assignments";
        }
        model.addAttribute("classroom", classroomService.retrieveClassroomDetail(classSn));
        model.addAttribute("assignment", assignment);
        model.addAttribute("submitList", assignmentBoardService.getSubmitList(asgmtSn, classSn));
        return "classroom/assignment-detail";
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
        AssignmentBoardDto existing = assignmentBoardService.getAssignmentDetail(asgmtSn);
        if (existing == null || !classSn.equals(existing.getClassSn()))
            return "redirect:/classroom/detail/" + classSn + "/assignments";
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

    // 과제 채점 — 강사 소유권 및 과제 소속 검증 후 채점
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

    // ── 성적 관리 ────────────────────────────────────────────────

    // 수강생별 성적 목록
    @GetMapping("/detail/{classSn}/grades")
    public String gradeList(@PathVariable Long classSn, Model model) {
        model.addAttribute("classroom", classroomService.retrieveClassroomDetail(classSn));
        model.addAttribute("gradeList", classroomService.retrieveGradeList(classSn));
        return "classroom/list-classroom-grades";
    }

    // ── 수강생 목록 ──────────────────────────────────────────────

    // 수강생 목록
    @GetMapping("/detail/{classSn}/members")
    public String memberList(@PathVariable Long classSn, Model model) {
        model.addAttribute("classroom", classroomService.retrieveClassroomDetail(classSn));
        return "classroom/list-classroom-members";
    }

    // ── Q&A ──────────────────────────────────────────────────────

    // Q&A 목록
    @GetMapping("/detail/{classSn}/qna")
    public String qnaList(@PathVariable Long classSn, Model model) {
        model.addAttribute("classroom", classroomService.retrieveClassroomDetail(classSn));
        model.addAttribute("qnaList", instructorBoardService.getClassroomQnaList(classSn));
        return "classroom/list-classroom-qna";
    }

    // Q&A 상세 (editAnswer=true면 답변 수정 모드)
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

    // Q&A 답변 등록/수정
    @PostMapping("/detail/{classSn}/qna/{postSn}/answer")
    public String qnaAnswer(@PathVariable Long classSn, @PathVariable Long postSn,
            @RequestParam String answCn, Authentication authentication) {
        instructorBoardService.answerClassroomQna(postSn, authentication.getName(), answCn);
        return "redirect:/classroom/detail/" + classSn + "/qna/" + postSn;
    }
}
