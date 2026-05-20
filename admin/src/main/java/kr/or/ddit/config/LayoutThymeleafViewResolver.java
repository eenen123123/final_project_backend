package kr.or.ddit.config;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.web.servlet.View;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;

public class LayoutThymeleafViewResolver extends ThymeleafViewResolver {

    @Override
    protected View loadView(String viewName, Locale locale) throws Exception {

        // 1. 예외 케이스 처리 (기존 코드 그대로 유지)
        if (viewName.startsWith("redirect:") || viewName.startsWith("forward:") || viewName.equals("error")) {
            return super.loadView(viewName, locale);
        }

        if (viewName.equals("login")) {
            return super.loadView(viewName, locale);
        }

        // 2. 레이아웃 템플릿 결정 (기존 코드 그대로 유지)
        String layoutTemplate;
        if (viewName.startsWith("admin:/")) {
            layoutTemplate = "layouts/admin-layout";
        } else {
            layoutTemplate = "layouts/default";
        }

        final String originalView = viewName;

        // 3. 커스텀 렌더링을 위한 익명 View 객체 반환 (기존 구조 유지)
        return new View() {
            @Override
            public String getContentType() {
                return "text/html;charset=UTF-8";
            }

            @Override
            public void render(Map<String, ?> model, jakarta.servlet.http.HttpServletRequest request,
                    jakarta.servlet.http.HttpServletResponse response) throws Exception {

                Map<String, Object> mergedModel = new HashMap<>(model);

                // ⭐️ [여기만 수정!] 
                // 원래는 "admin:main"이 그대로 들어갔지만, "admin:" 접두어를 칼같이 떼어내서
                // 타임리프가 실제 파일인 "main"을 똑바로 찾아갈 수 있도록 순수 파일명만 가공합니다.
                String pureViewName = originalView;
                if (originalView.startsWith("admin:/")) {
                    pureViewName = originalView.replace("admin:/", "");
                } else if (originalView.startsWith("default:")) { // 혹시 default 접두어도 쓰신다면 처리
                    pureViewName = originalView.replace("default:", "");
                }

                // 가공된 순수 파일명(예: "main")을 타임리프 변수로 심어줍니다.
                mergedModel.put("contentPage", pureViewName);

                View layoutView = LayoutThymeleafViewResolver.super.loadView(layoutTemplate, locale);
                if (layoutView != null) {
                    layoutView.render(mergedModel, request, response);
                }
            }
        };
    }
}
