package kr.or.ddit.finalProject.service.log;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.web.session.HttpSessionDestroyedEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 로그인/로그아웃 이벤트 리스너
 * - 로그인 성공 시 로그인 로그 기록
 * - 로그아웃 또는 세션 타임아웃 시 로그아웃 일시 업데이트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoginEventListener {

    private final LoginLogService loginLogService;

    // 서버 완전히 기동된 후 미처리 세션(서버 강제 종료 등으로 LOGOUT_DT가 NULL인 채 남은 것) 일괄 정리
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        loginLogService.closeAllOpenSessions();
    }

    // 로그인 성공 시 Spring Security가 자동으로 발행하는 이벤트
    @EventListener
    public void onLoginSuccess(InteractiveAuthenticationSuccessEvent event) {
        String userId = event.getAuthentication().getName();

        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            log.warn("[LoginLog] RequestContextHolder 없음 - IP/세션 수집 불가");
            return;
        }

        HttpServletRequest request = attrs.getRequest();

        // 프록시/로드밸런서 환경에서는 실제 IP가 X-Forwarded-For 헤더에 담긴다.
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) {
            ip = request.getRemoteAddr();
        }
        // localhost(::1, 0:0:0:0:0:0:0:1, 127.0.0.1)로 접속한 경우 실제 네트워크 IP로 대체한다.
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip) || "127.0.0.1".equals(ip)) {
            ip = resolveLocalIp();
        }

        String sessionId = request.getSession(true).getId();
        loginLogService.saveLoginLog(userId, ip, sessionId);
    }

    // 로그아웃 또는 세션 타임아웃 시 서블릿 컨테이너가 자동으로 발행하는 이벤트
    @EventListener
    public void onSessionDestroyed(HttpSessionDestroyedEvent event) {
        HttpSession session = event.getSession();
        loginLogService.saveLogoutLog(session.getId());
    }

    // 루프백 접속 시 실제 네트워크 인터페이스의 IPv4를 반환한다.
    // InetAddress.getLocalHost()는 WSL/Linux에서 127.0.0.1을 반환할 수 있어 직접 순회한다.
    private String resolveLocalIp() {
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
            log.warn("[LoginLog] 네트워크 IP 조회 실패");
        }
        return "127.0.0.1";
    }
}
