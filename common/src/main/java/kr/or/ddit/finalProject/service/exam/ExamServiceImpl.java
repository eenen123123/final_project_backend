package kr.or.ddit.finalProject.service.exam;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.or.ddit.finalProject.dto.exam.ExamDto;
import kr.or.ddit.finalProject.dto.exam.ExamQuestionDto;
import kr.or.ddit.finalProject.dto.exam.ExamSaveRequest;
import kr.or.ddit.finalProject.dto.exam.ExamTakerDto;
import kr.or.ddit.finalProject.exception.ErrorCode;
import kr.or.ddit.finalProject.exception.FinalProjectException;
import kr.or.ddit.finalProject.mapper.exam.ExamMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExamServiceImpl implements ExamService {

    private final ExamMapper examMapper;
    private final ObjectMapper objectMapper;

    // ──────────────────────────────────────────────
    // 조회
    // ──────────────────────────────────────────────

    @Override
    public List<ExamDto> retrieveMyExams(String instrUserId) {
        List<ExamDto> exams = examMapper.selectMyExams(instrUserId);
        exams.forEach(exam -> {
            List<ExamQuestionDto> questions = examMapper.selectExamQuestions(exam.getExamSn());
            questions.forEach(eq -> eq.setStem(parseStemFromJson(eq.getStem())));
            exam.setQuestions(questions);
        });
        return exams;
    }

    @Override
    public ExamDto retrieveExamDetail(Long examSn, String instrUserId) {
        ExamDto exam = examMapper.selectExamBySn(examSn);
        if (exam == null || "99".equals(exam.getExamStatCd())) {
            throw new FinalProjectException(ErrorCode.EXAM_NOT_FOUND);
        }
        if (!exam.getExamChrgUserId().equals(instrUserId)) {
            throw new FinalProjectException(ErrorCode.EXAM_ACCESS_DENIED);
        }
        List<ExamQuestionDto> questions = examMapper.selectExamQuestions(examSn);
        questions.forEach(eq -> eq.setStem(parseStemFromJson(eq.getStem())));
        exam.setQuestions(questions);
        return exam;
    }

    // ──────────────────────────────────────────────
    // 등록
    // ──────────────────────────────────────────────

    @Override
    @Transactional
    public void addExam(String instrUserId, ExamSaveRequest request) {
        validateExamRequest(request);

        ExamDto dto = new ExamDto();
        dto.setExamChrgUserId(instrUserId);
        dto.setExamRegNm(request.getExamRegNm());
        dto.setExamTypeCd(trimOrNull(request.getExamTypeCd()));
        dto.setExamStrtDt(trimOrNull(request.getExamStrtDt()));
        dto.setExamEndDt(trimOrNull(request.getExamEndDt()));
        dto.setClassSn(request.getClassSn());

        examMapper.insertExam(dto);
        insertExamQuestions(dto.getExamSn(), request.getQstnSnList());
    }

    // ──────────────────────────────────────────────
    // 수정
    // ──────────────────────────────────────────────

    @Override
    @Transactional
    public void modifyExam(Long examSn, String instrUserId, ExamSaveRequest request) {
        validateExamRequest(request);

        ExamDto existing = examMapper.selectExamBySn(examSn);
        if (existing == null || "99".equals(existing.getExamStatCd())) {
            throw new FinalProjectException(ErrorCode.EXAM_NOT_FOUND);
        }
        if (!existing.getExamChrgUserId().equals(instrUserId)) {
            throw new FinalProjectException(ErrorCode.EXAM_ACCESS_DENIED);
        }

        ExamDto dto = new ExamDto();
        dto.setExamSn(examSn);
        dto.setExamRegNm(request.getExamRegNm());
        dto.setExamTypeCd(trimOrNull(request.getExamTypeCd()));
        dto.setExamStrtDt(trimOrNull(request.getExamStrtDt()));
        dto.setExamEndDt(trimOrNull(request.getExamEndDt()));
        dto.setClassSn(request.getClassSn());

        examMapper.updateExam(dto);
        examMapper.deleteExamQuestions(examSn);
        insertExamQuestions(examSn, request.getQstnSnList());
    }

    // ──────────────────────────────────────────────
    // 삭제
    // ──────────────────────────────────────────────

    @Override
    @Transactional
    public void removeExam(Long examSn, String instrUserId) {
        ExamDto existing = examMapper.selectExamBySn(examSn);
        if (existing == null || "99".equals(existing.getExamStatCd())) {
            throw new FinalProjectException(ErrorCode.EXAM_NOT_FOUND);
        }
        if (!existing.getExamChrgUserId().equals(instrUserId)) {
            throw new FinalProjectException(ErrorCode.EXAM_ACCESS_DENIED);
        }
        examMapper.deleteExam(examSn);
        examMapper.deleteExamQuestions(examSn);
        examMapper.deleteExamTakers(examSn);
    }

    @Override
    public List<ExamDto> retrieveExamsByClassSn(Long classSn) {
        return examMapper.selectExamsByClassSn(classSn);
    }

    @Override
    public List<ExamTakerDto> retrieveTakers(Long examSn, String instrUserId) {
        ExamDto exam = examMapper.selectExamBySn(examSn);
        if (exam == null || "99".equals(exam.getExamStatCd())) {
            throw new FinalProjectException(ErrorCode.EXAM_NOT_FOUND);
        }
        if (!exam.getExamChrgUserId().equals(instrUserId)) {
            throw new FinalProjectException(ErrorCode.EXAM_ACCESS_DENIED);
        }
        return examMapper.selectTakersByExamSn(examSn);
    }

    @Override
    public List<ExamTakerDto> retrieveTakersDirectly(Long examSn) {
        return examMapper.selectTakersByExamSn(examSn);
    }

    @Override
    public List<ExamTakerDto> retrieveTakersWithScore(Long examSn) {
        return examMapper.selectTakersWithScore(examSn);
    }

    @Override
    public List<kr.or.ddit.finalProject.dto.exam.StudentAnswerDto> retrieveStudentAnswers(Long examSn, String userId) {
        List<kr.or.ddit.finalProject.dto.exam.StudentAnswerDto> answers =
                examMapper.selectStudentAnswers(examSn, userId);
        for (kr.or.ddit.finalProject.dto.exam.StudentAnswerDto ans : answers) {
            Map<String, Object> parsed = parseQstnCnFull(ans.getQstnCn());
            ans.setStem((String) parsed.getOrDefault("stem", ""));
            @SuppressWarnings("unchecked")
            List<String> choices = (List<String>) parsed.get("choices");
            ans.setChoices(choices);
            // corrAnswCn 알파벳(A/B/C/D) → 1-based 숫자로 정규화
            ans.setCorrAnswCn(normalizeAnswerKey(ans.getCorrAnswCn()));
            // 객관식: 미리 정답 여부 계산
            if ("MULTIPLE_CHOICE".equals(ans.getQstnTypeCd()) && ans.getSbmtAnswSn() != null) {
                String corrNorm = ans.getCorrAnswCn(); // 이미 정규화됨
                String sbmtNorm = normalizeAnswerKey(ans.getSbmtAnswCn());
                ans.setCorrect(corrNorm != null && sbmtNorm != null
                        && toSortedSet(corrNorm).equals(toSortedSet(sbmtNorm)));
            }
        }
        return answers;
    }

    @Override
    @Transactional
    public void gradeStudentExam(Long examSn, String userId,
                                 java.util.Map<Long, java.math.BigDecimal> scores,
                                 String graderId) {
        // 주관식/서술형 점수 저장
        for (java.util.Map.Entry<Long, java.math.BigDecimal> entry : scores.entrySet()) {
            examMapper.updateAnswerScore(entry.getKey(), entry.getValue(), graderId);
        }
        // 객관식 자동채점 (제출된 답안이 있는 경우에만)
        List<kr.or.ddit.finalProject.dto.exam.StudentAnswerDto> answers =
                examMapper.selectStudentAnswers(examSn, userId);
        for (kr.or.ddit.finalProject.dto.exam.StudentAnswerDto ans : answers) {
            if (!"MULTIPLE_CHOICE".equals(ans.getQstnTypeCd())) continue;
            if (ans.getSbmtAnswSn() == null) continue;
            String corrNorm = normalizeAnswerKey(ans.getCorrAnswCn());
            String sbmtNorm = normalizeAnswerKey(ans.getSbmtAnswCn());
            boolean correct = corrNorm != null && sbmtNorm != null
                    && toSortedSet(corrNorm).equals(toSortedSet(sbmtNorm));
            BigDecimal mcScore = correct ? ans.getAllocScr() : BigDecimal.ZERO;
            examMapper.updateAnswerScore(ans.getSbmtAnswSn(), mcScore, graderId);
        }
        examMapper.updateExamTakerTotalScore(examSn, userId);
    }

    @Override
    public List<kr.or.ddit.finalProject.dto.classroom.StudentExamDto> retrieveExamsByStudent(Long classSn, String userId) {
        return examMapper.selectExamsByStudent(classSn, userId);
    }

    // ──────────────────────────────────────────────
    // 내부 유틸
    // ──────────────────────────────────────────────

    private void validateExamRequest(ExamSaveRequest request) {
        if (request.getExamRegNm() == null || request.getExamRegNm().isBlank()) {
            throw new FinalProjectException(ErrorCode.BAD_REQUEST);
        }
        if (request.getQstnSnList() == null || request.getQstnSnList().isEmpty()) {
            throw new FinalProjectException(ErrorCode.BAD_REQUEST);
        }
    }

    @Override
    public int countPendingGradesByClassSn(Long classSn) {
        return examMapper.countPendingGradesByClassSn(classSn);
    }

    /**
     * 선택된 문항 목록을 EXAM_QUESTION 테이블에 순서대로 삽입합니다.
     */
    private void insertExamQuestions(Long examSn, List<Long> qstnSnList) {
        if (qstnSnList == null || qstnSnList.isEmpty()) return;
        for (int i = 0; i < qstnSnList.size(); i++) {
            ExamQuestionDto eq = new ExamQuestionDto();
            eq.setExamSn(examSn);
            eq.setQstnSn(qstnSnList.get(i));
            eq.setQstnOrdr(i + 1);
            examMapper.insertExamQuestion(eq);
        }
    }

    /**
     * QSTN_CN JSON에서 stem 필드만 추출합니다.
     * selectExamQuestions 쿼리가 QSTN_CN을 stem 별칭으로 반환하므로 한 번 더 파싱합니다.
     */
    private String parseStemFromJson(String qstnCnJson) {
        if (qstnCnJson == null || qstnCnJson.isBlank()) return "";
        try {
            Map<String, Object> parsed = objectMapper.readValue(
                    qstnCnJson, new TypeReference<Map<String, Object>>() {});
            return (String) parsed.getOrDefault("stem", "");
        } catch (JsonProcessingException e) {
            return "(파싱 오류)";
        }
    }

    private String trimOrNull(String value) {
        return (value != null && !value.isBlank()) ? value : null;
    }

    /** QSTN_CN JSON 전체 파싱 (stem + choices) */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseQstnCnFull(String json) {
        if (json == null || json.isBlank()) return Map.of();
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            return Map.of("stem", json);
        }
    }

    /**
     * 정답 키를 1-based 숫자 문자열로 정규화.
     * "C,D" → "3,4", "1,3" → "1,3", "A" → "1"
     */
    private String normalizeAnswerKey(String raw) {
        if (raw == null || raw.isBlank()) return null;
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .map(token -> {
                    if (token.length() == 1 && Character.isLetter(token.charAt(0))) {
                        return String.valueOf(Character.toUpperCase(token.charAt(0)) - 'A' + 1);
                    }
                    return token;
                })
                .sorted()
                .collect(Collectors.joining(","));
    }

    /** 정규화된 답안 문자열을 정렬된 숫자 집합으로 변환 */
    private Set<String> toSortedSet(String normalized) {
        return Arrays.stream(normalized.split(","))
                .map(String::trim)
                .collect(Collectors.toCollection(TreeSet::new));
    }
}
