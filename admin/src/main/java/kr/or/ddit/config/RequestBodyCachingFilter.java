package kr.or.ddit.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;

// AuditInterceptor가 afterCompletion에서 body를 읽을 수 있도록 요청을 래핑
@Component
@Order(1)
public class RequestBodyCachingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String contentType = request.getContentType();
        // multipart는 래핑하지 않음 — ContentCachingRequestWrapper가 multipart body를 소비할 수 있음
        if (contentType != null && contentType.startsWith("multipart/")) {
            filterChain.doFilter(request, response);
        } else {
            // 감사 로그용 body는 최대 10KB만 캐싱 (메모리 보호)
            filterChain.doFilter(new ContentCachingRequestWrapper(request, 10_000), response);
        }
    }
}
