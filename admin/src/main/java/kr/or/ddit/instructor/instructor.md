# instructor 커리큘럼 관리 기능 작업 기록

## 최종 상태 요약

---

### 1. 프로젝트 개요

`instructor` 커리큘럼 관리 기능은 **정상 동작 확인 완료 상태**입니다.
Mapper XML/DTO/DB 정합성, 컨트롤러 인증, 시퀀스 중복 PK 문제, UI 디자인, 관심사 분리까지 순차적으로 정리됐습니다.

---

### 2. 해결된 핵심 이슈

#### A. Mapper XML과 DTO/DB 컬럼 정합성 정리

- `InstructorCurriculumMapper.xml` 에서 실제 DB 컬럼명 기준으로 매핑을 정리했습니다.
- 실제 컬럼명: `RGTR_ID`, `REG_DT`, `LAST_MDFR_ID`, `MDFCN_DT`
- 기존에 사용하던 `REG_ID`, `MOD_ID`, `REG_DATE`, `MOD_DATE` 형태는 제거됐습니다.

#### B. 컨트롤러 인증 처리 안정화

- `InstructorCurriculumController` 에서 `SecurityContextHolder` 필드 초기화 방식 대신, 요청 시점의 `Authentication` 에서 로그인 ID를 받아오도록 수정했습니다.
- 저장/수정 시 감사 필드 주입 로직을 정리했습니다.

#### C. 템플릿 안전성 개선

- `curriculum_main.html` 의 `th:onclick` 에 제목 문자열을 직접 삽입하던 방식을 `escapeJavaScript` 로 변경했습니다.
- 제목에 특수문자가 포함되어도 JS 문자열 깨짐 위험을 줄였습니다.

#### D. ORA-00904 수정

- `GET_NEXT_DETAIL_SEQ()` 를 실제 DB에 존재하는 `SEQ_CURRICULUM_DETAIL.NEXTVAL` 로 변경했습니다.

#### E. ORA-00001 근본 해결 (INSERT ALL → 단건 INSERT 루프)

Oracle에서 `INSERT ALL ... SELECT * FROM DUAL` 구문은 단일 SQL 문장이므로 `NEXTVAL` 이 문장당 1회만 증가합니다.
결과적으로 모든 행이 동일한 `DETAIL_ID` 를 받아 PK 중복(`ORA-00001`)이 발생했습니다.

**해결:** `insertDetailList` (INSERT ALL) 를 폐기하고 단건 `insertDetail` 로 교체 후 서비스 레이어에서 루프 처리합니다.

수정된 파일:

| 파일 | 변경 내용 |
| --- | --- |
| `InstructorCurriculumMapper.xml` | `insertDetailList` 제거 → `insertDetail` (단건 INSERT) 추가 |
| `InstructorCurriculumMapper.java` | 메서드 시그니처 교체, `@Param` 명칭 정합성 정리 |
| `InstructorCurriculumServiceImpl.java` | `createCurriculum()`, `modifyCurriculum()` 양쪽 모두 루프 처리로 변경 |

> 시퀀스 자체는 정상입니다. 롤백되어도 `NEXTVAL` 소비는 취소되지 않으므로 번호에 공백(gap)이 생길 수 있으나, PK는 연속일 필요가 없어 문제되지 않습니다. 시퀀스 재생성은 불필요합니다.

---

### 3. UI 개선

#### AG Grid 테마 재정의

페이지 전체 디자인(sky/slate 계열, Noto Sans KR)과 조화롭도록 `ag-theme-alpine` CSS 변수를 재정의했습니다.

- 폰트: `"Noto Sans KR", "Apple SD Gothic Neo", sans-serif` (Alpine 기본값 override용 `!important` 병행 적용)
- 행 높이 `42px`, 헤더 높이 `40px`
- 헤더 텍스트: uppercase 제거, slate 계열 색상 적용
- 체크박스 색상: sky blue 통일
- 셀 편집 포커스 링: sky blue

---

### 4. 관심사 분리

`curriculum_main.html` 한 파일에 혼재하던 HTML/CSS/JS를 3개 파일로 분리했습니다.

```text
static/
  css/curriculum/curriculum.css   ← AG Grid 테마 CSS
  js/curriculum/curriculum.js     ← 그리드 초기화 및 비즈니스 로직 전체

templates/instructor/
  curriculum_main.html            ← HTML 마크업만 남김
```

- Thymeleaf `th:href="@{...}"` / `th:src="@{...}"` 로 참조 (context path 자동 대응)
- JS에 Thymeleaf 인라인 표현식이 없었으므로 외부 파일로 완전 이동 가능
- `defer` 속성으로 DOM 준비 후 JS 실행 보장

---

### 5. 현재 상태

- 커리큘럼 생성/수정/삭제 기능 테스트 정상 확인
- Mapper XML, DTO, DB 컬럼 정합성 확보
- 컨트롤러 인증/감사 필드 처리 정상
- ORA-00001 근본 해결 완료
- AG Grid UI 디자인 정리
- HTML/CSS/JS 관심사 분리 완료
- 빌드 통과 (`mvn -pl admin -am -DskipTests compile` → BUILD SUCCESS)

---

### 6. 수정된 주요 파일 목록

- `admin/src/main/java/kr/or/ddit/instructor/InstructorCurriculumMapper.java`
- `admin/src/main/java/kr/or/ddit/instructor/InstructorCurriculumServiceImpl.java`
- `admin/src/main/resources/mapper/InstructorCurriculumMapper.xml`
- `admin/src/main/resources/templates/instructor/curriculum_main.html`
- `admin/src/main/resources/static/css/curriculum/curriculum.css` (신규)
- `admin/src/main/resources/static/js/curriculum/curriculum.js` (신규)
