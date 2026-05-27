# instructor 커리큘럼 & 강좌 관리 기능 작업 기록

## 최종 상태 요약

---

### 1. 프로젝트 개요

강사(instructor) 관련 기능은 **커리큘럼 관리**와 **나의 강좌** 두 메뉴로 구성됩니다.
현재 커리큘럼 CRUD 및 강좌 목록 조회 기능이 정상 동작 확인 완료 상태입니다.

---

### 2. 패키지 구조

```text
admin/src/main/java/kr/or/ddit/
  dto/instructor/
    CurriculumMasterDto.java
    CurriculumDetailDto.java
    CourseDto.java
  instructor/
    controller/
      InstructorCurriculumController.java
      InstructorCourseController.java
    service/
      InstructorCurriculumService.java
      InstructorCurriculumServiceImpl.java
      InstructorCourseService.java
      InstructorCourseServiceImpl.java
    mapper/
      InstructorCurriculumMapper.java
      InstructorCourseMapper.java

admin/src/main/resources/
  mapper/instructor/
    InstructorCurriculumMapper.xml
    InstructorCourseMapper.xml
  templates/instructor/
    curriculum_main.html
    course.html
  static/
    css/curriculum/curriculum.css
    js/curriculum/curriculum.js
```

MyBatis mapper scan 설정(`classpath*:mapper/**/*.xml`)이 `mapper/instructor/` 하위 디렉토리를 자동으로 포함합니다.

---

### 3. 해결된 핵심 이슈

#### A. ORA-00001 근본 해결 (INSERT ALL → 단건 INSERT 루프)

Oracle에서 `INSERT ALL ... SELECT * FROM DUAL` 구문은 단일 SQL 문장이므로 `NEXTVAL`이 문장당 1회만 증가합니다.
결과적으로 모든 행이 동일한 `DETAIL_ID`를 받아 PK 중복(`ORA-00001`)이 발생했습니다.

**해결:** `insertDetailList`(INSERT ALL)를 폐기하고 단건 `insertDetail`로 교체 후 서비스 레이어에서 루프 처리합니다.

> 시퀀스 rollback 이후에도 `NEXTVAL` 소비는 취소되지 않아 번호에 gap이 생길 수 있으나, PK는 연속일 필요가 없으므로 문제되지 않습니다.

#### B. ORA-17004 (JdbcType OTHER) 해결

MyBatis가 `null` 값을 `JdbcType.OTHER(1111)`로 전송할 때 Oracle JDBC가 거부하는 문제입니다.

**해결:** `InstructorCourseMapper.xml`의 INSERT 구문에서 nullable 파라미터 전체에 `jdbcType` 명시:

```xml
#{curriculumId, jdbcType=NUMERIC}
#{courseExplnCn, jdbcType=VARCHAR}
#{opnnYn,        jdbcType=CHAR}
```

#### C. ORA-00942 테이블명 오류

SQL에서 `CLASS` 테이블을 참조했으나 실제 테이블명은 `CLASSROOM`입니다.
`InstructorCourseMapper.xml`의 SELECT 쿼리에서 수정 완료했습니다.

#### D. Mapper XML/DTO/DB 컬럼 정합성

실제 DB 컬럼명 기준으로 매핑 정리 완료:

- 사용 컬럼: `RGTR_ID`, `REG_DT`, `LAST_MDFR_ID`, `MDFCN_DT`
- 제거: `REG_ID`, `MOD_ID`, `REG_DATE`, `MOD_DATE` 형태의 잘못된 참조

#### E. 컨트롤러 인증 처리

`SecurityContextHolder` 필드 초기화 방식 대신 요청 시점의 `Authentication`에서 로그인 ID를 받아오도록 수정했습니다.

---

### 4. DB 테이블 변경 사항

#### COURSE 테이블

| 추가 컬럼 | 타입 | 설명 |
| --- | --- | --- |
| `CURRICULUM_ID` | NUMBER | 커리큘럼 마스터 FK |
| `COURSE_PRICE` | NUMBER | 강좌 수강료 |

#### CLASSROOM 테이블 (CLASS → CLASSROOM으로 실제 테이블명 확인)

| 변경 | 내용 |
| --- | --- |
| `CLASS_STAT_CD` 제거 | 운영 여부는 `USE_YN`('Y'/'N')으로 대체 |

---

### 5. 강좌(Course) 기능 설계

강사는 강좌를 직접 개설/수정할 수 없습니다. 행정팀이 강좌를 개설하고 강사에게 배정하는 구조입니다.
강사가 할 수 있는 것:

- **나의 강좌** 목록 조회 (자신에게 배정된 강좌 카드 뷰)
- 각 강좌에 연결된 **클래스룸 목록 조회** (현재 개발 예정)
- 클래스룸 내 **수강생 목록 조회** (현재 개발 예정)

강좌 수정 요청은 행정팀에 기안을 올리는 방식으로 처리할 예정입니다.

---

### 6. UI

#### 커리큘럼 관리 페이지 (`curriculum_main.html`)

- AG Grid(`ag-theme-alpine`)를 사용한 커리큘럼 상세 편집 그리드
- 폰트: `"Noto Sans KR"` (Alpine 기본값 override용 `!important` 병행 적용)
- 행 높이 42px, 헤더 높이 40px, sky blue 계열 테마
- HTML/CSS/JS 관심사 분리: `curriculum.css`, `curriculum.js` 외부 파일로 분리

#### 나의 강좌 페이지 (`course.html`)

- 카드 뷰 그리드 레이아웃
- 커리큘럼명 배지, 제작방식 코드 배지, 강좌명, 설명, 클래스룸 수, 총 학습시간 표시
- 클래스룸 입장 버튼 (클래스룸 기능 구현 전까지 비활성화)

---

### 7. 현재 정상 동작 확인 완료 항목

- 커리큘럼 생성 / 수정 / 논리 삭제(`USE_YN='N'`)
- 커리큘럼 상세 행 추가 / 삭제
- 나의 강좌 목록 조회 (카드 뷰)
- 빌드: `mvn clean install -pl common,admin -am -DskipTests` → BUILD SUCCESS
