package kr.or.ddit.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * 모든 컨트롤러에 공통 모델 변수를 주입한다.
 * Thymeleaf에서 #httpServletRequest가 null로 조회되는 환경이므로
 * @ModelAttribute로 직접 주입하는 방식을 사용한다.
 */
@ControllerAdvice
public class GlobalModelAdvice {

    // 사이드바 활성 메뉴 판별에 사용
    @ModelAttribute("currentUri")
    public String currentUri(HttpServletRequest request) {
        return request.getRequestURI();
    }
}
