# Admin 공통 레이아웃 가이드

이 문서는 `admin` 프로젝트에서 공통 레이아웃을 어떻게 사용하고 확장하는지, UI 개발 시 어떤 점을 주의해야 하는지 설명합니다.

---

## 1. 공통 레이아웃 구조

### 핵심 파일

- `src/main/resources/templates/layouts/admin-layout.html`
    - admin 페이지의 기본 레이아웃 템플릿
    - 공통 CSS와 JS를 로드하고, 헤더·사이드바·메인 콘텐츠를 배치합니다
- `src/main/resources/templates/fragments/header.html`
    - 상단 헤더 프래그먼트
    - 사이드바 토글 버튼, 검색창, 알림, 계정 UI 등이 포함됩니다
- `src/main/resources/templates/fragments/sidebars/sidebar_admin.html`
    - 사이드바 전체 구조
    - 공통 메뉴와 역할별 메뉴를 분리하여 `th:replace`로 삽입합니다
- `src/main/resources/static/css/admin-core.css`
    - 글로벌 공통 스타일
    - 버튼, 입력, 공통 유틸리티 등 전역 UI 요소 정의
- `src/main/resources/static/css/sidebar-admin.css`
    - 사이드바 전용 스타일
    - `.sidebar-section`, `.sidebar-link`, `.sidebar-title`, `.sidebar-collapsed` 등
- `src/main/resources/static/js/admin-core.js`
    - 글로벌 공통 스크립트
    - 토스트 알림 같은 전역 기능
- `src/main/resources/static/js/sidebar-admin.js`
    - 사이드바 토글 및 상태 저장 전용 스크립트

---

## 2. 페이지 개발 흐름

### 2-1. 기본 레이아웃에 페이지 삽입

1. 새 admin 페이지 템플릿을 만듭니다.
2. 컨트롤러에서 `contentPage`에 해당 템플릿 경로를 설정합니다.
3. `admin-layout.html`이 로드되면, `<main>` 내부에서 `th:insert="~{${contentPage}}"`가 실제 페이지를 렌더합니다.

### 2-2. 헤더와 사이드바는 공통 처리

- `admin-layout.html`은 항상 `header`와 `sidebar`를 공통으로 삽입합니다.
- 페이지 템플릿에서는 헤더/사이드바를 직접 다시 작성할 필요가 없습니다.

### 2-3. 사용자 역할별 메뉴 관리

- `sidebar_admin.html`은 사용자 역할에 따라 메뉴를 선택해서 로드합니다.
- 역할별 메뉴 파일:
    - `menu_common.html`
    - `menu_principal.html`
    - `menu_manager.html`
    - `menu_staff.html`
    - `menu_pd.html`
    - `menu_instructor.html`
- 역할별 메뉴는 `th:if`와 `th:replace`로 동적으로 렌더됩니다.

---

## 3. UI 개발 시 꼭 알아야 할 점

### 3-1. 클래스 이름은 공통 CSS로 유지

- 사이드바 관련 스타일은 `sidebar-admin.css`에서 관리합니다.
- 새로운 sidebar 스타일은 가능한 한 공통 클래스(`sidebar-link`, `sidebar-icon`, `sidebar-title` 등)로 추가하세요.
- 메뉴 템플릿에서는 반복 Tailwind 클래스 대신 공통 클래스를 사용합니다.

### 3-2. 사이드바 축소 동작

- 사이드바 토글 버튼 ID: `sidebarToggle`
- 사이드바 컨테이너 ID: `adminSidebar`
- 토글 동작은 `sidebar-admin.js`에서 관리합니다.
- 로컬스토리지 키: `hermesSidebarState`
- 축소 상태 클래스: `sidebar-collapsed`
- 축소 시 숨겨지는 요소는 `.sidebar-title`, `.sidebar-link-text` 등으로 제어합니다.

### 3-3. 새 페이지를 추가할 때

1. `templates`에 새 뷰 템플릿 생성
2. 공통 레이아웃을 사용하는 컨트롤러 경로 설정
3. 페이지에서 별도의 헤더/사이드바 작성 금지
4. 필요한 UI 스타일은 `admin-core.css` 또는 `sidebar-admin.css`에 추가

### 3-4. UI 코드 수정 시 주의

- `admin-layout.html`은 공통 레이아웃의 진짜 진입점입니다.
- 여기서 CSS / JS 경로를 잘못 수정하면 전체 admin UI가 깨집니다.
- `sidebar-admin.css`는 사이드바 전용, `admin-core.css`는 전역 공통으로 구분해서 관리합니다.
- `sidebar-admin.js`는 사이드바 토글 및 상태 저장만 담당하도록 유지하세요.
- 공통 프래그먼트(`header.html`, `sidebar_admin.html`, `footer.html`)는 `<html>` 또는 `<!doctype html>`을 포함하지 않아야 합니다.

---

## 3-5. 개선 권장 사항

- 프래그먼트는 전체 페이지 템플릿이 아니라 공통 부분만 포함해야 합니다.
- 사이드바 상태 저장 키는 `hermesSidebarState`처럼 네임스페이스를 포함하는 것이 충돌 방지에 안전합니다.
- `sidebar-admin.css`의 축소 스타일은 `#adminSidebar.sidebar-collapsed`와 같이 구체적인 선택자를 사용해 `!important` 의존성을 줄입니다.
- 가능한 경우 `admin-core.css`와 `sidebar-admin.css`의 역할을 명확히 분리하여 유지보수성을 높입니다.
- 라이브러리 또는 Tailwind 브라우저 런타임을 사용할 때는 빌드된 정적 CSS로 전환하는 것을 고려하세요.

---

## 4. 실제 예시

### admin-layout.html 주요 구조

```html
<link rel="stylesheet" th:href="@{/css/admin-core.css}" />
<link rel="stylesheet" th:href="@{/css/sidebar-admin.css}" />

<header th:replace="~{fragments/header :: headerFragment}"></header>

<div class="flex-1 flex min-h-0 w-full relative">
    <div
        th:replace="~{fragments/sidebars/sidebar_admin :: sidebarAdminFragment}"
    ></div>

    <main class="flex-1 overflow-y-auto p-8 no-scrollbar bg-[#F8FAFC]">
        <th:block th:insert="~{${contentPage}}"></th:block>
    </main>
</div>

<script th:src="@{/js/admin-core.js}" defer></script>
<script th:src="@{/js/sidebar-admin.js}" defer></script>
```

### header.html 내부 토글 버튼

```html
<button id="sidebarToggle" class="...">
    <i class="fa-solid fa-bars text-lg"></i>
</button>
```

### sidebar-admin.js 동작 요약

- 페이지 로드 시 `hermesSidebarState`가 `collapsed`이면 축소 상태 적용
- 버튼 클릭 시 `sidebar-collapsed`를 토글하고 로컬스토리지에 상태 저장
- 사이드바 토글 상태 저장 키는 네임스페이스를 포함한 `hermesSidebarState`입니다.
- `sidebar-admin.js`는 사이드바 토글과 관련된 기능만 담당합니다.

---

## 5. 협업 중 팀원용 팁

- UI를 잘 모르는 팀원은 `templates/layouts/admin-layout.html`부터 읽으세요.
- 사이드바 관련 수정은 `sidebar-admin.css` / `sidebar-admin.js`를 먼저 확인하세요.
- 새로운 admin 메뉴는 반드시 역할별 메뉴 파일과 `sidebar_admin.html`의 구조를 따르세요.
- 공통 스타일은 `admin-core.css`에 추가하고, sidebar 전용은 `sidebar-admin.css`에 추가합니다.

---

## 6. 변경 시 체크리스트

- [ ] `admin-layout.html`에 CSS/JS 경로가 올바른가?
- [ ] 새로운 페이지에서 헤더 또는 사이드바를 직접 작성하지 않았는가?
- [ ] 새로운 sidebar 스타일이 `sidebar-admin.css`에 추가되었는가?
- [ ] 사이드바 토글 동작이 `sidebar-admin.js`에 남아 있는가?
- [ ] 페이지 역할별 메뉴 로드가 `sidebar_admin.html`에서 정상 동작하는가?

---

## 7. 파일 위치 요약

- 템플릿: `src/main/resources/templates/layouts/admin-layout.html`
- 헤더: `src/main/resources/templates/fragments/header.html`
- 사이드바: `src/main/resources/templates/fragments/sidebars/sidebar_admin.html`
- 메뉴: `src/main/resources/templates/fragments/sidebars/menus/*.html`
- 공통 CSS: `src/main/resources/static/css/admin-core.css`
- 사이드바 CSS: `src/main/resources/static/css/sidebar-admin.css`
- 공통 JS: `src/main/resources/static/js/admin-core.js`
- 사이드바 JS: `src/main/resources/static/js/sidebar-admin.js`
