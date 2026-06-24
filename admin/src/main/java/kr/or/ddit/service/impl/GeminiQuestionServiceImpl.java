package kr.or.ddit.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
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

    // ──────────────────────────────────────────────
    // 문항 생성
    // ──────────────────────────────────────────────

    @Override
    public QuestionDto generateQuestion(GeminiQuestionRequest request) {
        String prompt = buildPrompt(request);
        String rawJson = callGemini(prompt);
        return parseResponse(rawJson, request);
    }

    // ──────────────────────────────────────────────
    // 내부 유틸
    // ──────────────────────────────────────────────

    private String buildPrompt(GeminiQuestionRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("너는 수능 전문 교육 서비스의 AI 문제 생성 엔진이다. ");
        sb.append("대상: 대한민국 예비 수험생(고3/N수생). ");
        sb.append("출력 형식: 오직 순수 JSON 데이터만 출력하라. 마크다운 기호(```json)를 포함하지 말 것.\n\n");

        // 약점 컨텍스트 (classSn이 있고 데이터가 충분할 때)
        if (request.getClassSn() != null) {
            List<WeakPointDto> weakPoints = retrieveWeakPoints(request.getClassSn());
            weakPoints.stream()
                    .filter(w -> w.getSubjId().equals(request.getSubjId()))
                    .findFirst()
                    .ifPresent(target -> sb.append(String.format(
                            "학습 이력: %s 영역에서 수강생 평균 득점률 %d%%. ",
                            target.getSubjNm(), target.getAvgScoreRate())));
        }

        sb.append(String.format(
                "%s 영역의 %s 난이도 수능 실전 객관식 문제 1개를 생성하라.\n\n",
                request.getSubjNm(), request.getDifficulty().getLabel()));

        if (request.getExtraPrompt() != null && !request.getExtraPrompt().isBlank()) {
            sb.append("추가 요구사항: ").append(request.getExtraPrompt().strip()).append("\n\n");
        }

        sb.append("""
                반드시 아래 JSON 스키마를 정확히 준수하라:
                {
                  "topic": "세부 주제명",
                  "problem_text": "문제 본문 (수식은 LaTeX 사용)",
                  "options": {"A": "...", "B": "...", "C": "...", "D": "...", "E": "..."},
                  "correct_answer": "A~E 중 하나",
                  "explanation": "상세 풀이 (수식 포함 가능)",
                  "chart_data": null
                }
                도표 문제인 경우 chart_data에 Chart.js 호환 JSON을 포함하라.
                JSON 외 어떤 텍스트도 출력하지 말 것.
                """);

        return sb.toString();
    }

    private String callGemini(String prompt) {
        try {
            Client client = Client.builder().apiKey(geminiApiKey).build();
            GenerateContentResponse response = client.models.generateContent(MODEL, prompt, null);
            String text = response.text();
            if (text == null || text.isBlank()) {
                throw new FinalProjectException(ErrorCode.GEMINI_EMPTY_RESPONSE);
            }
            // 마크다운 코드블록 방어 처리
            text = text.strip();
            if (text.startsWith("```")) {
                text = text.replaceAll("^```[a-zA-Z]*\\n?", "").replaceAll("```$", "").strip();
            }
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

            @SuppressWarnings("unchecked")
            Map<String, String> options = (Map<String, String>) map.get("options");
            List<String> choices = List.of("A", "B", "C", "D", "E").stream()
                    .filter(options::containsKey)
                    .map(k -> k + ". " + options.get(k))
                    .toList();

            String chartDataJson = null;
            if (map.get("chart_data") != null) {
                chartDataJson = objectMapper.writeValueAsString(map.get("chart_data"));
            }

            QuestionDto dto = new QuestionDto();
            dto.setSubjId(request.getSubjId());
            dto.setQstnTypeCd(QuestionType.MULTIPLE_CHOICE);
            dto.setDiffCd(request.getDifficulty());
            dto.setAiGenYn("Y");
            dto.setAllocScr(BigDecimal.ONE);    // 기본 1점, 강사가 저장 전 수정 가능
            dto.setStem((String) map.get("problem_text"));
            dto.setTopic((String) map.get("topic"));
            dto.setChoices(choices);
            dto.setCorrAnswCn((String) map.get("correct_answer"));
            dto.setExplnCn((String) map.get("explanation"));
            dto.setChartData(chartDataJson);

            return dto;
        } catch (Exception e) {
            log.error("Gemini 응답 파싱 실패: {}", rawJson, e);
            throw new FinalProjectException(ErrorCode.GEMINI_PARSE_ERROR);
        }
    }
}
