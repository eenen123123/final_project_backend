# LayoutThymeleafViewResolver 역할 분석

## 기능 개요

관리자(admin) 모듈에서 컨트롤러가 `admin:/xxx` 형태의 뷰 이름을 반환하면, 실제 컨텐츠 템플릿을 공통 레이아웃(`layouts/admin-layout`) 안에 자동으로 끼워 넣어주는 커스텀 `ThymeleafViewResolver`이다. 컨트롤러마다 매번 사이드바·헤더 등 레이아웃 마크업을 반복 작성하지 않아도 되게 해주는 핵심 인프라 코드다.

## 등록 위치

[WebConfig.java](admin/src/main/java/kr/or/ddit/config/WebConfig.java#L22-L29)에서 Bean으로 등록된다.

```java
@Bean
public LayoutThymeleafViewResolver thymeleafViewResolver() {
    LayoutThymeleafViewResolver resolver = new LayoutThymeleafViewResolver(memberMapper);
    resolver.setTemplateEngine(templateEngine);
    resolver.setCharacterEncoding("UTF-8");
    return resolver;
}
```

Spring MVC의 기본 `ThymeleafViewResolver`를 상속해서 만든 커스텀 리졸버이며, `MemberMapper`를 직접 주입받아 로그인한 관리자 정보를 조회하는 데 사용한다.

## 동작 흐름 ([LayoutThymeleafViewResolver.java](admin/src/main/java/kr/or/ddit/config/LayoutThymeleafViewResolver.java))

`loadView(viewName, locale)`를 오버라이드해서 뷰 이름에 따라 분기한다.

### ① 예외 케이스 — 레이아웃 적용 안 함 (L27-34)

- `redirect:`, `forward:`로 시작하는 뷰
- `error` 뷰
- `login` 뷰

이 경우는 원본 로직(`super.loadView`)을 그대로 호출해서 레이아웃 없이 반환한다. 로그인 페이지나 리다이렉트까지 레이아웃을 씌우면 안 되기 때문이다.

### ② `admin:/` 접두어가 없는 뷰 — 레이아웃 적용 안 함 (L37-39)

일반 뷰 이름은 그대로 원본 뷰를 반환한다. 즉 **레이아웃 적용 여부는 컨트롤러가 반환하는 뷰 이름의 접두어(`admin:/`)로 결정**되는 구조다.

### ③ `admin:/` 접두어가 있는 뷰 — 레이아웃 씌워서 반환 (L41-75)

1. `admin:/` 접두어를 떼어낸 순수 뷰 이름(`pureViewName`)을 구한다.
2. 익명 `View` 객체를 만들어 반환하는데, 실제 렌더링 시점(`render()`)에 다음을 수행한다.
   - **로그인 관리자 정보 주입**: `request.getUserPrincipal()`이 있으면 `memberMapper.findByUserId()`로 조회해서 `adminName`, `adminUserName`, `adminUserProfile`(프로필 이미지 URL이 있을 때만)을 request 속성으로 심어준다. 사이드바 상단 프로필 표시 등에 쓰인다.
   - **모델에 `contentPage` 추가**: 원래 모델을 복사한 뒤 `contentPage` 키로 `pureViewName`을 넣는다.
   - **레이아웃 템플릿(`layouts/admin-layout`)을 로드해서 렌더링**: 이때 `contentPage`가 담긴 모델을 그대로 넘긴다.

### ④ 레이아웃 템플릿에서의 조립 ([admin-layout.html](admin/src/main/resources/templates/layouts/admin-layout.html))

- `<title th:replace="~{${contentPage} :: pageTitle} ?: _">` — 컨텐츠 템플릿에 정의된 `pageTitle` 프래그먼트를 가져와 페이지 제목으로 사용(없으면 기본 제목 유지)
- `<th:block th:insert="~{${contentPage}}"></th:block>` — 컨텐츠 템플릿 전체를 본문 영역(`<main>`)에 삽입

즉, `contentPage`라는 모델 값 하나로 Thymeleaf의 `th:insert`가 "어떤 화면을 레이아웃 안에 넣을지"를 동적으로 결정하는 구조다.

## 왜 이런 구조가 필요한가

- 컨트롤러는 `return "admin:/instructor/dashboard";`처럼 접두어만 붙이면 사이드바·헤더가 포함된 완성된 관리자 페이지를 얻는다.
- 로그인 사용자 정보 조회(`memberMapper`)를 뷰 리졸버 레벨에서 한 번만 처리하므로, 각 컨트롤러가 매번 관리자 이름/프로필을 모델에 담아줄 필요가 없다.
- `login`, `error`, `redirect`처럼 레이아웃이 필요 없는 화면은 접두어를 안 붙이면 그만이라 예외 처리가 단순하다.

## 한 줄 요약

컨트롤러가 `admin:/`로 시작하는 뷰 이름을 반환하면, 로그인한 관리자 정보를 조회해서 모델에 채워 넣고 해당 화면을 공통 관리자 레이아웃(`admin-layout.html`) 안에 자동으로 삽입해 렌더링해주는 커스텀 뷰 리졸버다.
