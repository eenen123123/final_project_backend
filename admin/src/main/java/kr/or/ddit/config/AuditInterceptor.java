package kr.or.ddit.config;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.or.ddit.dto.AuditLogDto;
import kr.or.ddit.finalProject.util.TraceIdHolder;
import kr.or.ddit.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditInterceptor implements HandlerInterceptor {

    private static final int PARAMS_MAX_LEN = 4000;
    private static final Set<String> MASKED_KEYS
            = Set.of("password", "passwd", "secretkey", "secret", "token", "accesstoken", "refreshtoken");
    private static final String[] IP_HEADERS = {
        "X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP",
        "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"
    };

    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler) {
        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler,
            @org.springframework.lang.Nullable Exception ex) {
        try {
            String traceId = TraceIdHolder.get();
            String adminId = resolveAdminId();
            String ip = resolveClientIp(request);
            String params = extractParams(request);

            AuditLogDto dto = AuditLogDto.builder()
                    .traceId(traceId != null ? traceId : "unknown")
                    .adminId(adminId)
                    .memberIp(ip)
                    .httpMethod(request.getMethod())
                    .requestUri(request.getRequestURI())
                    .requestParams(params)
                    .statusCode(response.getStatus())
                    .build();

            auditLogService.save(dto);
        } catch (Exception e) {
            log.error("[AUDIT] Interceptor 오류: {}", e.getMessage());
        }
    }

    // ── 파라미터 수집 ──────────────────────────────────────────
    private String extractParams(HttpServletRequest request) {
        String contentType = request.getContentType();

        // 파일 업로드 — body 수집 생략
        if (contentType != null && contentType.startsWith("multipart/")) {
            return "[파일 업로드 요청됨]";
        }

        // JSON body (POST/PUT/PATCH)
        if (request instanceof ContentCachingRequestWrapper wrapper) {
            byte[] body = wrapper.getContentAsByteArray();
            if (body.length > 0) {
                String bodyStr = new String(body, StandardCharsets.UTF_8);
                return truncate(maskJsonBody(bodyStr));
            }
        }

        // Query string (GET 등)
        String qs = request.getQueryString();
        if (qs != null && !qs.isBlank()) {
            return truncate(maskQueryString(qs));
        }

        return null;
    }

    private String maskJsonBody(String body) {
        try {
            JsonNode node = objectMapper.readTree(body);
            if (node instanceof ObjectNode obj) {
                MASKED_KEYS.forEach(key -> {
                    if (obj.has(key)) {
                        obj.put(key, "[MASKED]");
                    }
                });
                return obj.toString();
            }
            return body;
        } catch (Exception e) {
            return body;
        }
    }

    private String maskQueryString(String qs) {
        return Arrays.stream(qs.split("&"))
                .map(param -> {
                    String[] kv = param.split("=", 2);
                    if (kv.length == 2 && MASKED_KEYS.contains(kv[0].toLowerCase())) {
                        return kv[0] + "=[MASKED]";
                    }
                    return param;
                })
                .collect(Collectors.joining("&"));
    }

    private String truncate(String s) {
        if (s == null || s.length() <= PARAMS_MAX_LEN) {
            return s;
        }
        return s.substring(0, PARAMS_MAX_LEN) + "...[truncated]";
    }

    // ── 보조 ──────────────────────────────────────────────────
    private String resolveAdminId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }
        return "anonymous";
    }

    private String resolveClientIp(HttpServletRequest request) {
        for (String header : IP_HEADERS) {
            String ip = request.getHeader(header);
            if (isValidIp(ip)) {
                return ip.split(",")[0].trim();
            }
        }
        String remoteAddr = request.getRemoteAddr();
        // 루프백/미확인 주소면 실제 네트워크 NIC IP로 대체
        if ("::1".equals(remoteAddr) || "0:0:0:0:0:0:0:1".equals(remoteAddr)
                || "127.0.0.1".equals(remoteAddr) || "0.0.0.0".equals(remoteAddr)) {
            return resolveLocalNetworkIp();
        }
        return remoteAddr;
    }

    private boolean isValidIp(String ip) {
        return ip != null && !ip.isBlank()
                && !"unknown".equalsIgnoreCase(ip)
                && !"0.0.0.0".equals(ip);
    }

    private String resolveLocalNetworkIp() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (!iface.isUp() || iface.isLoopback() || iface.isVirtual()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            log.warn("[AUDIT] 네트워크 IP 조회 실패");
        }
        return "127.0.0.1";
    }
}
