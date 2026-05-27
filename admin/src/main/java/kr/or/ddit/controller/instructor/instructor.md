# instructor 커리큘럼 & 강좌 관리 기능 작업 기록

## 최종 상태 요약

---

### 1. 프로젝트 개요

강사(instructor) 관련 기능은 **커리큘럼 관리**와 **나의 강좌** 두 메뉴로 구성됩니다.
현재 커리큘럼 CRUD 및 강좌 목록 조회 기능이 정상 동작 확인 완료 상태입니다.

---

### 2. 패키지 구조

뷰 레이어(템플릿)와 컨트롤러는 `admin` 모듈에, 나머지(DTO·Mapper·Service)는 `common` 모듈에서 관리합니다.

```text
[common 모듈]
src/main/java/kr/or/ddit/finalProject/
  dto/curriculum/
    CurriculumMasterDto.java
    CurriculumDetailDto.java
  dto/course/
    CourseDto.java
  mapper/curriculum/
    CurriculumMapper.java
  mapper/course/
    CourseMapper.java
  service/curriculum/
    CurriculumService.java
    CurriculumServiceImpl.java
  service/course/
    CourseService.java
    CourseServiceImpl.java

src/main/resources/mapper/
  curriculum/CurriculumMapper.xml
  course/CourseMapper.xml

[admin 모듈]
src/main/java/kr/or/ddit/controller/instructor/
  InstructorCurriculumController.java
  InstructorCourseController.java

src/main/resources/
  templates/instructor/
    curriculum.html
    course.html
  static/
    css/instructor/curriculum.css
    js/instructor/curriculum.js
```

MyBatis mapper scan 설정(`classpath*:mapper/**/*.xml`)이 하위 디렉토리를 자동으로 포함합니다.

---

### 3. 뷰 리졸버 동작 방식

`LayoutThymeleafViewResolver`가 뷰 이름을 처리합니다.

```java
return "admin:/instructor/curriculum";
// → admin-layout.html 렌더링
// → contentPage = "instructor/curriculum" 자동 주입
// → admin-layout.html 내부에서 th:insert="~{${contentPage}}" 로
//    templates/instructor/curriculum.html 삽입
```

**주의:** 뷰 리졸버가 `contentPage`를 직접 주입하므로 컨트롤러에서
`model.addAttribute("contentPage", ...)` 를 명시적으로 세팅할 필요가 없습니다.
작성해도 뷰 리졸버가 덮어씁니다.

---

### 4. 해결된 핵심 이슈

#### A. ORA-00001 근본 해결 (INSERT ALL → 단건 INSERT 루프)

Oracle에서 `INSERT ALL ... SELECT * FROM DUAL` 구문은 단일 SQL 문장이므로
`NEXTVAL`이 문장당 1회만 증가합니다. 결과적으로 모든 행이 동일한 `DETAIL_ID`를
받아 PK 중복(`ORA-00001`)이 발생했습니다.

**해결:** `insertDetailList`(INSERT ALL)를 폐기하고 단건 `insertDetail`로 교체 후
서비스 레이어에서 루프 처리합니다.

> 시퀀스 rollback 이후에도 `NEXTVAL` 소비는 취소되지 않아 번호에 gap이 생길 수
> 있으나, PK는 연속일 필요가 없으므로 문제되지 않습니다.

#### B. ORA-17004 (JdbcType OTHER) 해결

MyBatis가 `null` 값을 `JdbcType.OTHER(1111)`로 전송할 때 Oracle JDBC가 거부하는 문제입니다.

**해결:** `CourseMapper.xml`의 INSERT 구문에서 nullable 파라미터 전체에 `jdbcType` 명시:

```xml
#{curriculumId,  jdbcType=NUMERIC}
#{courseExplnCn, jdbcType=VARCHAR}
#{opnnYn,        jdbcType=CHAR}
```

#### C. ORA-00942 테이블명 오류

SQL에서 `CLASS` 테이블을 참조했으나 실제 테이블명은 `CLASSROOM`입니다.
`CourseMapper.xml`의 SELECT 쿼리에서 수정 완료했습니다.

#### D. Mapper XML/DTO/DB 컬럼 정합성

실제 DB 컬럼명 기준으로 매핑 정리 완료:

- 사용 컬럼: `RGTR_ID`, `REG_DT`, `LAST_MDFR_ID`, `MDFCN_DT`
- 제거: `REG_ID`, `MOD_ID`, `REG_DATE`, `MOD_DATE` 형태의 잘못된 참조

---

### 5. DB 테이블 변경 사항

#### COURSE 테이블

| 추가 컬럼 | 타입 | 설명 |
| --- | --- | --- |
| `CURRICULUM_ID` | NUMBER | 커리큘럼 마스터 FK |
| `COURSE_PRICE` | NUMBER | 강좌 수강료 |

#### CLASSROOM 테이블

| 변경 | 내용 |
| --- | --- |
| `CLASS_STAT_CD` 제거 | 운영 여부는 `USE_YN`('Y'/'N')으로 대체 |

---

### 6. 강좌(Course) 기능 설계

강사는 강좌를 직접 개설/수정할 수 없습니다. 행정팀이 강좌를 개설하고 강사에게
배정하는 구조입니다. 강사가 할 수 있는 것:

- **나의 강좌** 목록 조회 (자신에게 배정된 강좌 카드 뷰)
- 각 강좌에 연결된 **클래스룸 목록 조회** (개발 예정)
- 클래스룸 내 **수강생 목록 조회** (개발 예정)

강좌 수정 요청은 행정팀에 기안을 올리는 방식으로 처리할 예정입니다.

---

### 7. UI

#### 커리큘럼 관리 페이지 (`curriculum.html`)

- AG Grid(`ag-theme-alpine`)를 사용한 커리큘럼 상세 편집 그리드
- 폰트: `"Noto Sans KR"` (Alpine 기본값 override용 `!important` 병행 적용)
- 행 높이 42px, 헤더 높이 40px, sky blue 계열 테마
- HTML/CSS/JS 관심사 분리: `css/instructor/curriculum.css`, `js/instructor/curriculum.js`

#### 나의 강좌 페이지 (`course.html`)

- 카드 뷰 그리드 레이아웃
- 커리큘럼명 배지, 제작방식 코드 배지, 강좌명, 설명, 클래스룸 수, 총 학습시간 표시
- 클래스룸 입장 버튼 (클래스룸 기능 구현 전까지 비활성화)

---

### 8. 현재 정상 동작 확인 완료 항목

- 커리큘럼 생성 / 수정 / 논리 삭제(`USE_YN='N'`)
- 커리큘럼 상세 행 추가 / 삭제
- 나의 강좌 목록 조회 (카드 뷰)
- 빌드: `mvn clean install -pl common,admin -am -DskipTests` → BUILD SUCCESS
