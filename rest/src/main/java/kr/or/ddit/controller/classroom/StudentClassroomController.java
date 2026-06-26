package kr.or.ddit.controller.classroom;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.or.ddit.finalProject.dto.assignment.AssignmentBoardDto;
import kr.or.ddit.finalProject.dto.assignment.AssignmentSubmitDto;
import kr.or.ddit.finalProject.dto.classroom.ClassroomDetailResponse;
import kr.or.ddit.finalProject.dto.file.FileDto;
import kr.or.ddit.finalProject.service.file.FileUploadService;
import kr.or.ddit.finalProject.dto.classroom.ClassroomMemberListResponse;
import kr.or.ddit.finalProject.dto.classroom.ClassroomQnaDto;
import kr.or.ddit.finalProject.dto.classroom.MySummaryResponse;
import kr.or.ddit.finalProject.dto.classroom.StudentAssignmentDetail;
import kr.or.ddit.finalProject.dto.classroom.StudentAssignmentItem;
import kr.or.ddit.finalProject.dto.classroom.StudentAssignmentDto;
import kr.or.ddit.finalProject.dto.classroom.StudentExamDetailResponse;
import kr.or.ddit.finalProject.dto.classroom.StudentExamDto;
import kr.or.ddit.finalProject.dto.classroom.StudentExamResponse;
import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.dto.exam.ExamDto;
import kr.or.ddit.finalProject.dto.exam.ExamQuestionDto;
import kr.or.ddit.finalProject.dto.exam.ExamTakerDto;
import kr.or.ddit.finalProject.dto.instructor.board.InstructorBoardDto;
import kr.or.ddit.finalProject.mapper.assignment.AssignmentBoardMapper;
import kr.or.ddit.finalProject.mapper.assignment.AssignmentSubmitMapper;
import kr.or.ddit.finalProject.mapper.classroom.ClassroomMemberMapper;
import kr.or.ddit.finalProject.mapper.exam.ExamMapper;
import kr.or.ddit.finalProject.service.classroom.ClassroomService;
import kr.or.ddit.finalProject.service.instructor.InstructorBoardService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/classroom/{classSn}")
@RequiredArgsConstructor
public class StudentClassroomController {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final ClassroomMemberMapper classroomMemberMapper;
    private final AssignmentBoardMapper assignmentBoardMapper;
    private final AssignmentSubmitMapper assignmentSubmitMapper;
    private final ExamMapper examMapper;
    private final InstructorBoardService instructorBoardService;
    private final ClassroomService classroomService;
    private final FileUploadService fileUploadService;
    private final ObjectMapper objectMapper;

    // ── 수강생 검증 헬퍼 ─────────────────────────────────────────────────

    private boolean isMember(Long classSn, String userId) {
        return classroomMemberMapper.isMember(classSn, userId);
    }

    private static int computeTotalPages(int totalCount, int size) {
        return size > 0 ? (totalCount + size - 1) / size : 0;
    }

    // ── 홈 탭: 개인 진도 요약 ─────────────────────────────────────────────

    @GetMapping("/my-summary")
    public ResponseEntity<MySummaryResponse> getMySummary(
            @PathVariable Long classSn, Authentication authentication) {
        String userId = authentication.getName();
        if (!isMember(classSn, userId)) return ResponseEntity.status(403).build();

        // 강의 진도율
        int progressRate = (int) classroomMemberMapper.selectProgressRatesByClassSn(classSn).stream()
                .filter(m -> userId.equals(m.getUserId()))
                .mapToDouble(ClassroomMemberListResponse::getProgressRate)
                .findFirst().orElse(0);

        // 과제 제출률 + 평균 점수
        List<StudentAssignmentDto> assignments =
                assignmentBoardMapper.selectAssignmentsByStudent(classSn, userId);
        int totalAsgmt = assignments.size();
        long submittedCount = assignments.stream().filter(a -> "Y".equals(a.getSbmtYn())).count();
        int assignSubmitRate = totalAsgmt > 0 ? (int) (submittedCount * 100 / totalAsgmt) : 0;

        double sum = 0;
        int gradedCount = 0;
        for (StudentAssignmentDto a : assignments) {
            if ("Y".equals(a.getGrddYn()) && a.getScore() != null) {
                sum += a.getScore();
                gradedCount++;
            }
        }
        Double avgScore = gradedCount > 0 ? Math.round(sum / gradedCount * 100.0) / 100.0 : null;

        // 예정/진행 시험 수
        LocalDateTime now = LocalDateTime.now();
        List<StudentExamDto> exams = examMapper.selectExamsByStudent(classSn, userId);
        long upcomingExamCount = exams.stream()
                .filter(e -> "N".equals(e.getTakenYn()) && e.getExamEndDt() != null
                        && LocalDateTime.parse(e.getExamEndDt(), DT_FMT).isAfter(now))
                .count();

        return ResponseEntity.ok(new MySummaryResponse(
                progressRate, assignSubmitRate, (int) upcomingExamCount, avgScore));
    }

    // ── 홈 탭: 마감 임박 과제 (오늘~2일 이내) ────────────────────────────

    @GetMapping("/assignments/upcoming")
    public ResponseEntity<List<StudentAssignmentItem>> getUpcomingAssignments(
            @PathVariable Long classSn, Authentication authentication) {
        String userId = authentication.getName();
        if (!isMember(classSn, userId)) return ResponseEntity.status(403).build();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime limit = now.plusDays(2);

        List<StudentAssignmentItem> result = assignmentBoardMapper
                .selectAssignmentsByStudent(classSn, userId).stream()
                .filter(a -> {
                    if (a.getSbmtDdlnDt() == null) return false;
                    LocalDateTime due = LocalDateTime.parse(a.getSbmtDdlnDt(), DT_FMT);
                    return !due.isBefore(now) && !due.isAfter(limit);
                })
                .map(a -> new StudentAssignmentItem(
                        a.getAsgmtSn(), a.getAsgmtSj(), a.getSbmtDdlnDt(),
                        "Y".equals(a.getSbmtYn()), null))
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // ── 과제 탭: 목록 ─────────────────────────────────────────────────────

    @GetMapping("/assignments")
    public ResponseEntity<PageResponse<StudentAssignmentItem>> getAssignments(
            @PathVariable Long classSn,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        String userId = authentication.getName();
        if (!isMember(classSn, userId)) return ResponseEntity.status(403).build();

        List<StudentAssignmentDto> all =
                assignmentBoardMapper.selectAssignmentsByStudent(classSn, userId);
        int totalCount = all.size();
        int totalPages = computeTotalPages(totalCount, size);

        List<StudentAssignmentItem> items = all.stream()
                .skip((long) (page - 1) * size)
                .limit(size)
                .map(a -> new StudentAssignmentItem(
                        a.getAsgmtSn(), a.getAsgmtSj(), a.getSbmtDdlnDt(),
                        "Y".equals(a.getSbmtYn()),
                        "Y".equals(a.getGrddYn()) ? a.getScore() : null))
                .collect(Collectors.toList());

        return ResponseEntity.ok(new PageResponse<>(items, totalCount, totalPages));
    }

    // ── 과제 탭: 상세 ─────────────────────────────────────────────────────

    @GetMapping("/assignments/{asgmtSn}")
    public ResponseEntity<StudentAssignmentDetail> getAssignmentDetail(
            @PathVariable Long classSn,
            @PathVariable Long asgmtSn,
            Authentication authentication) {
        String userId = authentication.getName();
        if (!isMember(classSn, userId)) return ResponseEntity.status(403).build();

        AssignmentBoardDto board = assignmentBoardMapper.selectAssignmentDetail(asgmtSn);
        if (board == null || !classSn.equals(board.getClassSn())) return ResponseEntity.notFound().build();

        AssignmentSubmitDto submit = assignmentSubmitMapper.selectMySubmit(asgmtSn, userId);
        boolean submitted = submit != null;
        String dueDt = board.getSbmtDdlnDt() != null
                ? board.getSbmtDdlnDt().format(DT_FMT) : null;

        String sbmtCnVal = null;
        Double scoreVal = null;
        if (submit != null) {
            sbmtCnVal = submit.getSbmtCn();
            if ("Y".equals(submit.getGrddYn()) && submit.getScore() != null) {
                scoreVal = submit.getScore().doubleValue();
            }
        }

        List<FileDto> attachedFiles = null;
        if (submit != null && submit.getAtchFileId() != null) {
            attachedFiles = fileUploadService.retrieveFilesByGroupId(submit.getAtchFileId().intValue());
        }

        return ResponseEntity.ok(new StudentAssignmentDetail(
                board.getAsgmtSn(),
                board.getAsgmtSj(),
                board.getAsgmtCn(),
                dueDt,
                submitted,
                sbmtCnVal,
                scoreVal,
                null,
                board.getResbmtAlldYn() != null ? board.getResbmtAlldYn() : "N",
                attachedFiles
        ));
    }

    // ── 과제 탭: 제출 ─────────────────────────────────────────────────────

    @PostMapping("/assignments/{asgmtSn}/submit")
    public ResponseEntity<Void> submitAssignment(
            @PathVariable Long classSn,
            @PathVariable Long asgmtSn,
            @RequestBody Map<String, Object> body,
            Authentication authentication) {
        String userId = authentication.getName();
        if (!isMember(classSn, userId)) return ResponseEntity.status(403).build();

        String sbmtCn = (String) body.get("sbmtCn");

        @SuppressWarnings("unchecked")
        List<?> rawIds = body.get("fileServerIds") instanceof List ? (List<?>) body.get("fileServerIds") : null;
        Long atchFileId = null;
        if (rawIds != null && !rawIds.isEmpty()) {
            List<Long> fileServerIds = rawIds.stream()
                    .map(id -> ((Number) id).longValue())
                    .collect(Collectors.toList());
            atchFileId = (long) fileUploadService.linkFilesToGroup(fileServerIds);
        }

        AssignmentSubmitDto existing = assignmentSubmitMapper.selectMySubmit(asgmtSn, userId);
        if (existing == null) {
            AssignmentSubmitDto dto = new AssignmentSubmitDto();
            dto.setAsgmtSn(asgmtSn);
            dto.setSbmtUserId(userId);
            dto.setSbmtCn(sbmtCn);
            dto.setAtchFileId(atchFileId);
            assignmentSubmitMapper.insertSubmit(dto);
        } else {
            AssignmentBoardDto board = assignmentBoardMapper.selectAssignmentDetail(asgmtSn);
            if (!"Y".equals(board.getResbmtAlldYn())) return ResponseEntity.status(409).build();
            assignmentSubmitMapper.updateMySubmit(asgmtSn, userId, sbmtCn, atchFileId);
        }
        return ResponseEntity.ok().build();
    }

    // ── 시험 탭: 목록 ─────────────────────────────────────────────────────

    @GetMapping("/exams")
    public ResponseEntity<List<StudentExamResponse>> getExams(
            @PathVariable Long classSn, Authentication authentication) {
        String userId = authentication.getName();
        if (!isMember(classSn, userId)) return ResponseEntity.status(403).build();

        LocalDateTime now = LocalDateTime.now();
        List<StudentExamResponse> result = examMapper.selectExamsByStudent(classSn, userId).stream()
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

    // ── 시험 응시: 문제지 조회 ─────────────────────────────────────────────

    @GetMapping("/exams/{examSn}")
    public ResponseEntity<StudentExamDetailResponse> getExamDetail(
            @PathVariable Long classSn,
            @PathVariable Long examSn,
            Authentication authentication) {
        String userId = authentication.getName();
        if (!isMember(classSn, userId)) return ResponseEntity.status(403).build();

        ExamDto exam = examMapper.selectExamBySn(examSn);
        if (exam == null || !classSn.equals(exam.getClassSn())) return ResponseEntity.notFound().build();

        LocalDateTime now = LocalDateTime.now();
        String status = resolveExamStatus(exam.getExamStrtDt(), exam.getExamEndDt(), now);
        if (!"ONGOING".equals(status)) return ResponseEntity.status(403).build();

        ExamTakerDto taker = examMapper.selectExamTaker(examSn, userId);
        if (taker != null) return ResponseEntity.status(409).build(); // 이미 제출

        List<ExamQuestionDto> raw = examMapper.selectExamQuestions(examSn);
        List<StudentExamDetailResponse.QuestionItem> questions = new ArrayList<>();
        for (ExamQuestionDto q : raw) {
            Map<String, Object> parsed = parseQstnCn(q.getStem());
            String stemText = (String) parsed.getOrDefault("stem", "");
            @SuppressWarnings("unchecked")
            List<String> choices = (List<String>) parsed.get("choices");
            questions.add(new StudentExamDetailResponse.QuestionItem(
                    q.getQstnSn(), q.getQstnOrdr(), stemText,
                    q.getQstnTypeCd() != null ? q.getQstnTypeCd().name() : null,
                    q.getAllocScr(), choices));
        }

        return ResponseEntity.ok(new StudentExamDetailResponse(
                examSn, exam.getExamRegNm(), exam.getExamEndDt(), questions));
    }

    // ── 시험 응시: 답안 제출 ──────────────────────────────────────────────

    @PostMapping("/exams/{examSn}/submit")
    public ResponseEntity<Void> submitExam(
            @PathVariable Long classSn,
            @PathVariable Long examSn,
            @RequestBody Map<String, List<Map<String, Object>>> body,
            Authentication authentication) {
        String userId = authentication.getName();
        if (!isMember(classSn, userId)) return ResponseEntity.status(403).build();

        ExamDto exam = examMapper.selectExamBySn(examSn);
        if (exam == null || !classSn.equals(exam.getClassSn())) return ResponseEntity.notFound().build();

        LocalDateTime now = LocalDateTime.now();
        String status = resolveExamStatus(exam.getExamStrtDt(), exam.getExamEndDt(), now);
        if (!"ONGOING".equals(status)) return ResponseEntity.status(409).build();

        if (examMapper.selectExamTaker(examSn, userId) != null) return ResponseEntity.status(409).build();

        examMapper.insertExamTaker(examSn, userId);

        List<Map<String, Object>> answers = body.get("answers");
        if (answers != null) {
            for (Map<String, Object> ans : answers) {
                Long qstnSn = ans.get("qstnSn") instanceof Number
                        ? ((Number) ans.get("qstnSn")).longValue() : null;
                String answCn = ans.get("answCn") instanceof String ? (String) ans.get("answCn") : null;
                if (qstnSn != null) {
                    examMapper.insertAnswer(examSn, userId, qstnSn, answCn);
                }
            }
        }
        return ResponseEntity.ok().build();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseQstnCn(String json) {
        if (json == null || json.isBlank()) return Map.of();
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return Map.of();
        }
    }

    private String resolveExamStatus(String strtDt, String endDt, LocalDateTime now) {
        if (strtDt == null || endDt == null) return "UPCOMING";
        LocalDateTime start = LocalDateTime.parse(strtDt, DT_FMT);
        LocalDateTime end   = LocalDateTime.parse(endDt, DT_FMT);
        if (now.isBefore(start)) return "UPCOMING";
        if (now.isAfter(end))    return "CLOSED";
        return "ONGOING";
    }

    // ── 공지사항 ──────────────────────────────────────────────────────────

    @GetMapping("/notices")
    public ResponseEntity<PageResponse<InstructorBoardDto>> getNotices(
            @PathVariable Long classSn,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        if (!isMember(classSn, authentication.getName())) return ResponseEntity.status(403).build();

        PageResponse<InstructorBoardDto> svc = instructorBoardService.getClassroomNoticeList(classSn, page, size);
        return ResponseEntity.ok(new PageResponse<>(
                svc.getItems(), svc.getTotalCount(), computeTotalPages(svc.getTotalCount(), size)));
    }

    @GetMapping("/notices/{postSn}")
    public ResponseEntity<InstructorBoardDto> getNoticeDetail(
            @PathVariable Long classSn, @PathVariable Long postSn, Authentication authentication) {
        if (!isMember(classSn, authentication.getName())) return ResponseEntity.status(403).build();
        InstructorBoardDto detail = instructorBoardService.getClassroomNoticeDetail(postSn, classSn);
        if (detail == null) return ResponseEntity.notFound().build();
        instructorBoardService.incrementViewCount(postSn);
        return ResponseEntity.ok(detail);
    }

    // ── 자료실 ────────────────────────────────────────────────────────────

    @GetMapping("/dataroom")
    public ResponseEntity<PageResponse<InstructorBoardDto>> getDataroom(
            @PathVariable Long classSn,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        if (!isMember(classSn, authentication.getName())) return ResponseEntity.status(403).build();

        PageResponse<InstructorBoardDto> svc = instructorBoardService.getClassroomDataroomList(classSn, page, size);
        return ResponseEntity.ok(new PageResponse<>(
                svc.getItems(), svc.getTotalCount(), computeTotalPages(svc.getTotalCount(), size)));
    }

    @GetMapping("/dataroom/{postSn}")
    public ResponseEntity<InstructorBoardDto> getDataroomDetail(
            @PathVariable Long classSn, @PathVariable Long postSn, Authentication authentication) {
        if (!isMember(classSn, authentication.getName())) return ResponseEntity.status(403).build();
        InstructorBoardDto detail = instructorBoardService.getClassroomDataroomDetail(postSn, classSn);
        if (detail == null) return ResponseEntity.notFound().build();
        instructorBoardService.incrementViewCount(postSn);
        return ResponseEntity.ok(detail);
    }

    // ── Q&A ───────────────────────────────────────────────────────────────

    @GetMapping("/qna")
    public ResponseEntity<PageResponse<ClassroomQnaDto>> getQna(
            @PathVariable Long classSn,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "false") boolean myOnly,
            Authentication authentication) {
        String userId = authentication.getName();
        if (!isMember(classSn, userId)) return ResponseEntity.status(403).build();

        String writerFilter = myOnly ? userId : null;
        PageResponse<ClassroomQnaDto> svc =
                instructorBoardService.getClassroomQnaList(classSn, page, size, writerFilter);
        return ResponseEntity.ok(new PageResponse<>(
                svc.getItems(), svc.getTotalCount(), computeTotalPages(svc.getTotalCount(), size)));
    }

    @GetMapping("/qna/{postSn}")
    public ResponseEntity<ClassroomQnaDto> getQnaDetail(
            @PathVariable Long classSn, @PathVariable Long postSn, Authentication authentication) {
        if (!isMember(classSn, authentication.getName())) return ResponseEntity.status(403).build();
        ClassroomQnaDto detail = instructorBoardService.getClassroomQnaDetail(postSn, classSn);
        if (detail == null) return ResponseEntity.notFound().build();
        instructorBoardService.incrementViewCount(postSn);
        return ResponseEntity.ok(detail);
    }

    @PutMapping("/qna/{postSn}")
    public ResponseEntity<Void> updateQna(
            @PathVariable Long classSn,
            @PathVariable Long postSn,
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        String userId = authentication.getName();
        if (!isMember(classSn, userId)) return ResponseEntity.status(403).build();
        String postSj = body.getOrDefault("boardSj", body.get("postSj"));
        String postCn = body.getOrDefault("boardCn", body.get("postCn"));
        try {
            instructorBoardService.updateClassroomQna(postSn, classSn, userId, postSj, postCn);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/qna")
    public ResponseEntity<Void> createQna(
            @PathVariable Long classSn,
            @RequestBody InstructorBoardDto req,
            Authentication authentication) {
        String userId = authentication.getName();
        if (!isMember(classSn, userId)) return ResponseEntity.status(403).build();

        ClassroomDetailResponse classroom = classroomService.retrieveClassroomDetail(classSn);
        req.setClassSn(classSn);
        req.setWrtrUserId(userId);
        req.setInstrUserId(classroom.getInstrUserId());
        instructorBoardService.insertClassroomQna(req);
        return ResponseEntity.ok().build();
    }
}
