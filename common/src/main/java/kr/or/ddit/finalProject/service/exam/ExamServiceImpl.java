package kr.or.ddit.finalProject.service.exam;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.or.ddit.finalProject.dto.exam.ExamDto;
import kr.or.ddit.finalProject.dto.exam.ExamQuestionDto;
import kr.or.ddit.finalProject.dto.exam.ExamSaveRequest;
import kr.or.ddit.finalProject.dto.exam.QuestionDto;
import kr.or.ddit.finalProject.dto.exam.QuestionSaveRequest;
import kr.or.ddit.finalProject.exception.ErrorCode;
import kr.or.ddit.finalProject.exception.FinalProjectException;
import kr.or.ddit.finalProject.mapper.exam.ExamMapper;
import kr.or.ddit.finalProject.mapper.exam.QuestionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExamServiceImpl implements ExamService {

    private final QuestionMapper questionMapper;
    private final ExamMapper examMapper;
    private final ObjectMapper objectMapper; // Spring Boot 자동 구성 빈

    // ──────────────────────────────────────────────
    // 문항 조회
    // ──────────────────────────────────────────────

    @Override
    public List<QuestionDto> retrieveMyQuestions(String instrUserId) {
        List<QuestionDto> list = questionMapper.selectMyQuestions(instrUserId);
        // QSTN_CN JSON을 stem/choices 필드로 파싱
        list.forEach(this::parseQstnCn);
        return list;
    }

    // ──────────────────────────────────────────────
    // 문항 등록
    // ──────────────────────────────────────────────

    @Override
    @Transactional
    public void addQuestion(String instrUserId, QuestionSaveRequest request) {
        validateQuestionRequest(request);

        QuestionDto dto = new QuestionDto();
        dto.setRgtrId(instrUserId);
        dto.setQstnTypeCd(request.getQstnTypeCd());
        dto.setAllocScr(request.getAllocScr());
        dto.setCorrAnswCn(trimOrNull(request.getCorrAnswCn()));
        dto.setExplnCn(trimOrNull(request.getExplnCn()));
        dto.setSubjDtclCd(trimOrNull(request.getSubjDtclCd()));
        // stem + choices → JSON 직렬화
        dto.setQstnCn(buildQstnCn(request));

        questionMapper.insertQuestion(dto);
    }

    // ──────────────────────────────────────────────
    // 문항 수정
    // ──────────────────────────────────────────────

    @Override
    @Transactional
    public void modifyQuestion(Long qstnSn, String instrUserId, QuestionSaveRequest request) {
        validateQuestionRequest(request);

        QuestionDto existing = questionMapper.selectQuestionBySn(qstnSn);
        if (existing == null || "99".equals(existing.getStatCd())) {
            throw new FinalProjectException(ErrorCode.QUESTION_NOT_FOUND);
        }
        // 본인 문항인지 확인 (RGTR_ID = instrUserId)
        if (!existing.getRgtrId().equals(instrUserId)) {
            throw new FinalProjectException(ErrorCode.QUESTION_ACCESS_DENIED);
        }

        QuestionDto dto = new QuestionDto();
        dto.setQstnSn(qstnSn);
        dto.setLastMdfrId(instrUserId);
        dto.setQstnTypeCd(request.getQstnTypeCd());
        dto.setAllocScr(request.getAllocScr());
        dto.setCorrAnswCn(trimOrNull(request.getCorrAnswCn()));
        dto.setExplnCn(trimOrNull(request.getExplnCn()));
        dto.setQstnCn(buildQstnCn(request));

        questionMapper.updateQuestion(dto);
    }

    // ──────────────────────────────────────────────
    // 문항 논리 삭제
    // ──────────────────────────────────────────────

    @Override
    @Transactional
    public void removeQuestion(Long qstnSn, String instrUserId) {
        QuestionDto existing = questionMapper.selectQuestionBySn(qstnSn);
        if (existing == null || "99".equals(existing.getStatCd())) {
            throw new FinalProjectException(ErrorCode.QUESTION_NOT_FOUND);
        }
        // 본인 문항인지 확인
        if (!existing.getRgtrId().equals(instrUserId)) {
            throw new FinalProjectException(ErrorCode.QUESTION_ACCESS_DENIED);
        }

        questionMapper.deleteQuestion(qstnSn);
    }

    // ──────────────────────────────────────────────
    // 시험 조회
    // ──────────────────────────────────────────────

    @Override
    public List<ExamDto> retrieveMyExams(String instrUserId) {
        List<ExamDto> exams = examMapper.selectMyExams(instrUserId);

        // 각 시험의 포함 문항 목록을 조회해 questions 필드에 채웁니다.
        // 시험 상세 모달에서 문항 목록을 표시하기 위해 사용됩니다.
        // 강사 1인의 시험 수가 많지 않아 N+1 조회를 허용합니다.
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

        // 배정된 문항 목록 조회 및 JSON 파싱 (stem 필드 채우기)
        List<ExamQuestionDto> questions = examMapper.selectExamQuestions(examSn);
        questions.forEach(eq -> eq.setStem(parseStemFromJson(eq.getStem())));
        exam.setQuestions(questions);

        return exam;
    }

    // ──────────────────────────────────────────────
    // 시험 등록
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

        examMapper.insertExam(dto); // dto.examSn에 생성된 PK 주입됨

        // 선택된 문항들을 EXAM_QUESTION에 삽입
        insertExamQuestions(dto.getExamSn(), request.getQstnSnList());
    }

    // ──────────────────────────────────────────────
    // 시험 수정
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

        // 기존 문항 배정 전체 삭제 후 재삽입 (단순하고 안전한 방식)
        examMapper.deleteExamQuestions(examSn);
        insertExamQuestions(examSn, request.getQstnSnList());
    }

    // ──────────────────────────────────────────────
    // 시험 논리 삭제
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
        // EXAM_QUESTION도 함께 삭제 (논리 삭제된 시험의 연결 행 정리)
        examMapper.deleteExamQuestions(examSn);
    }

    // ──────────────────────────────────────────────
    // 내부 유틸 메서드
    // ──────────────────────────────────────────────

    /**
     * 문항 유형 코드와 문항 본문 유효성 검증.
     *
     * @throws FinalProjectException BAD_REQUEST — 형식이 잘못된 경우
     */
    private void validateQuestionRequest(QuestionSaveRequest request) {
        if (request.getQstnTypeCd() == null ||
                !List.of("01", "02", "03").contains(request.getQstnTypeCd())) {
            throw new FinalProjectException(ErrorCode.BAD_REQUEST);
        }
        if (request.getStem() == null || request.getStem().isBlank()) {
            throw new FinalProjectException(ErrorCode.BAD_REQUEST);
        }
    }

    /**
     * 시험명 유효성 검증.
     *
     * @throws FinalProjectException BAD_REQUEST — 시험명이 없는 경우
     */
    private void validateExamRequest(ExamSaveRequest request) {
        if (request.getExamRegNm() == null || request.getExamRegNm().isBlank()) {
            throw new FinalProjectException(ErrorCode.BAD_REQUEST);
        }
    }

    /**
     * QuestionSaveRequest를 유형에 맞는 QSTN_CN JSON 문자열로 직렬화합니다.
     *
     * 객관식(01): {"stem":"...","choices":["보기1",...]}
     * 주관식(02): {"stem":"...","maxLength":N}         ← maxLength 없으면 생략
     * 서술형(03): {"stem":"...","scoringCriteria":"..."} ← scoringCriteria 없으면 생략
     */
    private String buildQstnCn(QuestionSaveRequest request) {
        try {
            Map<String, Object> json = new LinkedHashMap<>();
            json.put("stem", request.getStem());

            if ("01".equals(request.getQstnTypeCd())) {
                // 객관식: 비어있지 않은 보기만 수집
                List<String> choices = new ArrayList<>();
                if (request.getChoice1() != null && !request.getChoice1().isBlank()) choices.add(request.getChoice1());
                if (request.getChoice2() != null && !request.getChoice2().isBlank()) choices.add(request.getChoice2());
                if (request.getChoice3() != null && !request.getChoice3().isBlank()) choices.add(request.getChoice3());
                if (request.getChoice4() != null && !request.getChoice4().isBlank()) choices.add(request.getChoice4());
                if (!choices.isEmpty()) json.put("choices", choices);

            } else if ("02".equals(request.getQstnTypeCd())) {
                // 주관식(단답형): 최대 글자 수가 설정된 경우에만 저장
                if (request.getMaxLength() != null && request.getMaxLength() > 0) {
                    json.put("maxLength", request.getMaxLength());
                }

            } else if ("03".equals(request.getQstnTypeCd())) {
                // 서술형(장문형): 채점 기준이 입력된 경우에만 저장
                if (request.getScoringCriteria() != null && !request.getScoringCriteria().isBlank()) {
                    json.put("scoringCriteria", request.getScoringCriteria());
                }
            }

            return objectMapper.writeValueAsString(json);
        } catch (JsonProcessingException e) {
            throw new FinalProjectException(ErrorCode.JSON_PROCESSING_FAILED);
        }
    }

    /**
     * QuestionDto의 qstnCn JSON을 각 필드로 파싱합니다.
     * 파싱 실패 시 stem = "(파싱 오류)"로 설정합니다 (서비스 중단 방지).
     *
     * 객관식(01) → stem, choices
     * 주관식(02) → stem, maxLength
     * 서술형(03) → stem, scoringCriteria
     */
    private void parseQstnCn(QuestionDto dto) {
        String json = dto.getQstnCn();
        if (json == null || json.isBlank()) {
            dto.setStem("");
            return;
        }
        try {
            Map<String, Object> parsed = objectMapper.readValue(
                    json, new TypeReference<Map<String, Object>>() {});
            dto.setStem((String) parsed.getOrDefault("stem", ""));

            if ("01".equals(dto.getQstnTypeCd())) {
                @SuppressWarnings("unchecked")
                List<String> choices = (List<String>) parsed.get("choices");
                dto.setChoices(choices);

            } else if ("02".equals(dto.getQstnTypeCd())) {
                Object maxLen = parsed.get("maxLength");
                if (maxLen instanceof Number) {
                    dto.setMaxLength(((Number) maxLen).intValue());
                }

            } else if ("03".equals(dto.getQstnTypeCd())) {
                dto.setScoringCriteria((String) parsed.get("scoringCriteria"));
            }

        } catch (JsonProcessingException e) {
            dto.setStem("(파싱 오류)");
        }
    }

    /**
     * ExamQuestionDto.stem 필드에 임시 저장된 QSTN_CN JSON에서 stem만 추출합니다.
     * selectExamQuestions 쿼리에서 QSTN_CN을 stem 컬럼 별칭으로 받아오므로
     * 이 메서드로 한 번 더 파싱합니다.
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

    /**
     * 선택된 문항 목록을 EXAM_QUESTION 테이블에 순서대로 삽입합니다.
     * 목록이 null이거나 비어있으면 삽입하지 않습니다.
     */
    private void insertExamQuestions(Long examSn, List<Long> qstnSnList) {
        if (qstnSnList == null || qstnSnList.isEmpty()) return;

        for (int i = 0; i < qstnSnList.size(); i++) {
            ExamQuestionDto eq = new ExamQuestionDto();
            eq.setExamSn(examSn);
            eq.setQstnSn(qstnSnList.get(i));
            eq.setQstnOrdr(i + 1); // 1부터 시작
            examMapper.insertExamQuestion(eq);
        }
    }

    /** null이거나 공백인 문자열을 null로 변환합니다. */
    private String trimOrNull(String value) {
        return (value != null && !value.isBlank()) ? value : null;
    }
}
