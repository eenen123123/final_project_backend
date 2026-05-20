# 📑 [가이드] 관리자(Admin) 레이아웃 시스템 및 화면 개발 가이드

본 프로젝트는 화면 개발의 생산성을 높이고 마크업 중복을 근본적으로 제거하기 위해 **`LayoutThymeleafViewResolver`를 활용한 커스텀 접두사(Prefix) 기반 아키텍처**를 사용합니다.

스프링 프레임워크의 `redirect:/` 문법처럼 컨트롤러에서 **`admin:/` 접두사**를 던지면, 자바 단에서 이를 가로채 공통 레이아웃을 입히고 알맹이 페이지를 동적으로 조립합니다.

팀원들은 불필요한 레이아웃 선언문(`layout:decorate`)이나 웹 페이지의 겉껍데기 태그(`<html>`, `<head>`, `<body>`)를 적을 필요 없이, **오직 화면에 보여줄 순수 HTML 알맹이(Content)만 개발**하면 됩니다.

---

## 🏗️ 1. 아키텍처 및 화면 조립 메커니즘

1. **컨트롤러 요청**: 백엔드 컨트롤러에서 리턴 값으로 `admin:/[파일명]` 구조의 뷰네임을 반환합니다.
2. **리졸버 인터셉트**: 커스텀 클래스인 `LayoutThymeleafViewResolver`가 `admin:/` 접두사를 감지하여 제어권을 가져옵니다.
3. **파일명 및 변수 분리**: 접두사를 떼어낸 순수 파일명을 `${contentPage}`라는 변수에 담아 마스터 레이아웃인 `layouts/admin-layout`으로 토스합니다.
4. **최종 화면 조립**: 마스터 레이아웃 내부에서 `th:insert="~{${contentPage}}"` 문법을 통해 `views/` 하위(또는 지정 폴더)의 알맹이 HTML 내용을 껍데기 중복 없이 깨끗하게 병합하여 브라우저로 내보냅니다.

---

## 📝 2. 화면 개발 권장 사항 (팀원용 파일 작성법)

### 💡 기본 작성 스타일: "알맹이 위주로 가볍게 짜기"

- `<!DOCTYPE html>`, `<html>`, `<head>`, `<body>` 태그는 공통 레이아웃 파일(`admin-layout.html`)이 완벽하게 표준 구조로 제공하므로 **작성하지 않는 것을 권장**합니다.
- 최상단은 전체 콘텐츠를 감싸는 하나의 `<div>` 태그로 시작하여 알맹이 내부 코드만 작성해 주세요.

### 💻 권장 작성 예시 (`templates/main.html` 또는 `hello.html`)

```html
<div>
  <div th:if="${message != null}">
    <p th:text="${message}">Message goes here</p>
  </div>

  <h1>Welcome to the Admin Main Page</h1>
  <a href="/login"><button>Go to Login Page</button></a>
  <a href="/admin/test"><button>Go to Test Page</button></a>
</div>

<th:block th:fragment="pageCss">
  <link rel="stylesheet" th:href="@{/css/page/main-dashboard.css}" />
</th:block>

<th:block th:fragment="pageJs">
  <script th:src="@{/js/page/main-dashboard.js}"></script>
</th:block>
```

### 🛠️ 예외 상황 발생 시 (유연한 확장)

외부 마크업 코드를 그대로 긁어와서 사용해야 하거나 특수 케이스로 인해 본문 파일에 `<html>`이나 `<body>` 태그가 부득이하게 포함되어도 **타임리프 엔진은 심각한 에러를 발생시키지 않고 유연하게 파싱**합니다. 정해진 알맹이 스타일 가이드를 따르는 것이 아웃풋 소스코드 측면에서 가장 깔끔하지만, 예외 상황 시에는 유연하게 껍데기를 포함하여 작성하셔도 괜찮습니다.

---

## ☕ 3. 백엔드 컨트롤러(Java Controller) 작성 규칙

1. 브라우저 탭에 표시될 페이지 제목은 HTML에서 하드코딩하지 않고, 컨트롤러에서 `model.addAttribute("pageTitle", "페이지 제목");`을 통해 주입합니다.
2. 리턴 값은 뷰 리졸버가 작동할 수 있도록 반드시 **`admin:/[템플릿파일명]`** 형태로 리턴합니다.

### 💻 컨트롤러 작성 예시

```java
@Controller
public class MainPageController {

    @GetMapping("/")
    public String mainPage(Model model) {
        // 1. 브라우저 타이틀 주입
        model.addAttribute("pageTitle", "Main Page");

        // 2. admin:/ 접두사를 사용하여 templates/main.html 호출
        return "admin:/main";
    }
}

```

---

## 🏗️ 4. 백엔드 아키텍처 핵심 코드 (시스템 관리용)

### ① `LayoutThymeleafViewResolver.java`

`ThymeleafViewResolver`를 확장하여 `admin:/` 구문을 파싱하고, 최종 뼈대인 레이아웃에 알맹이 변수(`${contentPage}`)를 심어주는 핵심 뷰 리졸버입니다.

```java
package kr.or.ddit.config;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.springframework.web.servlet.View;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;

public class LayoutThymeleafViewResolver extends ThymeleafViewResolver {

    @Override
    protected View loadView(String viewName, Locale locale) throws Exception {
        if (viewName.startsWith("redirect:") || viewName.startsWith("forward:") || viewName.equals("error") || viewName.equals("login")) {
            return super.loadView(viewName, locale);
        }

        String layoutTemplate = viewName.startsWith("admin:/") ? "layouts/admin-layout" : "layouts/default";
        final String originalView = viewName;

        return new View() {
            @Override
            public String getContentType() { return "text/html;charset=UTF-8"; }

            @Override
            public void render(Map<String, ?> model, jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response) throws Exception {
                Map<String, Object> mergedModel = new HashMap<>(model);

                // admin:/ 접두사를 떼어내 순수 파일명만 contentPage 변수로 매핑
                String pureViewName = originalView;
                if (originalView.startsWith("admin:/")) {
                    pureViewName = originalView.replace("admin:/", "");
                }

                mergedModel.put("contentPage", pureViewName);

                View layoutView = LayoutThymeleafViewResolver.super.loadView(layoutTemplate, locale);
                if (layoutView != null) {
                    layoutView.render(mergedModel, request, response);
                }
            }
        };
    }
}

```

### ② `WebConfig.java`

스프링 부트 기본 뷰 리졸버보다 우선순위(`order = 1`)를 높여 우리가 커스텀한 리졸버가 먼저 동작하도록 등록해 줍니다.

```java
@Configuration
public class WebConfig {

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Bean
    public LayoutThymeleafViewResolver thymeleafViewResolver() {
        LayoutThymeleafViewResolver resolver = new LayoutThymeleafViewResolver();
        resolver.setTemplateEngine(templateEngine);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setOrder(1);
        return resolver;
    }
}

```

---

## 🏗️ 5. 마스터 레이아웃 파일 (`templates/layouts/admin-layout.html`)

타임리프 파싱 버그를 원천 차단하기 위해 동적 변수 뒤에 기호가 붙지 않는 최적의 문법 표준 스펙을 유지하고 있습니다. 개별 CSS/JS 조각이 없더라도 엘비스 연산자(`?: ~{}`)를 통해 부드럽게 예외 처리가 됩니다.

```html
<!doctype html>
<html xmlns:th="http://www.thymeleaf.org">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />

    <title th:text="${pageTitle}">기본 관리자 타이틀</title>

    <link rel="stylesheet" th:href="@{/css/tailwind.css}" />
    <link rel="stylesheet" th:href="@{/css/admin-common.css}" />

    <th:block th:replace="~{${contentPage} :: pageCss} ?: ~{}"></th:block>
  </head>
  <body class="bg-gray-50 text-gray-900 antialiased">
    <header th:replace="~{fragments/header :: header}"></header>

    <h1>관리자용 레이아웃 제목 - 이 글씨가 보이면 레이아웃 적용 성공</h1>

    <main class="container mx-auto px-4 py-6" th:insert="~{${contentPage}}"></main>

    <footer th:replace="~{fragments/footer :: footer}</footer>

    <script th:src="@{/js/admin-global.js}"></script>

    <th:block th:replace="~{${contentPage} :: pageJs} ?: ~{}"></th:block>
  </body>
</html>

```

---

## 🎨 6. Tailwind CSS 컴파일 설정 (`tailwind.config.js`)

팀원들이 새로 생성하는 모든 알맹이 파일들에서도 테일윈드 유틸리티 클래스가 정상 빌드 및 스캔될 수 있도록 `content` 영역에 모든 경로 패턴이 유기적으로 지정되어 있습니다.

```javascript
/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    // templates 내부의 깊은 하위 폴더 및 모든 HTML 파일을 추적하여 자동 빌드
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

## 🎯 7. 아키텍처 도입 결과 및 장점

1. **팀원 코드 코스트 최소화**: 매 HTML 파일마다 길고 복잡한 레이아웃 선언 스크립트를 수동으로 적는 휴먼 에러가 차단됩니다.
2. **유지보수 단일화**: 마스터 레이아웃의 파일명이나 위치 경로가 변경되더라도 모든 HTML을 열 필요 없이 자바의 `LayoutThymeleafViewResolver` 내부 경로 한 곳만 수정하면 전역 반영됩니다.
3. **완벽한 소스 구조**: 브라우저 소스 보기창을 열었을 때 `<html>`, `<head>`, `<body>` 구조가 깨지거나 중복 레이어로 겹치지 않는 완전무결한 단일 마크업 웹 표준 페이지가 제공됩니다.
