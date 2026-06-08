package kr.or.ddit.config;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.View;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class LayoutThymeleafViewResolver extends ThymeleafViewResolver {

    @Override
    protected View loadView(String viewName, Locale locale) throws Exception {

        // 1. 예외 케이스 처리 (기존 코드 그대로 유지)
        if (viewName.startsWith("redirect:") || viewName.startsWith("forward:")
                || viewName.equals("error")) {
            return super.loadView(viewName, locale);
        }

        if (viewName.equals("login")) {
            return super.loadView(viewName, locale);
        }

        // 2. admin:/ 접두어가 없으면 레이아웃 없이 원본 뷰 반환
        if (!viewName.startsWith("admin:/")) {
            return super.loadView(viewName, locale);
        }

        final String pureViewName = viewName.replace("admin:/", "");
        final String layoutTemplate = "layouts/admin-layout";

        // 3. 커스텀 렌더링을 위한 익명 View 객체 반환 (기존 구조 유지)
        return new View() {
            @Override
            public String getContentType() {
                return "text/html;charset=UTF-8";
            }

            @Override
            public void render(@Nullable Map<String, ?> model, @NonNull HttpServletRequest request,
                    @NonNull HttpServletResponse response) throws Exception {

                if (request.getUserPrincipal() != null) {
                    request.setAttribute("adminName", request.getUserPrincipal().getName());
                }

                Map<String, Object> mergedModel = new HashMap<>(model);
                mergedModel.put("contentPage", pureViewName);

                View layoutView =
                        LayoutThymeleafViewResolver.super.loadView(layoutTemplate, locale);
                if (layoutView != null) {
                    layoutView.render(mergedModel, request, response);
                }
            }
        };
    }
}
