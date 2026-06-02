# 강사(Instructor) 관리자 메뉴 설계 가이드라인

> 작성 목적: 관리자 페이지 내 강사 전용 메뉴의 구조, 역할 분리 원칙, 각 메뉴별 기능 범위를 정의하여 개발 시 참고 문서로 활용

---

## 1. 전체 구조 개요

### 1.1 도메인 계층

```
커리큘럼 (Curriculum)
└── 강좌 (Course)  ×N
    └── 강의 (Lesson)  ×N  ← 1회 수업 단위
```

### 1.2 페이지 영역 분리 원칙

| 영역 | 역할 | 특징 |
|---|---|---|
| **관리자 페이지** | 설계 / 집계 / 도구 | 특정 클래스에 종속되지 않음, 여러 클래스를 횡단하는 기능 |
| **클래스룸 페이지** | 실행 / 운영 / 상호작용 | 특정 클래스 내부 기능, 수강생 경험 중심 |

> **핵심 원칙**: 관리자 페이지의 강사 메뉴는 반드시 클래스 횡단(Cross-class) 기능이거나, 클래스 배정 이전 단계(설계/준비)의 기능이어야 한다.

---

## 2. 강사 전용 메뉴 목록

```
선생님 전용 권한
│
├── 담당 클래스 관리          /instructor/classroom/list
├── 커리큘럼 관리             /instructor/curriculum
├── 강좌 자료 관리            /instructor/course/materials
├── 전체 학습 현황            /instructor/learning/overview
├── 문항 / 시험 관리          /instructor/exams
├── AI 문항 생성 지원         /instructor/ai-questions
├── 강사 게시판               /instructor/board
├── 업무 일지                 /instructor/journals
└── 개인 페이지 관리          /instructor/profile/teacher
```

> **설계 원칙**: 관리자 페이지는 "수강생이 소비하는 공개 페이지"가 아닌 "강사가 편집·운영하는 도구"다.
> 공개 페이지(프론트)에서는 프로필·게시판·강좌가 한 페이지에 묶여 보이지만,
> 관리자 페이지에서는 기능별로 독립된 메뉴로 분리하여 일관성을 유지한다.

---

## 3. 메뉴별 상세 정의

### 3.1 담당 클래스 관리 `/instructor/classroom/list`

**목적**: 로그인한 강사가 담당하는 클래스룸 목록을 조회하고, 각 클래스룸 페이지로 진입하는 허브 역할

**관리자 페이지에서 제공하는 기능**
- 담당 클래스룸 카드/목록 조회 (클래스명, 수강생 수, 진행 상태 등 요약 정보)
- 클래스룸 페이지로 이동하는 진입 링크
- 클래스 상태 표시 (운영 중 / 종료 / 예정)

**클래스룸 페이지로 위임하는 기능** *(관리자 페이지에서 구현 금지)*
- 수강생 개별 출석 관리
- 강의 실시간 진행
- 클래스 내부 공지사항 작성
- 수강생과의 메시지/질의응답

**설계 메모**
- 강사는 복수의 클래스룸을 담당할 수 있으므로, 이 페이지는 목록 조회와 진입점 역할만 수행
- 특정 클래스에 종속된 기능은 이 페이지에서 절대 구현하지 않음

---

### 3.2 커리큘럼 관리 `/instructor/curriculum`

**목적**: 클래스에 배정되기 전 단계의 커리큘럼 및 강좌 구조를 설계하고 편집

**제공 기능**
- 커리큘럼 생성 / 수정 / 삭제
- 커리큘럼 내 강좌 구성 및 순서 편집
- 강좌 내 강의(Lesson) 목록 설계 (제목, 설명, 소요시간, 순서)
- 강의 공개 여부 / 잠금 조건 설정 (예: 선수 강의 이수 필요)
- 강좌 복제 (기수별 운영을 위한 버전 관리)
- 선수 강좌 조건 설정

**클래스룸과의 경계**
- 여기서는 콘텐츠 구조만 설계
- 실제 클래스에 커리큘럼을 배정하는 것은 클래스 설정에서 처리

---

### 3.3 강좌 자료 관리 `/instructor/course/materials`

**목적**: 강좌별 원본 자료(첨부파일, 링크, 공지 템플릿 등)를 중앙에서 관리

**제공 기능**
- 강좌별 첨부파일 업로드 / 수정 / 삭제
- 외부 링크 등록 및 관리
- 공지사항 템플릿 작성 (클래스룸에서 실제 발송은 클래스룸에서)
- 자료 공개 범위 설정 (전체 공개 / 특정 클래스에만)
- 자료 버전 이력 관리

**클래스룸과의 경계**
- 관리자 페이지: 자료 원본 관리 (업로드, 편집, 버전)
- 클래스룸: 해당 자료를 수강생에게 배포 및 공유

---

### 3.4 전체 학습 현황 `/instructor/learning/overview`

**목적**: 강사가 담당하는 **모든 클래스를 한눈에 집계**하여 파악하는 대시보드

**제공 기능**
- 담당 전체 클래스의 평균 진도율 / 이수율 집계
- 클래스별 진도율 비교 테이블
- 전체 클래스 기준 미진도자 현황 요약
- 이수증 발급 대상자 집계
- 기간별 학습 통계 필터 (주간 / 월간 / 기수별)

**클래스룸과의 경계**
- 관리자 페이지: 여러 클래스를 한눈에 보는 **집계/요약 뷰**
- 클래스룸: 특정 클래스 내 수강생 개별 진도 상세 조회 및 수동 보정

---

### 3.5 강사 게시판 `/instructor/board`

**목적**: 강사가 수강생을 대상으로 작성하는 공지사항·Q&A·자료실을 관리자 단에서 통합 관리

**제공 기능**

- 게시판 유형별 게시글 조회 (공지사항 / Q&A / 자료실)
- 게시글 작성 / 수정 / 삭제 (소프트 삭제, USE_YN)
- 삭제된 게시글 복구

**프론트와의 관계**

- 관리자 페이지: 강사가 게시글을 직접 작성·관리하는 도구
- 공개 페이지(프론트): 수강생이 강사 개인 페이지에서 게시판 링크를 통해 조회
- 공개 페이지에서는 강사 개인 페이지 하위에 게시판이 노출되지만, 관리는 이 메뉴에서 독립적으로 수행

**설계 메모**

- 기존 `/instructor/board/*` 컨트롤러·서비스·매퍼 구현 완료 상태로 재활용
- 게시판 유형은 `BOARD_TYPE_CD` (COM_CD 공통코드) 로 구분
- 프로필 관리(`/instructor/profile/teacher`)와 분리: 공개 페이지에서 함께 노출되더라도 관리 도구는 기능별로 독립

---

### 3.6 문항 / 시험 관리 `/instructor/exams`

**목적**: 문항 은행을 중앙에서 관리하고, 시험지를 설계하여 클래스에 배정할 수 있도록 준비

**제공 기능**
- 문항 은행(Question Bank) 관리: 문항 생성 / 수정 / 태그 / 분류
- 시험지 설계: 문항 선택, 배점, 시간 제한, 출제 방식(랜덤/고정) 설정
- 시험지 클래스 배정 (어느 클래스에서 사용할지 연결)
- 과제 설계: 마감일, 제출 형식, 채점 기준 설정
- 시험/과제 목록 조회 및 상태 관리 (미배정 / 배정 완료 / 진행 중 / 종료)

**클래스룸과의 경계**
- 관리자 페이지: 문항 은행 관리 + 시험지 설계 + 클래스 배정
- 클래스룸: 수강생 응시 관리, 개별 성적 조회, 성적 보정

---

### 3.7 AI 문항 생성 지원 `/instructor/ai-questions`

**목적**: AI를 활용해 문항 초안을 빠르게 생성하고, 문항 은행으로 저장하는 도구

**제공 기능**
- 강좌 내용 또는 텍스트 입력 기반 문항 자동 생성
- 문항 유형 선택 (객관식 / 주관식 / OX / 단답형)
- 생성된 문항 검토 및 편집
- 문항 은행으로 저장 (3.5 문항/시험 관리와 연동)
- 생성 이력 조회

**설계 메모**
- 이 메뉴는 독립 도구로, 특정 클래스에 종속되지 않음
- 생성 결과는 문항 은행에 저장 후 시험지에 활용하는 흐름으로 설계

---

### 3.8 업무 일지 `/instructor/journals`

**목적**: 강사 개인의 업무 기록 작성 및 관리

**제공 기능**
- 일지 작성 / 수정 / 삭제
- 날짜별 / 클래스별 태그 분류
- 일지 목록 조회 및 검색
- 관리자(운영자)와의 공유 여부 설정

**설계 메모**
- 특정 클래스에 종속되지 않는 강사 개인 기록
- 필요시 클래스 태그를 붙여 분류는 가능하지만, 클래스룸 기능과는 분리

---

### 3.9 개인 페이지 관리 `/instructor/profile/teacher`

**목적**: 수강생에게 노출되는 강사의 공개 프로필 페이지 편집

**제공 기능**

- 프로필 이미지 업로드 (Cloudinary 연동, INSTRUCTOR 테이블 별도 컬럼 저장)
- 강사 소개글 작성 및 수정 (`INSTRUCTOR.INSTR_INTRO`)
- 약력 / 저서 / 수상 / 방송출연 항목 관리 (`INSTRUCTOR_CAREER` 테이블)
  - 항목별 유형 구분: 01 약력 / 02 저서 / 03 수상 / 04 방송출연
  - 항목별 연도(시작/종료) + 내용 입력, 순서 조정, 개별 삭제

#### 설계 메모

- 게시판 관리(`/instructor/board`)와 분리: 공개 페이지에서 함께 보이더라도 관리 도구는 기능별로 독립
- `MEMBER.USER_PROFILE`은 계정 프로필 사진 전용이므로 강사 공개 프로필 이미지는 INSTRUCTOR 테이블에 별도 저장
- 공개 여부 설정 불필요: 강사 공개 페이지는 항상 공개

---

## 4. 기능 분리 요약표

| 기능 | 관리자 페이지 | 클래스룸 |
|---|:---:|:---:|
| 커리큘럼 / 강좌 / 강의 구조 설계 | ✅ | ❌ |
| 강좌 자료 원본 관리 | ✅ | ❌ |
| 문항 은행 / 시험지 설계 | ✅ | ❌ |
| AI 문항 생성 | ✅ | ❌ |
| 전체 클래스 집계 대시보드 | ✅ | ❌ |
| 클래스룸 진입점 (목록) | ✅ | ❌ |
| 수강생 개별 진도 상세 / 보정 | ❌ | ✅ |
| 개별 성적 조회 / 보정 | ❌ | ✅ |
| 수강생 출석 관리 | ❌ | ✅ |
| 클래스 내 공지 발송 | ❌ | ✅ |
| 수강생 질의응답 / 메시지 | ❌ | ✅ |
| 시험 배정 및 응시 관리 | ❌ | ✅ |

---

## 5. 메뉴 HTML 구조 (참고)

```html
<div class="sidebar-section">
  <p class="sidebar-title text-sky-500">선생님 전용 권한</p>
  <ul class="sidebar-list">
    <li>
      <a href="/instructor/classroom/list" class="sidebar-link">
        <i class="fa-solid fa-chalkboard sidebar-icon"></i>
        <span class="sidebar-link-text">담당 클래스 관리</span>
      </a>
    </li>
    <li>
      <a href="/instructor/curriculum" class="sidebar-link">
        <i class="fa-solid fa-book-open-reader sidebar-icon"></i>
        <span class="sidebar-link-text">커리큘럼 관리</span>
      </a>
    </li>
    <li>
      <a href="/instructor/course/materials" class="sidebar-link">
        <i class="fa-solid fa-folder-open sidebar-icon"></i>
        <span class="sidebar-link-text">강좌 자료 관리</span>
      </a>
    </li>
    <li>
      <a href="/instructor/learning/overview" class="sidebar-link">
        <i class="fa-solid fa-chart-line sidebar-icon"></i>
        <span class="sidebar-link-text">전체 학습 현황</span>
      </a>
    </li>
    <li>
      <a href="/instructor/exams" class="sidebar-link">
        <i class="fa-solid fa-marker sidebar-icon"></i>
        <span class="sidebar-link-text">문항 / 시험 관리</span>
      </a>
    </li>
    <li>
      <a href="/instructor/ai-questions" class="sidebar-link group">
        <i class="fa-solid fa-wand-magic-sparkles sidebar-icon group-hover:text-sky-400"></i>
        <span class="sidebar-link-text">AI 문항 생성 지원</span>
      </a>
    </li>
    <li>
      <a href="/instructor/board" class="sidebar-link">
        <i class="fa-solid fa-clipboard sidebar-icon"></i>
        <span class="sidebar-link-text">강사 게시판</span>
      </a>
    </li>
    <li>
      <a href="/instructor/journals" class="sidebar-link">
        <i class="fa-solid fa-book sidebar-icon"></i>
        <span class="sidebar-link-text">업무 일지</span>
      </a>
    </li>
    <li>
      <a href="/instructor/profile/teacher" class="sidebar-link">
        <i class="fa-solid fa-address-card sidebar-icon"></i>
        <span class="sidebar-link-text">개인 페이지 관리</span>
      </a>
    </li>
  </ul>
</div>
```

---

## 6. 변경 이력

| 메뉴 | 변경 내용 |
|---|---|
| 담당 클래스 관리 | 유지. 클래스룸 진입점 역할 명확화 |
| 커리큘럼 관리 | 유지 |
| 강좌 자료 관리 | **신규 추가** (기존 "나의 강좌" 재정의) |
| 전체 학습 현황 | **개편** (기존 "학습 현황 모니터링" → 클래스 횡단 집계 뷰로 범위 재정의) |
| 문항 / 시험 관리 | **개편** (기존 "시험 및 성적 관리" → 성적 조회는 클래스룸으로 분리) |
| AI 문항 생성 지원 | 유지. 문항 은행 저장 흐름 추가 |
| 강사 게시판 | **신규 추가** (기존 테스트 메뉴 → 정식 메뉴로 격상, `/instructor/board/*` 재활용) |
| 업무 일지 | 유지 |
| 개인 페이지 관리 | **범위 확장** (프로필 이미지 + 소개글 + INSTRUCTOR_CAREER 약력 관리 추가) |
| ~~나의 강좌~~ | **삭제** (강좌 자료 관리로 재정의) |
| ~~강사 게시판 테스트~~ | **삭제** (테스트 메뉴) |
