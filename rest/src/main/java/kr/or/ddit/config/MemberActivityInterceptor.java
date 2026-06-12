package kr.or.ddit.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.or.ddit.finalProject.aop.ActivityTargetIdHolder;
import kr.or.ddit.finalProject.dto.log.MemberActivityLogDto;
import kr.or.ddit.finalProject.mapper.log.MemberActivityLogMapper;
import kr.or.ddit.finalProject.util.TraceIdHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberActivityInterceptor implements HandlerInterceptor {

    private static final String[] IP_HEADERS = {"X-Forwarded-For", "X-Real-IP"};

    private final MemberActivityLogMapper activityLogMapper;

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request,
                                @NonNull HttpServletResponse response,
                                @NonNull Object handler,
                                @Nullable Exception ex) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            // request attribute 우선 (SecurityContext 유실 대비), SecurityContext 보조
            String userId = (String) request.getAttribute("TOKEN_USER_ID");
            if (userId == null && auth != null && auth.isAuthenticated()
                    && !"anonymousUser".equals(auth.getPrincipal())) {
                userId = auth.getName();
            }
            if (userId == null) return;

            String activityType = truncate(request.getMethod() + " " + request.getRequestURI(), 50);

            MemberActivityLogDto dto = MemberActivityLogDto.builder()
                    .traceId(TraceIdHolder.get())
                    .userId(userId)
                    .activityType(activityType)
                    .targetId(ActivityTargetIdHolder.get())
                    .activityIp(resolveClientIp(request))
                    .build();

            activityLogMapper.insertMemberActivityLog(dto);

        } catch (Exception e) {
            log.warn("[MemberActivityInterceptor] 활동 로그 기록 실패: {}", e.getMessage());
        }
    }

    private String truncate(String s, int max) {
        return s != null && s.length() > max ? s.substring(0, max) : s;
    }

    private String resolveClientIp(HttpServletRequest request) {
        for (String header : IP_HEADERS) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }
        String ip = request.getRemoteAddr();
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip) || "127.0.0.1".equals(ip)) {
            return resolveLocalNetworkIp();
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
                    if (addr instanceof Inet4Address) return addr.getHostAddress();
                }
            }
        } catch (Exception ignored) {}
        return "127.0.0.1";
    }
}
