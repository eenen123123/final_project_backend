package kr.or.ddit.controller.instructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.or.ddit.finalProject.dto.assignment.AssignmentBoardDto;
import kr.or.ddit.finalProject.dto.assignment.AssignmentSubmitDto;
import kr.or.ddit.finalProject.dto.attendance.ClassAttendanceSummaryDto;
import kr.or.ddit.finalProject.mapper.attendance.StudentAttendanceMapper;
import kr.or.ddit.finalProject.dto.classroom.ClassStatus;
import kr.or.ddit.finalProject.dto.classroom.ClassroomDetailResponse;
import kr.or.ddit.finalProject.dto.classroom.ClassroomDto;
import kr.or.ddit.finalProject.dto.classroom.ClassroomMemberListResponse;
import kr.or.ddit.finalProject.dto.instructor.board.InstructorBoardDto;
import kr.or.ddit.finalProject.dto.lecture.ClassroomLectureResponse;
import kr.or.ddit.finalProject.dto.lecture.LectureProgressDetailResponse;
import kr.or.ddit.finalProject.dto.staff.AdminActivityType;
import kr.or.ddit.finalProject.service.assignment.AssignmentBoardService;
import kr.or.ddit.finalProject.service.classroom.ClassroomService;
import kr.or.ddit.finalProject.service.course.CourseService;
import kr.or.ddit.finalProject.service.exam.ExamService;
import kr.or.ddit.finalProject.service.instructor.InstructorBoardService;
import kr.or.ddit.finalProject.service.lecture.LectureService;
import kr.or.ddit.finalProject.service.exam.GeminiQuestionService;
import kr.or.ddit.service.AdminActivityApprovalService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/classroom")
public class AdminClassroomController extends AbstractClassroomController {

    private static final int PAGE_SIZE = 10;

    private final LectureService lectureService;
    private final CourseService courseService;
    private final AdminActivityApprovalService adminActivityApprovalService;
    private final ExamService examService;
    private final GeminiQuestionService geminiQuestionService;
    private final StudentAttendanceMapper studentAttendanceMapper;

    public AdminClassroomController(ClassroomService classroomService,
                                    AssignmentBoardService assignmentBoardService,
                                    InstructorBoardService instructorBoardService,
                                    LectureService lectureService,
                                    CourseService courseService,
                                    AdminActivityApprovalService adminActivityApprovalService,
                                    ExamService examService,
                                    GeminiQuestionService geminiQuestionService,
                                    StudentAttendanceMapper studentAttendanceMapper) {
        super(classroomService, assignmentBoardService, instructorBoardService, examService);
        this.lectureService = lectureService;
        this.courseService = courseService;
        this.adminActivityApprovalService = adminActivityApprovalService;
        this.examService = examService;
        this.geminiQuestionService = geminiQuestionService;
        this.studentAttendanceMapper = studentAttendanceMapper;
    }

    // 클래스룸 목록 페이지 렌더링 (데이터는 AJAX로 별도 로드)
    @GetMapping("/list")
    public String classroomList(Model model, Authentication authentication) {
        String instrUserId = authentication.getName();
        model.addAttribute("courseList", courseService.retrieveCoursesByInstructor(instrUserId));
        return "admin:/instructor/list-classroom";
    }

    // 클래스룸 목록 AJAX 데이터
    @GetMapping("/list/data")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> classroomListData(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int screenSize,
            Authentication authentication) {
        String instrUserId = authentication.getName();
        var result = classroomService.retrieveClassroomListPaged(instrUserId, page, screenSize);
        return ResponseEntity.ok(Map.of(
                "items", result.getItems(),
                "totalCount", result.getTotalCount(),
                "page", page,
                "screenSize", screenSize
        ));
    }

    // 클래스룸 등록 결재 요청
    @PostMapping("/create")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createClassroom(
            @ModelAttribute ClassroomDto classroomDto,
            Authentication authentication) {
        if (classroomDto.getClassNm() == null || classroomDto.getClassNm().isBlank()) {
            return ResponseEntity.ok(Map.of("success", false, "message", "클래스룸명을 입력하세요."));
        }
        if (classroomDto.getCourseSn() == null) {
            return ResponseEntity.ok(Map.of("success", false, "message", "강좌를 선택하세요."));
        }

        String userId = authentication.getName();
        boolean isOwnCourse = courseService.retrieveCoursesByInstructor(userId).stream()
                .anyMatch(c -> c.getCourseSn().equals(classroomDto.getCourseSn()));
        if (!isOwnCourse) {
            return ResponseEntity.ok(Map.of("success", false, "message", "선택한 강좌에 대한 권한이 없습니다."));
        }
        classroomDto.setOpnrUserId(userId);
        classroomDto.setRgtrId(userId);

        Map<String, Object> payload = new HashMap<>();
        payload.put("classroomDto", classroomDto);

        adminActivityApprovalService.submitForApproval(
                userId,
                AdminActivityType.CLASSROOM_CREATE,
                classroomDto.getClassNm(),
                payload
        );
        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * 클래스룸 상태 즉시 변경 (결재 없음).
     * Thymeleaf form 은 GET/POST만 지원하므로 POST로 통일.
     * 소유권 검증은 Service → Mapper EXISTS 절에서 처리하므로 컨트롤러에서는 enum 파싱만 담당.
     */
    @PostMapping("/{classSn}/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateClassroomStatus(
            @PathVariable Long classSn,
            @RequestParam String classStatCd,
            Authentication authentication) {

        // 유효하지 않은 상태 코드 차단 (ClassStatus enum 에 없는 값은 거절)
        ClassStatus status;
        try {
            status = ClassStatus.valueOf(classStatCd);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(Map.of("success", false, "message", "유효하지 않은 상태값입니다."));
        }

        String userId = authentication.getName();
        // Mapper SQL 의 EXISTS 절이 소유권을 검증: 본인 클래스룸이 아니면 0건 UPDATE
        boolean updated = classroomService.updateClassroomStatus(classSn, status.name(), userId);
        if (!updated) {
            return ResponseEntity.ok(Map.of("success", false, "message", "상태 변경 권한이 없거나 존재하지 않는 클래스룸입니다."));
        }
        return ResponseEntity.ok(Map.of("success", true));
    }

    // 클래스룸 홈 대시보드
    @GetMapping("/detail/{classSn}")
    public String classroomDetail(@PathVariable Long classSn, Model model,
            Authentication authentication) {
        ClassroomDetailResponse ownedClassroom = getOwnedClassroom(classSn, authentication.getName());
        if (ownedClassroom == null) {
            return "redirect:/classroom/list";
        }
        LocalDate now = LocalDate.now();
        LocalDateTime nowDt = LocalDateTime.now();

        model.addAttribute("classroom", ownedClassroom);

        // ── 수강생 현황 요약 카드
        List<kr.or.ddit.finalProject.dto.classroom.ClassroomMemberListResponse> members = ownedClassroom.getMembers();
        long totalStudents = members.stream()
                .filter(m -> m.getEnrlStatCd() == kr.or.ddit.finalProject.dto.classroom.EnrollStatus.ENROLLED
                          || m.getEnrlStatCd() == kr.or.ddit.finalProject.dto.classroom.EnrollStatus.COMPLETED)
                .count();
        long completedStudents = members.stream()
                .filter(m -> m.getEnrlStatCd() == kr.or.ddit.finalProject.dto.classroom.EnrollStatus.COMPLETED)
                .count();
        int completionRate = totalStudents == 0 ? 0
                : (int) Math.round(completedStudents * 100.0 / totalStudents);
        model.addAttribute("totalStudents", totalStudents);
        model.addAttribute("completedStudents", completedStudents);
        model.addAttribute("completionRate", completionRate);

        // ── 평균 진도율
        Map<String, Double> progressRates = classroomService.retrieveProgressRates(classSn);
        double avgProgressRate = members.stream()
                .filter(m -> m.getEnrlStatCd() == kr.or.ddit.finalProject.dto.classroom.EnrollStatus.ENROLLED
                          || m.getEnrlStatCd() == kr.or.ddit.finalProject.dto.classroom.EnrollStatus.COMPLETED)
                .mapToDouble(m -> progressRates.getOrDefault(m.getUserId(), 0.0))
                .average().orElse(0.0);
        model.addAttribute("avgProgressRate", (int) Math.round(avgProgressRate));

        // ── 시험 현황 카드 + 예정 시험 목록
        List<kr.or.ddit.finalProject.dto.exam.ExamDto> allExams = examService.retrieveExamsByClassSn(classSn);
        String nowFormatted = now + " 00:00"; // "YYYY-MM-DD HH:MI" 포맷 비교용
        long ongoingExamCount = allExams.stream()
                .filter(e -> e.getExamStrtDt() != null && e.getExamEndDt() != null)
                .filter(e -> e.getExamStrtDt().compareTo(nowFormatted) <= 0
                          && e.getExamEndDt().compareTo(nowFormatted) >= 0)
                .count();
        long upcomingExamCount = allExams.stream()
                .filter(e -> e.getExamStrtDt() != null)
                .filter(e -> e.getExamStrtDt().compareTo(nowFormatted) > 0)
                .count();
        model.addAttribute("ongoingExamCount", ongoingExamCount);
        model.addAttribute("upcomingExamCount", upcomingExamCount);
        List<kr.or.ddit.finalProject.dto.exam.ExamDto> upcomingExams = allExams.stream()
                .filter(e -> e.getExamEndDt() == null || e.getExamEndDt().compareTo(nowFormatted) >= 0)
                .sorted(Comparator.comparing(e -> e.getExamStrtDt() != null ? e.getExamStrtDt() : "9999"))
                .limit(5)
                .collect(Collectors.toList());
        model.addAttribute("upcomingExams", upcomingExams);
        // pendingExamGradeCount는 addExamTabBadge()에서 공통 주입

        // ── 강좌 진도율 요약
        List<ClassroomLectureResponse> lectures = classroomService.retrieveLecturesWithProgress(classSn);
        int totalLectures = lectures.size();
        int avgCompletionPct = 0;
        if (totalLectures > 0) {
            avgCompletionPct = (int) lectures.stream()
                    .filter(l -> l.getTotMemberCnt() > 0)
                    .mapToLong(l -> (long) l.getCmplCnt() * 100 / l.getTotMemberCnt())
                    .average().orElse(0);
        }
        model.addAttribute("totalLectures", totalLectures);
        model.addAttribute("avgCompletionPct", avgCompletionPct);

        // ── 마감 임박 과제 (오늘~모레)
        List<AssignmentBoardDto> allAssignments = assignmentBoardService.getAssignmentList(classSn, 1, 9999).getItems();
        LocalDateTime threshold = now.plusDays(2).atTime(23, 59, 59);
        List<AssignmentBoardDto> deadlineSoonList = allAssignments.stream()
                .filter(a -> a.getSbmtDdlnDt() != null)
                .filter(a -> !a.getSbmtDdlnDt().isBefore(nowDt) && !a.getSbmtDdlnDt().isAfter(threshold))
                .sorted(Comparator.comparing(AssignmentBoardDto::getSbmtDdlnDt))
                .map(a -> { a.setDaysUntil((int) ChronoUnit.DAYS.between(now, a.getSbmtDdlnDt().toLocalDate())); return a; })
                .collect(Collectors.toList());
        model.addAttribute("totalAssignmentCount", allAssignments.size());
        model.addAttribute("deadlineSoonList", deadlineSoonList);

        // ── 최근 제출된 과제 (최대 5건)
        model.addAttribute("recentSubmits", assignmentBoardService.getRecentSubmits(classSn, 5));

        // ── 최근 공지사항
        List<InstructorBoardDto> notices = instructorBoardService.getClassroomNoticeList(classSn, 1, 1).getItems();
        model.addAttribute("recentNotice", notices.isEmpty() ? null : notices.get(0));

        // ── 약점 분석 TOP 3
        model.addAttribute("topWeakPoints",
                geminiQuestionService.retrieveWeakPoints(classSn).stream().limit(3).collect(Collectors.toList()));

        return "classroom/home-classroom";
    }

    // ── 온라인 강의 ──────────────────────────────────────────────
    // 강의 목록 + 수강생별 진도 현황
    @GetMapping("/detail/{classSn}/lectures")
    public String lectureList(@PathVariable Long classSn, Model model, Authentication authentication) {
        ClassroomDetailResponse classroom = getOwnedClassroom(classSn, authentication.getName());
        if (classroom == null) {
            return "redirect:/classroom/list";
        }
        model.addAttribute("classroom", classroom);
        model.addAttribute("lectureList", classroomService.retrieveLecturesWithProgress(classSn));
        return "classroom/list-classroom-lectures";
    }

    // 강의 상세 + 수강생 개인별 진도율 (강좌 소속 검증 포함)
    @GetMapping("/detail/{classSn}/lectures/{lectureSn}")
    public String lectureDetail(@PathVariable Long classSn, @PathVariable Long lectureSn, Model model,
            Authentication authentication) {
        ClassroomDetailResponse classroom = getOwnedClassroom(classSn, authentication.getName());
        if (classroom == null) {
            return "redirect:/classroom/list";
        }
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
        if (classroom == null) {
            return ResponseEntity.status(403).body("forbidden");
        }
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
        if (classroom == null) {
            return ResponseEntity.status(403).body("forbidden");
        }
        kr.or.ddit.finalProject.dto.lecture.LectureDto lecture = lectureService.retrieveLectureBySn(lectureSn);
        if (lecture == null || !lecture.getCourseSn().equals(classroom.getCourseSn())) {
            return ResponseEntity.badRequest().body("invalid");
        }
        lectureService.toggleLockYn(lectureSn, authentication.getName());
        return ResponseEntity.ok("ok");
    }

    // ── 약점 분석 ────────────────────────────────────────────────
    @GetMapping("/detail/{classSn}/grades")
    public String gradeList(@PathVariable Long classSn,
            Model model, Authentication authentication) {
        ClassroomDetailResponse classroom = getOwnedClassroom(classSn, authentication.getName());
        if (classroom == null) {
            return "redirect:/classroom/list";
        }
        model.addAttribute("classroom", classroom);
        model.addAttribute("weakPoints", geminiQuestionService.retrieveWeakPoints(classSn));
        model.addAttribute("difficultyStats", geminiQuestionService.retrieveDifficultyStats(classSn));
        model.addAttribute("examTrend", geminiQuestionService.retrieveExamTrend(classSn));
        return "classroom/list-classroom-grades";
    }

    // ── 수강생 목록 ──────────────────────────────────────────────
    @GetMapping("/detail/{classSn}/members")
    public String memberList(@PathVariable Long classSn,
            @RequestParam(defaultValue = "1") int page,
            Model model, Authentication authentication) {
        ClassroomDetailResponse classroom = getOwnedClassroom(classSn, authentication.getName());
        if (classroom == null) {
            return "redirect:/classroom/list";
        }
        kr.or.ddit.finalProject.dto.common.PageResponse<ClassroomMemberListResponse> memberPage =
                classroomService.retrieveMemberListPaged(classSn, page, PAGE_SIZE);
        model.addAttribute("classroom", classroom);
        model.addAttribute("memberPage", memberPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", PAGE_SIZE);
        model.addAttribute("totalPages", (int) Math.ceil((double) memberPage.getTotalCount() / PAGE_SIZE));
        return "classroom/list-classroom-members";
    }

    // ── 출결 관리 ──────────────────────────────────────────────────

    // 학생별 근태 특이사항(결석/지각/조퇴) 조회
    @GetMapping("/detail/{classSn}/attendance")
    public String attendancePage(@PathVariable Long classSn,
            Model model, Authentication authentication) {
        ClassroomDetailResponse classroom = getOwnedClassroom(classSn, authentication.getName());
        if (classroom == null) return "redirect:/classroom/list";

        List<ClassAttendanceSummaryDto> rows =
                studentAttendanceMapper.selectAttendanceSummaryByClassSn(classSn);

        model.addAttribute("classroom", classroom);
        model.addAttribute("attendanceRows", rows);
        return "classroom/list-classroom-attendance";
    }

    // 수강생 상세 — 기본정보 + 강의진도 + 과제 + 시험 + 최근 QnA
    @GetMapping("/detail/{classSn}/members/{userId}")
    public String memberDetail(@PathVariable Long classSn, @PathVariable String userId,
            Model model, Authentication authentication) {
        ClassroomDetailResponse classroom = getOwnedClassroom(classSn, authentication.getName());
        if (classroom == null) return "redirect:/classroom/list";

        kr.or.ddit.finalProject.dto.classroom.StudentDetailDto student =
                classroomService.retrieveStudentDetail(classSn, userId);
        if (student == null) return "redirect:/classroom/detail/" + classSn + "/members";

        List<LectureProgressDetailResponse> lectureProgress =
                lectureService.retrieveLectureProgressByStudent(classSn, userId);
        long completedLectureCount = lectureProgress.stream().filter(l -> "Y".equals(l.getCmplYn())).count();

        model.addAttribute("classroom", classroom);
        model.addAttribute("student", student);
        model.addAttribute("lectureProgress", lectureProgress);
        model.addAttribute("completedLectureCount", completedLectureCount);
        model.addAttribute("totalLectureCount", lectureProgress.size());
        model.addAttribute("assignments", assignmentBoardService.getAssignmentsByStudent(classSn, userId));
        model.addAttribute("exams", examService.retrieveExamsByStudent(classSn, userId));
        model.addAttribute("recentQna", instructorBoardService.getRecentQnaByStudent(classSn, userId, 5));
        model.addAttribute("attendanceHistory", studentAttendanceMapper.selectAttendanceHistoryByStudent(userId));
        return "classroom/detail-classroom-member";
    }
}
