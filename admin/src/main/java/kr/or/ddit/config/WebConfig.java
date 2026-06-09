package kr.or.ddit.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Configuration
public class WebConfig implements WebMvcConfigurer{

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Autowired
    private AuditInterceptor auditInterceptor;

    @Bean
    public LayoutThymeleafViewResolver thymeleafViewResolver() {
        LayoutThymeleafViewResolver resolver = new LayoutThymeleafViewResolver();
        resolver.setTemplateEngine(templateEngine);
        resolver.setCharacterEncoding("UTF-8");
        // resolver.setOrder(1); // 스프링 부트 기본 리졸버보다 우선순위를 높게 설정합니다.
        return resolver;
    }

    @Override
    public void addInterceptors(@org.springframework.lang.NonNull InterceptorRegistry registry) {
        registry.addInterceptor(auditInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                    "/css/**", "/js/**", "/images/**", "/fonts/**", "/favicon.ico", "/error"
                );
    }
}
