# 📑 [가이드] 관리자(Admin) 레이아웃 및 권한별 사이드바 시스템 개발 가이드

본 프로젝트는 화면 개발의 생산성을 극대화하고 코드 중복 및 Git 충돌을 근본적으로 방지하기 위해 **`LayoutThymeleafViewResolver`를 활용한 커스텀 접두사(Prefix) 아키텍처**와 **역할 기반 조립식 사이드바 컴포넌트**를 사용합니다.

팀원들은 레이아웃 선언문(`layout:decorate`)이나 웹 페이지의 겉껍데기 태그(`<html>`, `<head>`, `<body>`) 및 사이드바 제어 코드를 적을 필요 없이, **오직 화면에 보여줄 순수 HTML 알맹이(Content)만 개발**하면 됩니다.

---

## 🏗️ 1. 아키텍처 및 화면 조립 메커니즘

1. **컨트롤러 요청**: 백엔드 컨트롤러에서 리턴 값으로 `admin:/[파일명]` 구조의 뷰네임을 반환합니다.
2. **리졸버 인터셉트**: 커스텀 클래스인 `LayoutThymeleafViewResolver`가 `admin:/` 접두사를 감지하여 제어권을 가져옵니다.
3. **파일명 및 변수 분리**: 접두사를 떼어낸 순수 파일명을 `${contentPage}`라는 변수에 담아 마스터 레이아웃인 `layouts/admin-layout`으로 토스합니다.
4. **최종 화면 조립**: 마스터 레이아웃 내부에서 `th:insert`를 통해 알맹이 HTML을 결합하고, 동시에 내부 내비게이션 영역에서는 로그인한 유저의 세션 권한(`session.userRole`)을 검증하여 해당하는 역할의 메뉴 파일만 서버에서 동적으로 조립(`th:replace`)하여 브라우저로 내보냅니다.

---

## 📂 2. 사이드바 모듈화 폴더 및 파일 구조

사이드바 코드가 수백 줄로 늘어나 가독성이 떨어지거나 Git 충돌이 발생하는 것을 막기 위해 껍데기 틀과 역할별 메뉴 파일을 철저하게 격리합니다.

```text
src/main/resources/templates/
  └── fragments/
        └── sidebars/
              ├── sidebar_admin.html  (사이드바 외형 틀: 너비, 배경색, 토글 프레임)
              └── menus/              (역할별로 쪼갠 순수 메뉴 리스트 파일)
                    ├── menu_common.html     (공통: 모든 관리자 무조건 노출)
                    ├── menu_principal.html  (원장 전용 권한)
                    ├── menu_manager.html    (실장 전용 권한)
                    ├── menu_staff.html      (행정 전용 권한)
                    ├── menu_teacher.html    (선생님 전용 권한)
                    └── menu_pd.html         (PD 전용 권한)

```

---

## 📝 3. 화면 개발 권장 사항 (팀원용 파일 작성법)

### 💡 기본 작성 스타일: "알맹이 위주로 가볍게 짜기"

- `<!DOCTYPE html>`, `<html>`, `<head>`, `<body>` 태그는 공통 레이아웃 파일(`admin-layout.html`)이 완벽하게 표준 구조로 제공하므로 **작성하지 않습니다.**
- 최상단은 전체 콘텐츠를 감싸는 하나의 `<div>` 태그로 시작하여 알맹이 내부 코드만 작성해 주세요.
- 사이드바나 헤더는 이미 레이아웃이 잡고 있으므로 본문 코드에 사이드바 관련 마크업을 넣지 마세요.

### 💻 권장 작성 예시 (`templates/students/list.html`)

```html
<div>
  <div class="flex justify-between items-center mb-6">
    <h1 class="text-2xl font-bold text-slate-800">학생 목록 관리</h1>
    <button class="bg-blue-600 text-white px-4 py-2 rounded-xl">
      학생 등록
    </button>
  </div>

  <table class="w-full bg-white rounded-2xl shadow-sm"></table>
</div>

<th:block th:fragment="pageCss">
  <link rel="stylesheet" th:href="@{/css/page/student-list.css}" />
</th:block>

<th:block th:fragment="pageJs">
  <script th:src="@{/js/page/student-list.js}"></script>
</th:block>
```

---

## ☕ 4. 백엔드 컨트롤러(Java Controller) 작성 규칙

1. 브라우저 탭에 표시될 페이지 제목은 HTML에서 하드코딩하지 않고, 컨트롤러에서 `model.addAttribute("pageTitle", "페이지 제목");`을 통해 주입합니다.
2. 리턴 값은 뷰 리졸버가 작동할 수 있도록 반드시 **`admin:/[템플릿파일명]`** 형태로 리턴합니다.

### 💻 컨트롤러 작성 예시

```java
@Controller
@RequestMapping("/admin/students")
public class StudentController {

    @GetMapping("/list")
    public String studentList(Model model) {
        // 1. 브라우저 타이틀 주입
        model.addAttribute("pageTitle", "학생 목록 조회 | HERMES");

        // 2. admin:/ 접두사를 사용하여 templates/students/list.html 호출
        return "admin:/students/list";
    }
}

```

---

## 🏗️ 5. 마스터 레이아웃 파일 (`templates/layouts/admin-layout.html`)

기존의 단순 수직 나열 방식에서 **사이드바와 메인 컨텐츠 영역이 좌우 플렉스(Flex) 구조로 유기적으로 결합**된 마스터 본체입니다. 정적 스타일 에셋(`admin-core.css`, `admin-layout.css`)과 스크립트가 기본 탑재되어 있습니다.

```html
<!doctype html>
<html xmlns:th="http://www.thymeleaf.org">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title th:text="${pageTitle} ?: 'HERMES Admin'"></title>

    <script src="https://unpkg.com/@tailwindcss/browser@4"></script>
    <link
      rel="stylesheet"
      href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css"
    />

    <link rel="stylesheet" th:href="@{/css/admin-core.css}" />
    <link rel="stylesheet" th:href="@{/css/admin-layout.css}" />

    <th:block th:replace="~{${contentPage} :: pageCss} ?: ~{}"></th:block>
  </head>
  <body class="h-screen flex flex-col overflow-hidden bg-[#F8FAFC]">
    <header th:replace="~{fragments/header :: headerFragment}"></header>

    <div class="flex-1 flex min-h-0 w-full relative">
      <div
        th:replace="~{fragments/sidebars/sidebar_admin :: sidebarAdminFragment}"
      ></div>

      <main class="flex-1 overflow-y-auto p-10 no-scrollbar">
        <th:block th:insert="~{${contentPage}}"></th:block>
      </main>
    </div>

    <script th:src="@{/js/admin-core.js}"></script>
    <script th:src="@{/js/admin-layout.js}"></script>

    <th:block th:replace="~{${contentPage} :: pageJs} ?: ~{}"></th:block>
  </body>
</html>
```

---

## 🛠️ 6. 마스터 사이드바 조립 파일 (`fragments/sidebars/sidebar_admin.html`)

사이드바 외형 틀 정보만 유지하며 백엔드 세션 권한 값에 따라 해당 역할의 파일만 렌더링 시점에 병합합니다. 권한이 없는 코드는 HTML 렌더링 단계에서 원천 제외되므로 높은 보안성을 제공합니다.

```html
<!doctype html>
<html xmlns:th="http://www.thymeleaf.org">
  <aside
    th:fragment="sidebarAdminFragment"
    id="adminSidebar"
    class="w-72 bg-[#121212] text-slate-400 flex flex-col shrink-0 z-20 transition-all duration-300 overflow-hidden relative border-r border-white/5"
  >
    <nav class="flex-1 overflow-y-auto py-8 no-scrollbar overflow-x-hidden">
      <div th:replace="~{fragments/sidebars/menus/menu_common}"></div>

      <div
        th:if="${session.userRole == 'PRINCIPAL'}"
        th:replace="~{fragments/sidebars/menus/menu_principal}"
      ></div>

      <div
        th:if="${session.userRole == 'MANAGER'}"
        th:replace="~{fragments/sidebars/menus/menu_manager}"
      ></div>

      <div
        th:if="${session.userRole == 'STAFF'}"
        th:replace="~{fragments/sidebars/menus/menu_staff}"
      ></div>

      <div
        th:if="${session.userRole == 'TEACHER'}"
        th:replace="~{fragments/sidebars/menus/menu_teacher}"
      ></div>

      <div
        th:if="${session.userRole == 'PD'}"
        th:replace="~{fragments/sidebars/menus/menu_pd}"
      ></div>
    </nav>
  </aside>
</html>
```

---

## 🎨 7. Tailwind CSS 컴파일 설정 (`tailwind.config.js`)

새로 추가된 `menus/` 하위 폴더 내부의 세부 메뉴 HTML 파일들과 팀원들이 작성하는 모든 알맹이 파일들에서도 유틸리티 클래스가 누수 없이 스캔 및 실시간 빌드되도록 설정되어 있습니다.

```javascript
/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    // 레이아웃, 프래그먼트, 세부 권한 메뉴 폴더까지 완벽하게 추적 스캔
    "./src/main/resources/templates/**/*.html",
    "./src/main/resources/templates/*.html",
    "./src/main/resources/static/js/**/*.js",
  ],
  theme: {
    extend: {},
  },
  plugins: [],
};
```

---

## 🎯 8. 모듈화 아키텍처 도입 결과 및 장점

1. **완벽한 Git 충돌 차단**: 역할별로 메뉴 파일(`menu_*.html`)이 물리적으로 완전 분리되어 있어, 여러 팀원이 동시에 본인 파트의 메뉴를 수정·커밋하더라도 소스코드가 꼬이거나 충돌이 발생하지 않습니다.
2. **원천적인 프론트엔드 보안**: 자바스크립트로 메뉴를 숨기거나 띄우는 방식이 아닌, 서버 백엔드 단에서 세션 검증 후 HTML 트리 자체를 도려내어 전송하므로 브라우저 개발자 도구(F12) 조작을 통한 권한 우회가 불가능합니다.
3. **정적 자원 변경 제로**: 레이아웃 컴포넌트가 파편화되어도 최종 렌더링된 DOM 계층은 이전과 완전히 동일하므로, 기존에 제작된 `admin-layout.css`와 `admin-layout.js` 코드를 단 한 줄도 수정하지 않고 그대로 사용 가능합니다.
