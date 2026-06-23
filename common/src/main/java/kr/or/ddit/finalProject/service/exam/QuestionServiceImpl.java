package kr.or.ddit.finalProject.service.exam;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.or.ddit.finalProject.dto.exam.QuestionDto;
import kr.or.ddit.finalProject.dto.exam.QuestionSaveRequest;
import kr.or.ddit.finalProject.dto.exam.QuestionType;
import kr.or.ddit.finalProject.exception.ErrorCode;
import kr.or.ddit.finalProject.exception.FinalProjectException;
import kr.or.ddit.finalProject.mapper.exam.QuestionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestionServiceImpl implements QuestionService {

    private final QuestionMapper questionMapper;
    private final ObjectMapper objectMapper;

    // ──────────────────────────────────────────────
    // 조회
    // ──────────────────────────────────────────────

    @Override
    public List<QuestionDto> retrieveMyQuestions(String instrUserId) {
        List<QuestionDto> list = questionMapper.selectMyQuestions(instrUserId);
        list.forEach(this::parseQstnCn);
        return list;
    }

    @Override
    public List<QuestionDto> retrieveQuestionPage(String instrUserId, Long subjId, String diffCd,
                                                   int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<QuestionDto> list = questionMapper.selectQuestionPage(instrUserId, subjId, diffCd, offset, pageSize);
        list.forEach(this::parseQstnCn);
        return list;
    }

    @Override
    public int countQuestions(String instrUserId, Long subjId, String diffCd) {
        return questionMapper.countQuestions(instrUserId, subjId, diffCd);
    }

    @Override
    public QuestionDto retrieveQuestion(Long qstnSn, String instrUserId) {
        QuestionDto dto = questionMapper.selectQuestionBySn(qstnSn);
        if (dto == null || "99".equals(dto.getStatCd())) {
            throw new FinalProjectException(ErrorCode.QUESTION_NOT_FOUND);
        }
        if (!instrUserId.equals(dto.getRgtrId())) {
            throw new FinalProjectException(ErrorCode.QUESTION_ACCESS_DENIED);
        }
        parseQstnCn(dto);
        return dto;
    }

    // ──────────────────────────────────────────────
    // 등록
    // ──────────────────────────────────────────────

    @Override
    @Transactional
    public void addQuestion(String instrUserId, QuestionSaveRequest request) {
        validateQuestionRequest(request);

        QuestionDto dto = new QuestionDto();
        dto.setRgtrId(instrUserId);
        dto.setSubjId(request.getSubjId());
        dto.setQstnTypeCd(request.getQstnTypeCd());
        dto.setDiffCd(request.getDiffCd());
        dto.setAiGenYn("Y".equals(request.getAiGenYn()) ? "Y" : "N");
        dto.setAllocScr(request.getAllocScr());
        dto.setCorrAnswCn(trimOrNull(request.getCorrAnswCn()));
        dto.setExplnCn(trimOrNull(request.getExplnCn()));
        dto.setQstnCn(buildQstnCn(request));

        questionMapper.insertQuestion(dto);
    }

    // ──────────────────────────────────────────────
    // 수정
    // ──────────────────────────────────────────────

    @Override
    @Transactional
    public void modifyQuestion(Long qstnSn, String instrUserId, QuestionSaveRequest request) {
        validateQuestionRequest(request);

        QuestionDto existing = questionMapper.selectQuestionBySn(qstnSn);
        if (existing == null || "99".equals(existing.getStatCd())) {
            throw new FinalProjectException(ErrorCode.QUESTION_NOT_FOUND);
        }
        if (!existing.getRgtrId().equals(instrUserId)) {
            throw new FinalProjectException(ErrorCode.QUESTION_ACCESS_DENIED);
        }

        QuestionDto dto = new QuestionDto();
        dto.setQstnSn(qstnSn);
        dto.setLastMdfrId(instrUserId);
        dto.setSubjId(request.getSubjId());
        dto.setQstnTypeCd(request.getQstnTypeCd());
        dto.setDiffCd(request.getDiffCd());
        dto.setAllocScr(request.getAllocScr());
        dto.setCorrAnswCn(trimOrNull(request.getCorrAnswCn()));
        dto.setExplnCn(trimOrNull(request.getExplnCn()));
        dto.setQstnCn(buildQstnCn(request));

        questionMapper.updateQuestion(dto);
    }

    // ──────────────────────────────────────────────
    // 삭제
    // ──────────────────────────────────────────────

    @Override
    @Transactional
    public void removeQuestion(Long qstnSn, String instrUserId) {
        QuestionDto existing = questionMapper.selectQuestionBySn(qstnSn);
        if (existing == null || "99".equals(existing.getStatCd())) {
            throw new FinalProjectException(ErrorCode.QUESTION_NOT_FOUND);
        }
        if (!existing.getRgtrId().equals(instrUserId)) {
            throw new FinalProjectException(ErrorCode.QUESTION_ACCESS_DENIED);
        }
        questionMapper.deleteQuestion(qstnSn);
    }

    // ──────────────────────────────────────────────
    // 내부 유틸
    // ──────────────────────────────────────────────

    private void validateQuestionRequest(QuestionSaveRequest request) {
        if (request.getQstnTypeCd() == null) {
            throw new FinalProjectException(ErrorCode.BAD_REQUEST);
        }
        if (request.getStem() == null || request.getStem().isBlank()) {
            throw new FinalProjectException(ErrorCode.BAD_REQUEST);
        }
    }

    /**
     * QuestionSaveRequest를 유형에 맞는 QSTN_CN JSON 문자열로 직렬화합니다.
     *
     * MULTIPLE_CHOICE: {"stem":"...","topic":"...","choices":["A. ..."],"chartData":null}
     * SHORT_ANSWER:    {"stem":"...","topic":"...","maxLength":N}
     * ESSAY:           {"stem":"...","topic":"...","scoringCriteria":"..."}
     */
    private String buildQstnCn(QuestionSaveRequest request) {
        try {
            Map<String, Object> json = new LinkedHashMap<>();
            json.put("stem", request.getStem());
            json.put("topic", trimOrNull(request.getTopic()));

            if (QuestionType.MULTIPLE_CHOICE == request.getQstnTypeCd()) {
                List<String> choices = request.getChoices() == null ? new ArrayList<>()
                        : request.getChoices().stream()
                                .filter(c -> c != null && !c.isBlank())
                                .collect(Collectors.toList());
                json.put("choices", choices);
                json.put("chartData", null);

            } else if (QuestionType.SHORT_ANSWER == request.getQstnTypeCd()) {
                if (request.getMaxLength() != null && request.getMaxLength() > 0) {
                    json.put("maxLength", request.getMaxLength());
                }

            } else if (QuestionType.ESSAY == request.getQstnTypeCd()) {
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
     * QuestionDto의 qstnCn JSON을 stem/choices 등의 필드로 파싱합니다.
     * 파싱 실패 시 stem = "(파싱 오류)"로 설정합니다.
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
            dto.setTopic((String) parsed.get("topic"));
            dto.setChartData(parsed.get("chartData") != null
                    ? objectMapper.writeValueAsString(parsed.get("chartData")) : null);

            if (dto.getQstnTypeCd() != null) {
                switch (dto.getQstnTypeCd()) {
                    case MULTIPLE_CHOICE -> {
                        @SuppressWarnings("unchecked")
                        List<String> choices = (List<String>) parsed.get("choices");
                        dto.setChoices(choices);
                    }
                    case SHORT_ANSWER -> {
                        Object maxLen = parsed.get("maxLength");
                        if (maxLen instanceof Number n) dto.setMaxLength(n.intValue());
                    }
                    case ESSAY -> dto.setScoringCriteria((String) parsed.get("scoringCriteria"));
                }
            }
        } catch (JsonProcessingException e) {
            dto.setStem("(파싱 오류)");
        }
    }

    private String trimOrNull(String value) {
        return (value != null && !value.isBlank()) ? value : null;
    }
}
