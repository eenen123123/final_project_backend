package kr.or.ddit.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Configuration
public class WebConfig {

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Bean
    public LayoutThymeleafViewResolver thymeleafViewResolver() {
        LayoutThymeleafViewResolver resolver = new LayoutThymeleafViewResolver();
        resolver.setTemplateEngine(templateEngine);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setOrder(1); // 스프링 부트 기본 리졸버보다 우선순위를 높게 설정합니다.
        return resolver;
    }
}
