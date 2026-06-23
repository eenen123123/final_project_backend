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
import kr.or.ddit.finalProject.service.instructor.InstructorBoardService;
import kr.or.ddit.finalProject.service.lecture.LectureService;
import kr.or.ddit.service.AdminActivityApprovalService;
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
    private final CourseService courseService;
    private final AdminActivityApprovalService adminActivityApprovalService;

    @ModelAttribute
    public void addTabBadges(@PathVariable(required = false) Long classSn, Model model) {
        if (classSn != null) {
            model.addAttribute("assignmentCount",
                    assignmentBoardService.getPendingGradeCount(classSn));
            model.addAttribute("unansweredQnaCount",
                    instructorBoardService.getUnansweredQnaCount(classSn));
        }
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

    /**
     * 클래스룸 상세를 조회하되 요청 강사가 담당자인지 검증한다. 존재하지 않거나 본인 클래스가 아니면 null 반환 — 호출부에서 목록
     * 페이지로 리다이렉트 처리.
     */
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
        if (ownedClassroom == null) {
            return "redirect:/classroom/list";
        }
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

        List<InstructorBoardDto> notices
                = instructorBoardService.getClassroomNoticeList(classSn);
        model.addAttribute("recentNotice", notices.isEmpty() ? null : notices.get(0));

        // 강좌 진도율 요약 — 강의 위젯(home-classroom.html의 "강좌 진도율 요약" 카드)에서 사용
        // avgCompletionPct: 강의별 완료율의 평균 (강의 중심)
        // avgProgressRate(아래): 수강생별 진도율의 평균 (수강생 중심) — 요약 카드에서 사용
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

        // 수강생 현황 요약 카드 — 탈퇴(WITHDRAWN)/취소(CANCELLED)는 집계에서 제외
        List<kr.or.ddit.finalProject.dto.classroom.ClassroomMemberListResponse> members = ownedClassroom.getMembers();
        long totalStudents = members.stream() // 총 수강생 수 (수강중 + 이수완료)
                .filter(m -> m.getEnrlStatCd() == kr.or.ddit.finalProject.dto.classroom.EnrollStatus.ENROLLED
                || m.getEnrlStatCd() == kr.or.ddit.finalProject.dto.classroom.EnrollStatus.COMPLETED)
                .count();
        long completedStudents = members.stream() // 이수완료 수강생 수
                .filter(m -> m.getEnrlStatCd() == kr.or.ddit.finalProject.dto.classroom.EnrollStatus.COMPLETED)
                .count();
        int completionRate = totalStudents == 0 ? 0 // 이수율 (%) = 이수완료 / 총수강생
                : (int) Math.round(completedStudents * 100.0 / totalStudents);
        model.addAttribute("totalStudents", totalStudents);
        model.addAttribute("completedStudents", completedStudents);
        model.addAttribute("completionRate", completionRate);

        // 평균 진도율 — 수강생별 진도율의 평균 (수강중 + 이수완료만 대상, 탈퇴/취소 제외)
        Map<String, Double> progressRates = classroomService.retrieveProgressRates(classSn);
        double avgProgressRate = members.stream()
                .filter(m -> m.getEnrlStatCd() == kr.or.ddit.finalProject.dto.classroom.EnrollStatus.ENROLLED
                || m.getEnrlStatCd() == kr.or.ddit.finalProject.dto.classroom.EnrollStatus.COMPLETED)
                .mapToDouble(m -> progressRates.getOrDefault(m.getUserId(), 0.0))
                .average()
                .orElse(0.0);
        model.addAttribute("avgProgressRate", (int) Math.round(avgProgressRate));

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

    // ── 성적 관리 ────────────────────────────────────────────────
    // 수강생별 성적 목록
    @GetMapping("/detail/{classSn}/grades")
    public String gradeList(@PathVariable Long classSn, Model model, Authentication authentication) {
        ClassroomDetailResponse classroom = getOwnedClassroom(classSn, authentication.getName());
        if (classroom == null) {
            return "redirect:/classroom/list";
        }
        model.addAttribute("classroom", classroom);
        model.addAttribute("gradeList", classroomService.retrieveGradeList(classSn));
        return "classroom/list-classroom-grades";
    }

    // ── 수강생 목록 ──────────────────────────────────────────────
    // 수강생 목록 — 진도율 컬럼 표시를 위해 진도율 별도 조회 후 각 수강생 객체에 병합
    // (진도율은 retrieveClassroomDetail에 포함하지 않음 — 다른 탭 진입 시 불필요한 쿼리 방지)
    @GetMapping("/detail/{classSn}/members")
    public String memberList(@PathVariable Long classSn, Model model, Authentication authentication) {
        ClassroomDetailResponse classroom = getOwnedClassroom(classSn, authentication.getName());
        if (classroom == null) {
            return "redirect:/classroom/list";
        }
        Map<String, Double> progressRates = classroomService.retrieveProgressRates(classSn);
        // 진도율 맵(userId → rate)을 수강생 목록에 일괄 병합
        classroom.getMembers().forEach(m -> m.setProgressRate(progressRates.getOrDefault(m.getUserId(), 0.0)));
        model.addAttribute("classroom", classroom);
        return "classroom/list-classroom-members";
    }

    // 수강생 진도 상세 — 특정 수강생의 강의별 완료 현황 조회
    @GetMapping("/detail/{classSn}/members/{userId}")
    public String memberDetail(@PathVariable Long classSn, @PathVariable String userId,
            Model model, Authentication authentication) {
        ClassroomDetailResponse classroom = getOwnedClassroom(classSn, authentication.getName());
        if (classroom == null) {
            return "redirect:/classroom/list";
        }

        // classroom.getMembers()는 getOwnedClassroom에서 이미 로딩됨 — 추가 DB 쿼리 없이 재사용
        ClassroomMemberListResponse member = classroom.getMembers().stream()
                .filter(m -> m.getUserId().equals(userId))
                .findFirst()
                .orElse(null);
        // 해당 클래스 소속이 아닌 userId로 직접 URL 접근 시 수강생 목록으로 리다이렉트
        if (member == null) {
            return "redirect:/classroom/detail/" + classSn + "/members";
        }

        List<LectureProgressDetailResponse> lectureProgress
                = lectureService.retrieveLectureProgressByStudent(classSn, userId);
        long completedCount = lectureProgress.stream().filter(l -> "Y".equals(l.getCmplYn())).count();

        model.addAttribute("classroom", classroom);
        model.addAttribute("member", member);
        model.addAttribute("lectureProgress", lectureProgress);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("totalCount", lectureProgress.size());
        return "classroom/detail-classroom-member";
    }
}
