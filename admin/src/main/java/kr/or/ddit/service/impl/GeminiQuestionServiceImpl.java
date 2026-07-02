package kr.or.ddit.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Schema;
import com.google.genai.types.Type;
import kr.or.ddit.finalProject.dto.exam.DifficultyStatsDto;
import kr.or.ddit.finalProject.dto.exam.ExamTrendDto;
import kr.or.ddit.finalProject.dto.exam.GeminiQuestionRequest;
import kr.or.ddit.finalProject.dto.exam.QuestionDto;
import kr.or.ddit.finalProject.dto.exam.QuestionType;
import kr.or.ddit.finalProject.dto.exam.WeakPointDto;
import kr.or.ddit.finalProject.exception.ErrorCode;
import kr.or.ddit.finalProject.exception.FinalProjectException;
import kr.or.ddit.finalProject.mapper.exam.WeakPointMapper;
import kr.or.ddit.finalProject.service.exam.GeminiQuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiQuestionServiceImpl implements GeminiQuestionService {

    private final WeakPointMapper weakPointMapper;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api-key}")
    private String geminiApiKey;

    private static final String MODEL = "gemini-3.1-flash-lite";

    // ──────────────────────────────────────────────
    // 약점 조회
    // ──────────────────────────────────────────────

    @Override
    public List<WeakPointDto> retrieveWeakPoints(Long classSn) {
        if (classSn == null) return List.of();
        return weakPointMapper.selectWeakPointsByClassSn(classSn);
    }

    @Override
    public List<DifficultyStatsDto> retrieveDifficultyStats(Long classSn) {
        if (classSn == null) return List.of();
        return weakPointMapper.selectDifficultyStatsByClassSn(classSn);
    }

    @Override
    public List<ExamTrendDto> retrieveExamTrend(Long classSn) {
        if (classSn == null) return List.of();
        return weakPointMapper.selectExamTrendByClassSn(classSn);
    }

    // ──────────────────────────────────────────────
    // 문항 생성
    // ──────────────────────────────────────────────

    @Override
    public QuestionDto generateQuestion(GeminiQuestionRequest request) {
        QuestionType type = request.getQstnTypeCd() != null
                ? request.getQstnTypeCd() : QuestionType.MULTIPLE_CHOICE;
        String prompt = buildPrompt(request);
        String rawJson = callGemini(prompt, type);
        return parseResponse(rawJson, request);
    }

    // ──────────────────────────────────────────────
    // 내부 유틸
    // ──────────────────────────────────────────────

    private String buildPrompt(GeminiQuestionRequest request) {
        QuestionType type = request.getQstnTypeCd() != null
                ? request.getQstnTypeCd() : QuestionType.MULTIPLE_CHOICE;

        StringBuilder sb = new StringBuilder();
        sb.append("너는 수능 전문 교육 서비스의 AI 문제 생성 엔진이다. ");
        sb.append("대상: 대한민국 예비 수험생(고3/N수생). ");
        sb.append("출력 형식: 오직 순수 JSON 데이터만 출력하라. 마크다운 기호(```json)를 포함하지 말 것. ");
        sb.append("수식은 반드시 LaTeX 표기만 사용하라. 유니코드 수학 기호(−, ×, ÷, ∑, √ 등)나 ");
        sb.append("한글과 LaTeX를 혼용하지 말 것. 예: a^{x-1}은 $a^{x-1}$로만 표기하라.\n\n");

        // 약점 컨텍스트 (classSn이 있고 데이터가 충분할 때)
        if (request.getClassSn() != null) {
            List<WeakPointDto> weakPoints = retrieveWeakPoints(request.getClassSn());
            if (!weakPoints.isEmpty()) {
                sb.append("수강생 약점 주제(득점률 낮은 순): ");
                weakPoints.stream()
                        .limit(3)
                        .forEach(w -> sb.append(String.format("%s(%d%%) ", w.getTopic(), w.getAvgScoreRate())));
                sb.append(". 위 약점 주제 중 하나를 중점으로 출제하라. ");
            }
        }

        sb.append(String.format(
                "%s 영역의 %s 난이도 수능 실전 %s 문제 1개를 생성하라.\n\n",
                request.getSubjNm(), request.getDifficulty().getLabel(), type.getLabel()));

        if (request.getExtraPrompt() != null && !request.getExtraPrompt().isBlank()) {
            sb.append("추가 요구사항: ").append(request.getExtraPrompt().strip()).append("\n\n");
        }

        switch (type) {
            case SHORT_ANSWER -> sb.append("""
                    반드시 아래 JSON 스키마를 정확히 준수하라:
                    {
                      "topic": "세부 주제명",
                      "problem_text": "문제 본문 (수식은 LaTeX 사용)",
                      "correct_answer": "정답 (짧은 단어/숫자/식)",
                      "explanation": "상세 풀이"
                    }
                    JSON 외 어떤 텍스트도 출력하지 말 것.
                    """);
            case ESSAY -> sb.append("""
                    반드시 아래 JSON 스키마를 정확히 준수하라:
                    {
                      "topic": "세부 주제명",
                      "problem_text": "문제 본문 (수식은 LaTeX 사용)",
                      "correct_answer": "모범 답안 (핵심 내용 요약)",
                      "explanation": "채점 기준 및 상세 해설"
                    }
                    JSON 외 어떤 텍스트도 출력하지 말 것.
                    """);
            default -> sb.append("""
                    반드시 아래 JSON 스키마를 정확히 준수하라:
                    {
                      "topic": "세부 주제명",
                      "problem_text": "문제 본문 (수식은 LaTeX 사용)",
                      "options": {"A": "...", "B": "...", "C": "...", "D": "...", "E": "..."},
                      "correct_answer": "A~E 중 하나",
                      "explanation": "상세 풀이 (수식 포함 가능)",
                      "chart_data": null
                    }
                    도표 문제인 경우 chart_data에 Chart.js 호환 JSON을 문자열로 인코딩하여 포함하라. 없으면 null.
                    JSON 외 어떤 텍스트도 출력하지 말 것.
                    """);
        }

        return sb.toString();
    }

    private Schema buildSchema(QuestionType type) {
        Type stringType = new Type(Type.Known.STRING);
        Type objectType = new Type(Type.Known.OBJECT);

        Map<String, Schema> properties = new java.util.LinkedHashMap<>();
        properties.put("topic", Schema.builder().type(stringType).build());
        properties.put("problem_text", Schema.builder().type(stringType).build());
        properties.put("correct_answer", Schema.builder().type(stringType).build());
        properties.put("explanation", Schema.builder().type(stringType).build());

        List<String> required = new java.util.ArrayList<>(
                List.of("topic", "problem_text", "correct_answer", "explanation"));

        if (type == QuestionType.MULTIPLE_CHOICE) {
            Map<String, Schema> optionProps = new java.util.LinkedHashMap<>();
            for (String key : List.of("A", "B", "C", "D", "E")) {
                optionProps.put(key, Schema.builder().type(stringType).build());
            }
            properties.put("options", Schema.builder()
                    .type(objectType)
                    .properties(optionProps)
                    .required(List.of("A", "B", "C", "D", "E"))
                    .build());
            properties.put("chart_data", Schema.builder()
                    .type(stringType)
                    .nullable(true)
                    .description("도표 문제인 경우 Chart.js 호환 JSON을 문자열로 인코딩. 없으면 null")
                    .build());
            required.add("options");
        }

        return Schema.builder()
                .type(objectType)
                .properties(properties)
                .required(required)
                .build();
    }

    private String callGemini(String prompt, QuestionType type) {
        try {
            Client client = Client.builder().apiKey(geminiApiKey).build();
            GenerateContentConfig config = GenerateContentConfig.builder()
                    .responseMimeType("application/json")
                    .responseSchema(buildSchema(type))
                    .build();
            GenerateContentResponse response = client.models.generateContent(MODEL, prompt, config);
            String text = response.text();
            if (text == null || text.isBlank()) {
                throw new FinalProjectException(ErrorCode.GEMINI_EMPTY_RESPONSE);
            }
            // 마크다운 코드블록 방어 처리
            text = text.strip();
            if (text.startsWith("```")) {
                text = text.replaceAll("^```[a-zA-Z]*\\n?", "").replaceAll("```$", "").strip();
            }
            // LaTeX 백슬래시(\log, \frac, \times, \tau 등)가 JSON 이스케이프로 오해되는 것을 방지
            // 이미 이스케이프된 \\, \", 유니코드 이스케이프(u + 16진수 4자리)는 건드리지 않고, 그 외 단일 백슬래시만 \\X로 치환
            // (기존에는 다음 글자가 b/f/n/r/t/u면 "이미 올바른 JSON 이스케이프"로 간주해 건너뛰었으나,
            //  \tau·\times처럼 그 글자로 시작하는 LaTeX 명령까지 오탐되어 깨졌고,
            //  Gemini가 스스로 \\log처럼 이중 이스케이프해 준 경우 세 번째 백슬래시가 추가되어 파싱이 깨졌다)
            text = text.replaceAll("(?<!\\\\)\\\\(?!\\\\|\"|u[0-9a-fA-F]{4})", "\\\\\\\\");
            return text;
        } catch (FinalProjectException e) {
            throw e;
        } catch (Exception e) {
            log.error("Gemini API 호출 실패", e);
            throw new FinalProjectException(ErrorCode.GEMINI_API_ERROR);
        }
    }

    private QuestionDto parseResponse(String rawJson, GeminiQuestionRequest request) {
        try {
            Map<String, Object> map = objectMapper.readValue(
                    rawJson, new TypeReference<Map<String, Object>>() {});

            QuestionType type = request.getQstnTypeCd() != null
                    ? request.getQstnTypeCd() : QuestionType.MULTIPLE_CHOICE;

            QuestionDto dto = new QuestionDto();
            dto.setSubjId(request.getSubjId());
            dto.setQstnTypeCd(type);
            dto.setDiffCd(request.getDifficulty());
            dto.setAiGenYn("Y");
            dto.setAllocScr(BigDecimal.ONE);
            dto.setStem((String) map.get("problem_text"));
            dto.setTopic((String) map.get("topic"));
            dto.setCorrAnswCn((String) map.get("correct_answer"));
            dto.setExplnCn((String) map.get("explanation"));

            if (type == QuestionType.MULTIPLE_CHOICE) {
                @SuppressWarnings("unchecked")
                Map<String, String> options = (Map<String, String>) map.get("options");
                if (options != null) {
                    List<String> choices = List.of("A", "B", "C", "D", "E").stream()
                            .filter(options::containsKey)
                            .map(k -> k + ". " + options.get(k))
                            .toList();
                    dto.setChoices(choices);
                }
                if (map.get("chart_data") != null) {
                    dto.setChartData((String) map.get("chart_data"));
                }
            }

            return dto;
        } catch (Exception e) {
            log.error("Gemini 응답 파싱 실패: {}", rawJson, e);
            throw new FinalProjectException(ErrorCode.GEMINI_PARSE_ERROR);
        }
    }
}
