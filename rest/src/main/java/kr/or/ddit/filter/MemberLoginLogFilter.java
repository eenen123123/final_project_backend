package kr.or.ddit.filter;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Map;

import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.or.ddit.finalProject.jwt.JwtTokenProvider;
import kr.or.ddit.finalProject.service.log.MemberLoginLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 일반 회원(MEMBER) 로그인/로그아웃 이력을 MEMBER_LOGIN_LOG에 기록하는 필터.
 * AuthController 등 기존 코드를 수정하지 않고 필터 레이어에서 처리한다.
 */
@Slf4j
@Component
@Order(Integer.MAX_VALUE - 10)
@RequiredArgsConstructor
public class MemberLoginLogFilter extends OncePerRequestFilter {

    private final MemberLoginLogService memberLoginLogService;
    private final JwtTokenProvider      jwtTokenProvider;
    private final ObjectMapper          objectMapper;

    private static final String LOGIN_URI  = "/api/auth/login";
    private static final String LOGOUT_URI = "/api/auth/logout";

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String uri = request.getRequestURI();
        return !"POST".equals(request.getMethod())
            || (!LOGIN_URI.equals(uri) && !LOGOUT_URI.equals(uri));
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        if (LOGIN_URI.equals(request.getRequestURI())) {
            handleLogin(request, response, chain);
        } else {
            handleLogout(request, response, chain);
        }
    }

    // ─── login ──────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private void handleLogin(HttpServletRequest request,
                             HttpServletResponse response,
                             FilterChain chain) throws ServletException, IOException {

        ContentCachingRequestWrapper cached = new ContentCachingRequestWrapper(request, 10 * 1024);
        chain.doFilter(cached, response);

        byte[] body = cached.getContentAsByteArray();
        if (body.length == 0) return;

        try {
            Map<String, String> map = objectMapper.readValue(body, Map.class);
            String inputUserId = map.get("userId");
            if (inputUserId == null || inputUserId.isBlank()) return;

            String loginIp = resolveClientIp(request);

            if (response.getStatus() == HttpServletResponse.SC_OK) {
                memberLoginLogService.recordLoginSuccess(inputUserId, inputUserId, loginIp, null);
            } else {
                memberLoginLogService.recordLoginFailure(
                    inputUserId, null,
                    "로그인 실패 (HTTP " + response.getStatus() + ")",
                    loginIp, null
                );
            }
        } catch (Exception e) {
            log.warn("[MemberLoginLogFilter] 로그인 로그 기록 실패: {}", e.getMessage());
        }
    }

    // ─── logout ─────────────────────────────────────────────────────────────

    private void handleLogout(HttpServletRequest request,
                              HttpServletResponse response,
                              FilterChain chain) throws ServletException, IOException {

        String userId = extractUserIdFromCookie(request);
        chain.doFilter(request, response);

        if (userId != null && response.getStatus() == HttpServletResponse.SC_OK) {
            try {
                memberLoginLogService.recordLogout(userId);
            } catch (Exception e) {
                log.warn("[MemberLoginLogFilter] 로그아웃 로그 기록 실패: {}", e.getMessage());
            }
        }
    }

    // ─── helpers ────────────────────────────────────────────────────────────

    private String extractUserIdFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie c : cookies) {
            if ("refreshToken".equals(c.getName())) {
                try {
                    return jwtTokenProvider.getUserId(c.getValue());
                } catch (Exception e) {
                    return null;
                }
            }
        }
        return null;
    }

    private String resolveClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.split(",")[0].trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.trim();
        }
        ip = request.getRemoteAddr();
        // 로컬호스트(IPv6/IPv4) 접속 시 실제 네트워크 IP로 대체
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip) || "127.0.0.1".equals(ip)) {
            ip = resolveLocalNetworkIp();
        }
        return ip;
    }

    private String resolveLocalNetworkIp() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (!iface.isUp() || iface.isLoopback() || iface.isVirtual()) continue;
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            log.warn("[MemberLoginLogFilter] 네트워크 IP 조회 실패");
        }
        return "127.0.0.1";
    }
}
