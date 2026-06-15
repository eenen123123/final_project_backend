# 클래스룸(Classroom) 기능 설계 가이드라인

> 작성 목적: 클래스룸 페이지에서 강사가 사용할 수 있는 기능의 범위, 역할 분리 원칙, 자동화 설계 방향을 정의하여 개발 시 참고 문서로 활용

---

## 1. 클래스룸의 위치와 역할

### 1.1 관리자 페이지와의 관계

클래스룸은 관리자 페이지와 **완전히 별개의 페이지**로 구성된다.
강사는 관리자 페이지의 `담당 클래스 관리` 메뉴에서 클래스룸 목록을 조회하고, 해당 클래스룸 페이지로 진입한다.

```
관리자 페이지
└── 담당 클래스 관리 ──진입──▶ 클래스룸 페이지 (별도 영역)
```

### 1.2 역할 정의

| 영역 | 역할 |
|---|---|
| 관리자 페이지 | 설계 / 집계 / 도구 (클래스 횡단 기능) |
| 클래스룸 페이지 | 실행 / 운영 / 상호작용 (특정 클래스 내부 기능) |

### 1.3 강사 권한의 핵심 원칙

> **강사는 수업과 학습 진척도에만 관여한다.**

- ✅ 강사 권한 범위: 수업 운영, 학습 진도, 성적, 과제, Q&A, 시험 출제
- ❌ 강사 권한 외: 학생 소속 변경, 개인 신상 정보, 학부모 연락처 직접 접근, 감독관 배정 등 행정 영역
- ❌ 강사 권한 외: 강의 영상 업로드 / 편집 등 PD 영역

---

## 2. 역할별 권한 분리표

| 기능 영역 | 강사 | PD | 행정팀 |
|---|:---:|:---:|:---:|
| 수강생 목록 조회 (읽기 전용) | ✅ | ❌ | ✅ |
| 반 이동 / 소속 변경 | ❌ | ❌ | ✅ |
| 학부모 알림 발송 (학습 관련) | ✅ (시스템 경유) | ❌ | ❌ |
| 학부모 알림 발송 (행정 관련) | ❌ | ❌ | ✅ |
| 감독관 배정 | ❌ | ❌ | ✅ |
| 면담 기록 — 학습 맥락 필드 | ✅ | ❌ | ❌ |
| 면담 기록 — 신상/가정환경 필드 | ❌ | ❌ | ✅ |
| 학습 진도율 조회 | ✅ | ❌ | ❌ |
| 성적 조회 / 입력 | ✅ | ❌ | ❌ |
| 과제 미제출 목록 조회 | ✅ | ❌ | ❌ |
| 과제 미제출 알림 (자동화 설정) | ✅ | ❌ | ❌ |
| Q&A 답변 | ✅ | ❌ | ❌ |
| 시험 출제 / 일정 등록 | ✅ | ❌ | ❌ |
| 강의 구조 설계 (커리큘럼) | ✅ | ❌ | ❌ |
| 강의 영상 업로드 / 편집 | ❌ | ✅ | ❌ |
| 영상 연결 상태 확인 | ✅ | ✅ | ❌ |
| AI 문항 생성 | ✅ | ❌ | ❌ |
| 업무 일지 | ✅ | ❌ | ❌ |

---

## 3. 클래스룸 기능 상세 정의

### 3.1 수강생 관리

**원칙**: 강사는 수업 운영 목적의 조회만 가능. 소속 변경 등 행정 처리는 불가.

#### 수강생 목록 조회 (읽기 전용)

- 해당 클래스 소속 학생 목록 조회
- 표시 정보: 이름, 학습 진도율 요약, 최근 접속일, 과제 제출 현황
- **수정 불가**: 소속, 연락처 등 개인정보 필드는 표시하지 않거나 읽기 전용으로만 제공

#### 개별 면담 기록 작성

강사가 학습 맥락 안에서 진행한 상담 내용을 기록한다.
행정팀의 상담 기록과는 **별도 테이블**로 분리하여 관리한다.

**강사가 입력 가능한 필드**
- 면담 일시
- 면담 유형 (수업 이해도 / 학습 목표 / 강좌 건의 / 기타)
- 면담 내용 (자유 텍스트)
- 강사 소견 및 후속 조치 메모
- 다음 면담 예정일

**강사에게 노출 / 입력 금지 필드**
- 가정환경, 가족 관계
- 건강 정보, 장애 여부
- 개인 연락처, 주소
- 행정 처리가 필요한 민원 내용

> **설계 메모**: 폼 구성 시 강사용 필드와 행정팀용 필드를 DB 레벨에서 테이블 분리하거나, 접근 권한 컬럼으로 구분하여 강사 계정에서는 행정 필드가 노출되지 않도록 처리한다.

---

### 3.2 학습 현황 모니터링

**원칙**: 특정 클래스 내 수강생 개별 데이터를 조회. 관리자 페이지의 `전체 학습 현황`은 집계 뷰이고, 클래스룸은 개별 상세 뷰.

#### 학생별 성적 상세 조회

- 시험 점수 누적 데이터 조회
- 성적 변화 추이 그래프 (회차별 점수 시각화)
- 과목별 / 시험 유형별 필터

#### 개인별 진도율 및 과제 확인

- 온라인 강의 수강 진도율 (강의별 시청 완료 여부)
- 과제 제출 여부 및 제출 시각
- 미제출 / 지각 제출 표시

#### 과제 미제출자 관리 및 알림

→ **4.1 알림 자동화 설계** 참고

---

### 3.3 면담 알림 / 학부모 알림

**원칙**: 강사는 알림의 내용과 발송 대상을 지정하는 것만 담당하며, 실제 연락처 조회와 발송은 시스템이 처리한다. 강사에게 학부모 연락처가 직접 노출되지 않는다.

```
강사                  시스템                    학부모
──────────────────────────────────────────────────────
알림 내용 작성   ──▶  연락처는 시스템이 보유  ──▶  알림 수신
발송 대상 지정        강사에게 연락처 비노출
발송 트리거 실행
```

**강사가 발송 가능한 알림 유형**
- 과제 미제출 안내
- 시험 일정 공지
- 성적 결과 안내
- 강좌 관련 공지
- 면담 결과 요약 안내

**행정팀만 발송 가능한 알림 유형**
- 납부 / 환불 안내
- 입퇴원 / 반 이동 통보
- 개인정보 변경 확인
- 행정 민원 처리 결과

---

### 3.4 질의응답(Q&A) 관리

**원칙**: 수업 관련 질의응답은 클래스룸 전속 기능. 관리자 페이지와 연관 없음.

#### Q&A 목록 조회

- 해당 클래스 수강생의 질문 목록 조회
- 필터: 답변 대기 / 답변 완료 / 전체
- 정렬: 최신순 / 미답변 우선

#### 질문 답변 처리

- 답변 내용 등록
- 처리 상태 변경 (답변 대기 → 답변 완료)
- 답변 수정 / 삭제

---

### 3.5 시험 운영

**원칙**: 시험지 설계와 문항 은행 관리는 관리자 페이지의 `문항/시험 관리`에서 수행. 클래스룸에서는 배정받은 시험을 운영하고 결과를 처리.

```
관리자 페이지                    클래스룸
───────────────────────────────────────────
문항 은행 관리    ──배정──▶  시험 운영 (일정, 응시)
시험지 설계                  성적 입력 및 저장
클래스 배정                  오늘의 문제 출제
```

#### 오늘의 문제 출제

- 일일 문제 설정 (문항 은행에서 선택 또는 직접 작성)
- 출제 스케줄러 등록 (날짜 / 시간 지정 자동 출제)
- 출제 이력 조회

#### 오프라인 시험 일정 등록

- 시험명, 일시, 장소, 대상 클래스 등록
- 시험 상태 관리 (예정 / 진행 중 / 종료)
- **감독관 배정은 행정팀 권한** → 강사는 배정 결과 확인만 가능

#### 성적 입력

- 오프라인 시험 결과 점수 수동 입력
- 학생별 점수 저장 및 수정
- 입력 완료 후 성적 확정 처리 (확정 후 수정 시 이력 기록)

---

## 4. 자동화 설계

### 4.1 과제 미제출 알림 자동화

완전 수동이나 완전 자동이 아닌, **자동화 기본 + 강사 설정 + 예외 수동** 3단계 레이어 구조로 설계한다.

```
레이어 1 — 시스템 자동화 (기본값)
  └── 마감일 D-1, 당일, D+1 자동 알림 발송
  └── 강사 개입 없이 시스템이 처리

레이어 2 — 강사 설정 (자동화 커스터마이징)
  └── 알림 발송 시점 조정 (D-2, D-3 등)
  └── 알림 문구 수정
  └── 특정 학생 알림 제외 설정
  └── 자동화 ON / OFF 설정

레이어 3 — 강사 수동 발송 (예외 처리용)
  └── 자동화로 커버되지 않는 특수 상황에만 사용
  └── 발송 시 연락처는 시스템 경유 (강사에게 비노출)
```

### 4.2 강의 영상 연결 흐름

강사가 커리큘럼에서 강의 슬롯을 설계하면, PD가 해당 슬롯에 영상을 업로드하고 연결하는 방식으로 운영한다.

```
강사 (관리자 페이지)              PD (관리자 페이지)
────────────────────────────────────────────────────
강의 슬롯 생성           ──▶  영상 업로드 요청 확인
제목 / 설명 / 순서 설정        영상 파일 업로드 및 인코딩
영상 연결 상태 확인      ◀──  영상 연결 완료 처리
                               썸네일 / 자막 등 메타데이터 관리
```

**강사 권한**
- 강의 슬롯 생성 및 구조 편집
- 영상 연결 상태 확인 (연결됨 / 미연결 / 처리 중)
- PD에게 영상 등록 요청 발송

**PD 권한**
- 영상 파일 업로드 / 교체 / 삭제
- 인코딩 설정 및 품질 관리
- 썸네일, 자막 등 메타데이터 편집
- 강의 슬롯에 영상 연결 확정

---

## 5. 관리자 페이지 ↔ 클래스룸 전체 흐름

```
관리자 페이지                              클래스룸
────────────────────────────────────────────────────────────────
담당 클래스 관리       ──진입──▶  수강생 목록 / 면담 기록 / 성적 입력
전체 학습 현황 (집계)  ◀─데이터─  성적 상세 / 진도율 / 과제 미제출
문항/시험 관리 (설계)  ──배정──▶  오늘의 문제 / 시험 일정 / 성적 입력
커리큘럼 관리 (설계)   ──배정──▶  강의 구조 표시 (편집은 관리자에서)
AI 문항 생성 (도구)    ──저장──▶  문항 은행 ──배정──▶ 시험 운영
업무 일지              (관리자 페이지 독립 기능)
개인 페이지 관리       (관리자 페이지 독립 기능)
```

---

## 6. 최종 클래스룸 기능 목록 (강사 한정)

| 영역 | 기능 | 비고 |
|---|---|---|
| 수강생 관리 | 수강생 목록 조회 | 읽기 전용, 개인정보 필드 비노출 |
| 수강생 관리 | 개별 면담 기록 작성 | 학습 맥락 필드만 허용 |
| 학습 현황 | 학생별 성적 상세 조회 | 추이 그래프 포함 |
| 학습 현황 | 개인별 진도율 및 과제 확인 | |
| 학습 현황 | 과제 미제출자 관리 및 알림 | 자동화 기본, 수동 예외 처리 |
| 알림 | 학부모 알림 발송 (학습 관련) | 연락처 시스템 경유, 강사 비노출 |
| Q&A | 학생 질문 목록 조회 | |
| Q&A | 질문 답변 등록 및 처리 상태 변경 | |
| 시험 운영 | 오늘의 문제 출제 및 스케줄 등록 | |
| 시험 운영 | 오프라인 시험 일정 등록 | 감독관 배정 제외 |
| 시험 운영 | 오프라인 시험 성적 입력 | 확정 후 수정 시 이력 기록 |

---

## 7. UI 공통 디자인 패턴

### 7.1 레이아웃 구조

모든 클래스룸 탭 페이지는 **`classroom-fragments.html`의 `layout` 프래그먼트**를 겉껍질로 사용한다.

```
classroom-fragments.html
└── layout(pageTitle, classroom, activeTab, pageContent)
    ├── <head>   — CDN (Tailwind, Font Awesome) + font
    ├── <header> — 상단바 (클래스 목록 뒤로가기 + 클래스명)
    ├── <aside>  — 좌측 패널
    │   ├── 강사 아바타 + 이름 + 강사 페이지 방문 버튼
    │   ├── Information: 강좌 / 수강 인원 / 수강 시작일
    │   └── Calendar (th:if="${calendarDays != null}") ← 홈 탭에서만 표시
    ├── <nav>    — 7개 탭 네비게이션
    └── <th:block th:replace="${pageContent}"/>  ← 각 탭이 주입하는 고유 컨텐츠
```

모든 탭 페이지(home 포함)가 동일한 `layout` 프래그먼트를 사용한다. 캘린더는 `calendarDays` 모델 속성 유무로 자동 표시/비표시되므로 페이지별 분기가 필요 없다.

### 7.2 새 탭 페이지 작성법

```html
<!DOCTYPE html>
<html th:replace="~{instructor/classroom-fragments :: layout(
    ${classroom.classNm} + ' | {탭명}',
    ${classroom},
    '{activeTab}',
    ~{:: #content}
)}" lang="ko" xmlns:th="http://www.thymeleaf.org">
<head></head>
<body>
<div id="content">

  <!-- 이 탭 고유 컨텐츠만 작성 -->
  <div class="bg-white rounded-2xl border border-slate-200/80 shadow-sm overflow-hidden">
    ...
  </div>

</div>
</body>
</html>
```

`activeTab` 허용 값: `home` | `notice` | `lectures` | `assignments` | `qna` | `grades` | `members`

### 7.3 공통 컴포넌트 클래스

| 컴포넌트 | 클래스 |
|---|---|
| **카드 컨테이너** | `bg-white rounded-2xl border border-slate-200/80 shadow-sm overflow-hidden` |
| **카드 헤더** | `px-6 py-4 border-b border-slate-100` |
| **카드 푸터** | `border-t border-slate-100 px-6 py-3 bg-slate-50/50` |
| **테이블 thead** | `bg-slate-50 border-b border-slate-100 text-xs font-bold text-slate-500 tracking-wider uppercase` |
| **테이블 tbody** | `divide-y divide-slate-50 text-sm text-slate-700` |
| **테이블 행** | `hover:bg-slate-50/50 transition-colors` |
| **테이블 셀** | `py-3 px-6` |
| **빈 목록** | `p-16 flex flex-col items-center justify-center text-center gap-3` |
| **빈 목록 아이콘** | `w-14 h-14 rounded-full bg-slate-100 flex items-center justify-center text-slate-400 text-2xl` |
| **주요 버튼 (파랑)** | `text-xs font-bold px-3 py-1.5 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors` |
| **보조 버튼 (취소)** | `text-xs font-bold px-4 py-2 text-slate-500 border border-slate-200 rounded-lg hover:text-slate-700 transition-colors` |
| **삭제 버튼** | `text-[11px] font-bold text-rose-400 hover:text-rose-600 transition-colors` |
| **텍스트 인풋** | `w-full text-sm px-4 py-2.5 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500/30 focus:border-blue-400` |
| **상태 배지 — 수강중** | `inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold bg-blue-50 text-blue-600` |
| **상태 배지 — 완료** | `inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold bg-emerald-50 text-emerald-600` |
| **상태 배지 — 경고** | `inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold bg-amber-50 text-amber-500` |
| **상태 배지 — 비활성** | `inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold bg-slate-100 text-slate-400` |

### 7.4 탭 목록 (7개, 고정)

| 탭 | activeTab 값 | URL |
|---|---|---|
| 홈 | `home` | `/instructor/classroom/detail/{classSn}` |
| 공지사항 | `notice` | `/instructor/classroom/detail/{classSn}/notice` |
| 온라인 강의 | `lectures` | `/instructor/classroom/detail/{classSn}/lectures` |
| 과제 제출 | `assignments` | `/instructor/classroom/detail/{classSn}/assignments` |
| Q&A | `qna` | `/instructor/classroom/detail/{classSn}/qna` |
| 성적 관리 | `grades` | `/instructor/classroom/detail/{classSn}/grades` |
| 수강생 목록 | `members` | `/instructor/classroom/detail/{classSn}/members` |

상세 페이지(notice-detail, qna-detail, assignment-detail)는 부모 탭의 `activeTab` 값을 그대로 사용.

### 7.5 공통 모델 주입 (@ModelAttribute)

캘린더처럼 **모든 탭에서 공통으로 필요한 모델 속성**은 `InstructorClassroomController`의 `@ModelAttribute` 메서드에서 주입한다.
Spring MVC가 컨트롤러의 모든 핸들러 실행 전에 이 메서드를 자동 호출하므로 각 핸들러에서 중복 코드를 작성하지 않아도 된다.

```java
@ModelAttribute
public void addCalendarAttributes(
        @PathVariable(required = false) Long classSn,
        Model model) {
    if (classSn == null) return;   // /list 엔드포인트 skip
    LocalDate now = LocalDate.now();
    int year  = now.getYear();
    int month = now.getMonthValue();
    model.addAttribute("calendarYear",    year);
    model.addAttribute("calendarMonth",   month);
    model.addAttribute("calendarPadding", classroomHomeService.retrieveCalendarPadding(year, month));
    model.addAttribute("calendarDays",    classroomHomeService.retrieveCalendarDays(classSn, year, month));
}
```

새 탭 핸들러를 추가할 때 캘린더 데이터를 따로 주입할 필요가 없다. 추후 aside에 공통 데이터를 추가할 경우에도 이 메서드에만 작성한다.

---

## 8. 변경 이력

| 기능 | 최초 판단 | 최종 판단 | 사유 |
|---|---|---|---|
| 반 이동 처리 | ❌ 제거 | ❌ 유지 제거 | 소속 변경 = 행정팀 권한 |
| 개별 면담 기록 작성 | ❌ 제거 | ✅ 복구 | 학습 맥락 필드로 범위 제한 후 허용 |
| 학부모 알림 발송 | ❌ 제거 | ✅ 조건부 복구 | 학습 관련 알림만, 연락처 시스템 경유 |
| 과제 미제출 알림 | ⚠️ 부분 제한 | ✅ 복구 + 자동화 | 자동화 기본, 강사 설정 + 예외 수동 |
| 감독관 배정 | ⚠️ 부분 제한 | ❌ 행정팀 분리 | 인사/행정 영역, 강사는 결과 확인만 |
