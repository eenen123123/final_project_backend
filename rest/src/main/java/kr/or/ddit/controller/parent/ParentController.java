package kr.or.ddit.controller.parent;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kr.or.ddit.finalProject.dto.classroom.StudentAssignmentItem;
import kr.or.ddit.finalProject.dto.classroom.StudentExamResponse;
import kr.or.ddit.finalProject.dto.parent.ParentAttendanceResponse;
import kr.or.ddit.finalProject.dto.parent.ParentChildDto;
import kr.or.ddit.finalProject.dto.student.StudentAttendanceDto;
import kr.or.ddit.finalProject.mapper.assignment.AssignmentBoardMapper;
import kr.or.ddit.finalProject.mapper.exam.ExamMapper;
import kr.or.ddit.finalProject.mapper.parent.ParentMapper;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/parent")
@RequiredArgsConstructor
public class ParentController {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final ParentMapper parentMapper;
    private final AssignmentBoardMapper assignmentBoardMapper;
    private final ExamMapper examMapper;

    // ── 자녀 목록 ────────────────────────────────────────────────────────────

    @GetMapping("/children")
    public ResponseEntity<List<ParentChildDto>> getChildren(Authentication authentication) {
        String parentId = authentication.getName();
        return ResponseEntity.ok(parentMapper.selectChildrenByParentId(parentId));
    }

    // ── 자녀 월별 출결 ────────────────────────────────────────────────────────

    @GetMapping("/children/{studentId}/attendance")
    public ResponseEntity<ParentAttendanceResponse> getAttendance(
            @PathVariable String studentId,
            @RequestParam int year,
            @RequestParam int month,
            Authentication authentication) {

        if (!parentMapper.isParentOf(authentication.getName(), studentId)) {
            return ResponseEntity.status(403).build();
        }

        List<StudentAttendanceDto> raw = parentMapper.selectMonthlyAttendance(studentId, year, month);

        int lateCount = 0, absentCount = 0, earlyLeaveCount = 0;
        List<ParentAttendanceResponse.Record> records = new java.util.ArrayList<>();

        for (StudentAttendanceDto dto : raw) {
            String typeCd = dto.getAtndTypeCd() != null ? dto.getAtndTypeCd().trim() : "";
            String status;
            switch (typeCd) {
                case "02": status = "ABSENT";      absentCount++;     break;
                case "03": status = "LATE";        lateCount++;       break;
                case "04": status = "EARLY_LEAVE"; earlyLeaveCount++; break;
                default: continue; // 근태 특이사항이 아닌 기록(출석 등)은 제외
            }
            int day = dto.getAtndRegDt().getDayOfMonth();
            records.add(new ParentAttendanceResponse.Record(day, status, dto.getAtndNoteCn()));
        }

        return ResponseEntity.ok(
                new ParentAttendanceResponse(year, month, lateCount, absentCount, earlyLeaveCount, records));
    }

    // ── 자녀 과제 목록 ────────────────────────────────────────────────────────

    @GetMapping("/children/{studentId}/assignments")
    public ResponseEntity<List<StudentAssignmentItem>> getAssignments(
            @PathVariable String studentId,
            @RequestParam Long classSn,
            Authentication authentication) {

        if (!parentMapper.isParentOf(authentication.getName(), studentId)) {
            return ResponseEntity.status(403).build();
        }

        List<StudentAssignmentItem> items = assignmentBoardMapper
                .selectAssignmentsByStudent(classSn, studentId).stream()
                .map(a -> new StudentAssignmentItem(
                        a.getAsgmtSn(), a.getAsgmtSj(), a.getSbmtDdlnDt(),
                        "Y".equals(a.getSbmtYn()),
                        "Y".equals(a.getGrddYn()) ? a.getScore() : null))
                .collect(Collectors.toList());

        return ResponseEntity.ok(items);
    }

    // ── 자녀 시험 목록 ────────────────────────────────────────────────────────

    @GetMapping("/children/{studentId}/exams")
    public ResponseEntity<List<StudentExamResponse>> getExams(
            @PathVariable String studentId,
            @RequestParam Long classSn,
            Authentication authentication) {

        if (!parentMapper.isParentOf(authentication.getName(), studentId)) {
            return ResponseEntity.status(403).build();
        }

        LocalDateTime now = LocalDateTime.now();
        List<StudentExamResponse> result = examMapper.selectExamsByStudent(classSn, studentId).stream()
                .map(e -> {
                    String status = resolveExamStatus(e.getExamStrtDt(), e.getExamEndDt(), now);
                    Double score = "Y".equals(e.getTakenYn()) ? e.getTotScore() : null;
                    return new StudentExamResponse(
                            e.getExamSn(), e.getExamRegNm(),
                            e.getExamStrtDt(), e.getExamEndDt(),
                            status, "Y".equals(e.getTakenYn()), score);
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    private String resolveExamStatus(String strtDt, String endDt, LocalDateTime now) {
        if (strtDt == null || endDt == null) return "UPCOMING";
        LocalDateTime start = LocalDateTime.parse(strtDt, DT_FMT);
        LocalDateTime end   = LocalDateTime.parse(endDt, DT_FMT);
        if (now.isBefore(start)) return "UPCOMING";
        if (now.isAfter(end))    return "CLOSED";
        return "ONGOING";
    }
}
