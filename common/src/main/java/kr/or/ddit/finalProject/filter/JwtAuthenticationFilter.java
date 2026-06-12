package kr.or.ddit.finalProject.filter;

import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.or.ddit.finalProject.dto.member.MemberDto;
import kr.or.ddit.finalProject.jwt.JwtTokenProvider;
import kr.or.ddit.finalProject.service.member.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;
    // private final UserService userService;
    private final MemberService memberService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // Enumeration<String> names = request.getHeaderNames();
        // if (names != null) {
        //     while (names.hasMoreElements()) {
        //         String n = names.nextElement();
        //         log.info("Header: {} = {}", n, request.getHeader(n));
        //     }
        // }
        String authorizationHeader = request.getHeader("Authorization");
        // log.info("Authorization header: {}", authorizationHeader);
        // JWT 토큰이 "Bearer "로 시작하는지 확인
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);

            // JWT 토큰이 유효한지 검증
            if (jwtTokenProvider.validateToken(token)) {
                MemberDto user = memberService.getMemberByToken(token);
                // 인증 객체 생성 및 SecurityContext에 저장
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(user.getUserId(), null,
                                // List.of(new SimpleGrantedAuthority(user.getUserRole())));
                                List.of(new SimpleGrantedAuthority(user.getUserRole())));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                // 활동 로그용으로 request attribute에도 저장 (SecurityContext 유실 대비)
                request.setAttribute("TOKEN_USER_ID", user.getUserId());
            } else {
                // 만료된 토큰이라도 userId는 활동 로그 기록을 위해 보존
                String userId = jwtTokenProvider.getUserIdIgnoreExpiry(token);
                if (userId != null) {
                    request.setAttribute("TOKEN_USER_ID", userId);
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
