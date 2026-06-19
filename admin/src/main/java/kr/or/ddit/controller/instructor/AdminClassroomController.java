package kr.or.ddit.controller.instructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import kr.or.ddit.finalProject.dto.assignment.AssignmentBoardDto;
import kr.or.ddit.finalProject.dto.assignment.AssignmentSubmitDto;
import kr.or.ddit.finalProject.dto.classroom.ClassroomDetailResponse;
import kr.or.ddit.finalProject.dto.classroom.ClassroomListResponse;
import kr.or.ddit.finalProject.dto.lecture.ClassroomLectureResponse;
import kr.or.ddit.finalProject.dto.instructor.board.InstructorBoardDto;
import kr.or.ddit.finalProject.service.assignment.AssignmentBoardService;
import kr.or.ddit.finalProject.service.classroom.ClassroomService;
import kr.or.ddit.finalProject.service.instructor.InstructorBoardService;
import kr.or.ddit.finalProject.service.lecture.LectureService;
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

    @ModelAttribute
    public void addTabBadges(@PathVariable(required = false) Long classSn, Model model) {
        if (classSn != null) {
            model.addAttribute("assignmentCount",
                    assignmentBoardService.getPendingGradeCount(classSn));
            model.addAttribute("unansweredQnaCount",
                    instructorBoardService.getUnansweredQnaCount(classSn));
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

    private ClassroomDetailResponse getOwnedClassroom(Long classSn, String userId) {
        try {
            ClassroomDetailResponse classroom = classroomService.retrieveClassroomDetail(classSn);
            return userId.equals(classroom.getInstrUserId()) ? classroom : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // 클래스룸 홈 — 진도율·마감과제·최근제출·캘린더 등 대시보드
    @GetMapping("/detail/{classSn}")
    public String classroomDetail(@PathVariable Long classSn, Model model,
            Authentication authentication) {
        ClassroomDetailResponse ownedClassroom = getOwnedClassroom(classSn, authentication.getName());
        if (ownedClassroom == null) return "redirect:/classroom/list";
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();

        model.addAttribute("classroom", ownedClassroom);
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

        List<InstructorBoardDto> notices =
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
    public String lectureList(@PathVariable Long classSn, Model model, Authentication authentication) {
        ClassroomDetailResponse classroom = getOwnedClassroom(classSn, authentication.getName());
        if (classroom == null) return "redirect:/classroom/list";
        model.addAttribute("classroom", classroom);
        model.addAttribute("lectureList", classroomService.retrieveLecturesWithProgress(classSn));
        return "classroom/list-classroom-lectures";
    }

    // 강의 상세 + 수강생 개인별 진도율 (강좌 소속 검증 포함)
    @GetMapping("/detail/{classSn}/lectures/{lectureSn}")
    public String lectureDetail(@PathVariable Long classSn, @PathVariable Long lectureSn, Model model,
            Authentication authentication) {
        ClassroomDetailResponse classroom = getOwnedClassroom(classSn, authentication.getName());
        if (classroom == null) return "redirect:/classroom/list";
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
        ClassroomDetailResponse classroom = getOwnedClassroom(classSn, authentication.getName());
        if (classroom == null) return ResponseEntity.status(403).body("forbidden");
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
        ClassroomDetailResponse classroom = getOwnedClassroom(classSn, authentication.getName());
        if (classroom == null) return ResponseEntity.status(403).body("forbidden");
        kr.or.ddit.finalProject.dto.lecture.LectureDto lecture = lectureService.retrieveLectureBySn(lectureSn);
        if (lecture == null || !lecture.getCourseSn().equals(classroom.getCourseSn())) {
            return ResponseEntity.badRequest().body("invalid");
        }
        lectureService.toggleLockYn(lectureSn, authentication.getName());
        return ResponseEntity.ok("ok");
    }

    // ── 성적 관리 ────────────────────────────────────────────────

    // 수강생별 성적 목록
    @GetMapping("/detail/{classSn}/grades")
    public String gradeList(@PathVariable Long classSn, Model model, Authentication authentication) {
        ClassroomDetailResponse classroom = getOwnedClassroom(classSn, authentication.getName());
        if (classroom == null) return "redirect:/classroom/list";
        model.addAttribute("classroom", classroom);
        model.addAttribute("gradeList", classroomService.retrieveGradeList(classSn));
        return "classroom/list-classroom-grades";
    }

    // ── 수강생 목록 ──────────────────────────────────────────────

    // 수강생 목록
    @GetMapping("/detail/{classSn}/members")
    public String memberList(@PathVariable Long classSn, Model model, Authentication authentication) {
        ClassroomDetailResponse classroom = getOwnedClassroom(classSn, authentication.getName());
        if (classroom == null) return "redirect:/classroom/list";
        model.addAttribute("classroom", classroom);
        return "classroom/list-classroom-members";
    }
}
