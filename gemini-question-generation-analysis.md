# Gemini 문항 자동생성 기능 분석

## 기능 개요

강사가 관리자 페이지에서 과목·난이도·문항 유형을 지정하면, Google Gemini API(`gemini-3.1-flash-lite`)로 수능 실전 문제 1개를 자동 생성해서 문항 등록 폼에 채워주는 기능이다. 선택적으로 특정 클래스룸(`classSn`)의 **수강생 약점 주제**를 함께 넘겨서, "이 반은 로그함수가 약하다" 같은 데이터 기반 맞춤 출제도 가능하다.

## 요청/응답 흐름

1. **컨트롤러**: [InstructorQuestionController.java:169-174](admin/src/main/java/kr/or/ddit/controller/instructor/InstructorQuestionController.java#L169-L174)
   - `POST /instructor/questions/ai/generate` → `GeminiQuestionRequest`를 받아서 `generateQuestion()` 호출
   - 부가 API: `/ai/subjects`(과목 목록), `/ai/weak-points`(약점 주제 조회, 프론트에서 미리보기용으로 사용하는 것으로 추정)

2. **요청 DTO**: [GeminiQuestionRequest.java](common/src/main/java/kr/or/ddit/finalProject/dto/exam/GeminiQuestionRequest.java)
   - `classSn`(선택), `subjId`, `subjNm`, `difficulty`, `qstnTypeCd`(기본값 객관식), `extraPrompt`(강사 추가 요구사항)

3. **서비스 구현**: [GeminiQuestionServiceImpl.java](admin/src/main/java/kr/or/ddit/service/impl/GeminiQuestionServiceImpl.java)
   - `generateQuestion()` = `buildPrompt()` → `callGemini()` → `parseResponse()` 3단계

## 핵심 로직 상세

### ① 프롬프트 생성 (`buildPrompt`, L76-144)

- 역할 지정: "수능 전문 AI 문제 생성 엔진"
- 출력 형식 강제: 순수 JSON만 출력, 마크다운 코드블록(\`\`\`json) 금지
- 수식은 **LaTeX만** 사용하도록 강제(유니코드 수학 기호·한글 혼용 금지) — 프론트에서 렌더링 일관성을 확보하기 위한 목적으로 보인다
- `classSn`이 있으면 `retrieveWeakPoints()`로 해당 반의 약점 주제 상위 3개(득점률이 낮은 순)를 조회해 프롬프트에 삽입하고, "이 중 하나를 중점으로 출제하라"고 지시한다
- 문항 유형(`SHORT_ANSWER`/`ESSAY`/객관식 기본값)별로 요구하는 JSON 스키마가 다르며, 텍스트 블록으로 하드코딩되어 있다
  - 객관식은 `options(A~E)`, `chart_data`(Chart.js 호환 JSON, 도표 문제용)까지 요구한다

### ② Gemini 호출 (`callGemini`, `buildSchema`)

- `com.google.genai.Client`로 API 키(`gemini.api-key` 프로퍼티)를 이용해 호출한다
- **구조화 출력(Structured Output) 강제**: `GenerateContentConfig`에 `responseMimeType("application/json")`과 문항 유형별 `responseSchema`(`buildSchema()`)를 지정해서, Gemini가 API 차원에서 정해진 JSON 스키마(필드명·타입·필수값)를 벗어나지 않도록 강제한다. 객관식은 `options(A~E)`, `chart_data`(nullable string)까지 스키마에 포함.
- 이 덕분에 마크다운 코드블록으로 감싸거나 필드가 누락/변형된 형태로 응답하는 경우가 크게 줄어든다.
- 그래도 방어적으로 남겨둔 후처리:
  - 마크다운 코드블록 방어 제거(\`\`\`json으로 감싸서 오는 경우 대비)
  - **LaTeX 백슬래시 이스케이프 처리**: `\log`, `\frac`, `\times` 같은 LaTeX 명령이 JSON 파싱 시 잘못된 이스케이프로 깨지는 것을 막기 위한 정규식. 이미 올바르게 이스케이프된 `\\`, `\"`, `\uXXXX`는 건드리지 않고 그 외 단일 백슬래시만 `\\X`로 치환한다. 구조화 출력 도입 후에는 Gemini가 이미 유효한 JSON을 주기 때문에 대부분 아무 것도 바꾸지 않는 안전망(no-op)으로 동작한다.

> **참고 — 과거 버그(수정 완료)**: 초기 버전의 정규식은 "다음 글자가 `b/f/n/r/t/u`면 이미 올바른 JSON 이스케이프로 간주해 건너뛴다"는 방식이었는데, ① `\tau`·`\times`처럼 저 글자로 시작하는 LaTeX 명령까지 오탐해 깨지고, ② Gemini가 스스로 `\\log`처럼 이미 이중 이스케이프해서 준 경우 세 번째 백슬래시가 추가되어 `Unrecognized character escape` 파싱 에러가 났다. 이 문제는 수식(LaTeX)이 등장하는 수학/과학 계열 문항에서만 발생 확률이 높았다. `(?<!\\)\\(?!\\|"|u[0-9a-fA-F]{4})` 정규식으로 교체하고, 근본적으로는 구조화 출력을 도입해 재발 가능성을 크게 낮췄다.

### ③ 응답 파싱 (`parseResponse`)

- Gemini가 준 JSON을 `Map`으로 파싱한 뒤 `QuestionDto`로 변환한다
- `aiGenYn = "Y"` 플래그로 AI 생성 문항임을 표시한다(DB 컬럼, 어드민 UI에서 AI 생성 뱃지 표시용으로 추정)
- 배점(`allocScr`)은 일단 1점으로 고정되며, 이후 강사가 수정 가능한 구조로 보인다
- 객관식이면 `options` Map을 `"A. ..."` 형태의 문자열 리스트로 변환하고, `chart_data`는 스키마상 이미 문자열(JSON 인코딩)로 오므로 그대로 저장한다

## 예외 처리

`ErrorCode`에 다음 세 가지로 구분해서 던진다.

- `GEMINI_EMPTY_RESPONSE` — AI가 빈 응답을 반환한 경우
- `GEMINI_API_ERROR` — API 호출 자체가 실패한 경우
- `GEMINI_PARSE_ERROR` — 응답 JSON 파싱에 실패한 경우

빈 응답, API 호출 실패, JSON 파싱 실패를 각각 다르게 잡아낸다.

## 한 줄 요약

강사가 과목·난이도·유형을 고르면, 필요시 해당 반 수강생들의 취약 주제 데이터를 프롬프트에 함께 넣어서 Gemini에게 수능 실전 문제 1개를 JSON 형식으로 생성시키고, 그 결과를 문항 등록 폼에 자동으로 채워주는 기능이다. 수식은 LaTeX로만 나오도록 프롬프트와 후처리에서 강제하고 있다.
