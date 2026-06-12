package kr.or.ddit.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.or.ddit.finalProject.aop.ActivityTargetIdHolder;
import kr.or.ddit.finalProject.util.TraceIdHolder;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
public class TraceIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            TraceIdHolder.set(UUID.randomUUID().toString().replace("-", "").substring(0, 16));
            filterChain.doFilter(request, response);
        } finally {
            TraceIdHolder.clear();
            ActivityTargetIdHolder.clear();
        }
    }
}
