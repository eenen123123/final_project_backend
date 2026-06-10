package kr.or.ddit.aop;

import java.lang.reflect.Parameter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import kr.or.ddit.finalProject.aop.ActivityTargetIdHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import kr.or.ddit.finalProject.dto.log.MemberActivityLogDto;
import kr.or.ddit.finalProject.mapper.log.MemberActivityLogMapper;
import lombok.extern.slf4j.Slf4j;

/**
 * MemberActivityAspect
 *
 * ✔ 일반 사용자의 API 호출 행동을 감지하여 인적 사항, 활동 종류, 접속 대상 및 IP를 자동으로 데이터베이스에 기록하는 공통 모듈 컴포넌트
 *
 * ✔ 역할 요약
 * ---------------------------------------------------------------------
 * - RestController 하위 주소 중 인증 처리 부분을 제외한 전역 요청 감시
 * - Spring Security와 연동하여 실시간 행위 유저 정보 식별
 * - 클래스 정보 분석을 통한 URL 경로 변수(@PathVariable) 및 스레드 저장소 기반 대상 식별자 추출
 * - 네트워크 인터페이스 탐색 기반의 신뢰성 높은 클라이언트 IP 판별
 * - 수집된 감사 지표의 데이터베이스 저장 실행
 *
 * ✔ 설계 목적
 * ---------------------------------------------------------------------
 * 1. 비즈니스 컨트롤러 코드 내부에 로그 저장 로직이 중복 삽입되는 번거로운 코드를 제거
 * 2. 부가 기능 수행 중 예외가 발생하더라도 실제 핵심 비즈니스 로직의 수행은 정상 보장되도록 결합을 분리
 * 3. 독립적인 데이터 전달 모델을 활용하여 등록/수정 메커니즘의 동적 식별자 추적망 형성
 *
 * ✔ 아키텍처 위치 (공통 기능 Layer)
 * ---------------------------------------------------------------------
 * [Browser Client Request]
 * ↓
 * [Spring Security Filter Chain]
 * ↓
 * [MemberActivityAspect.logMemberActivity()] -> (중간 감지 및 처리)
 * ├── (사전 처리) : 실제 메서드 수행 (pjp.proceed)
 * ├── (사후 처리) : Security / Request 맥락 분석 및 IP 정제
 * └── (데이터베이스 기록) : MemberActivityLogMapper 연동
 * ↓
 * [Target RestController Endpoint]
 */
@Slf4j
@Aspect
public class MemberActivityAspect {

    private final MemberActivityLogMapper activityLogMapper;

    /**
     * 생성자 의존성 주입
     * @param activityLogMapper 로그 전용 매퍼 객체
     */
    public MemberActivityAspect(MemberActivityLogMapper activityLogMapper) {
        this.activityLogMapper = activityLogMapper;
    }

    /**
     * 지정된 RestController 범위에 대한 비동기 로그 처리 및 자동 저장 실행
     *
     * ✔ 대상 범위 스펙
     * ---------------------------------------------------------------------
     * - @RestController 어노테이션이 선언된 모든 클래스의 메서드 타깃 지정
     * - 다만, 로그인/로그아웃 등 인증을 전담하는 auth 패키지 하위 제어기는 감시 범위에서 제외함
     *
     * @param pjp           대상 메서드의 실행 제어권 및 정보 스냅샷을 보유한 객체
     * @return              타깃 메서드가 원래 반환해야 하는 결과 Object 인스턴스
     * @throws Throwable    타깃 비즈니스 메서드 수행 중 발생한 런타임 예외 일체
     */
    @Around("within(@org.springframework.web.bind.annotation.RestController *)" +
            " && !within(kr.or.ddit.controller.auth..*)")
    public Object logMemberActivity(ProceedingJoinPoint pjp) throws Throwable {
        try {
            // 1. 타깃 컨트롤러 메서드를 먼저 실행하여 비즈니스 로직을 온전히 수행한 후 결과 값을 확보한다.
            Object result = pjp.proceed();

            try {
                // 2. 현재 작업 스레드에서 로그인된 인증 객체를 꺼내온다.
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();

                // 3. 로그인하지 않은 유저나 익명 사용자의 접근일 경우 기록 대상이 아니므로 로그를 남기지 않는다.
                if (auth == null || !auth.isAuthenticated()
                        || "anonymousUser".equals(auth.getPrincipal())) return result;

                // 4. 서블릿 환경으로부터 현재 가동 중인 HTTP 요청 객체를 가져온다.
                HttpServletRequest request = currentRequest();
                if (request == null) return result;

                // 5. HTTP 메서드 정보와 URI 경로를 조합하여 식별성 높은 활동 타입 문자열을 생성하고 제한 길이에 맞게 자른다.
                String method       = request.getMethod();
                String uri          = request.getRequestURI();
                String activityType = truncate(method + " " + uri, 50);

                // 6. 1순위로 URL 검로 변수(@PathVariable)의 숫자형 식별자를 추출하고, 없을 경우 스레드 저장소에서 추출한다.
                String targetId = extractPathVariableId(pjp);
                if (targetId == null) targetId = ActivityTargetIdHolder.get();

                // 7. 정제된 인자값들을 기반으로 활동 로그 데이터 객체를 조립한다.
                MemberActivityLogDto dto = MemberActivityLogDto.builder()
                        .userId(auth.getName())
                        .activityType(activityType)
                        .targetId(targetId)
                        .activityIp(resolveClientIp(request))
                        .build();

                // 8. 매퍼 계층을 구동하여 데이터베이스에 사용자의 활동 이력을 최종 저장한다.
                activityLogMapper.insertMemberActivityLog(dto);

            } catch (Exception e) {
                // 9. 로그 데이터 분석 및 저장 실패가 본래의 서비스 실행에 지장을 주지 않도록 예외를 흡수하고 경고 로그만 남긴다.
                log.warn("[MemberActivityAspect] 활동 로그 기록 실패: {}", e.getMessage());
            }

            return result;
        } finally {
            // 10. 메모리 누수 방지 및 다음 요청 스레드의 오염을 방지하기 위해 저장소 내부 데이터를 완전히 비운다.
            ActivityTargetIdHolder.clear();
        }
    }

    /**
     * 타깃 메서드 매개변수 분석을 통한 URL 경로 바인딩 식별자(@PathVariable) 추출
     * @param pjp   실행 정보 객체
     * @return      파라미터 중 PathVariable 어노테이션이 존재하며 숫자형태인 경우의 문자열 식별값
     */
    private String extractPathVariableId(ProceedingJoinPoint pjp) {
        MethodSignature sig = (MethodSignature) pjp.getSignature();
        Parameter[] params  = sig.getMethod().getParameters();
        Object[]    args    = pjp.getArgs();

        // 1. 타깃 메서드의 매개변수 배열을 순회하며 구조 분석을 시도한다.
        for (int i = 0; i < params.length; i++) {
            // 2. 어노테이션이 @PathVariable이고, 실제 넘어온 아규먼트 값이 숫자 대역인 대상을 필터링한다.
            if (params[i].isAnnotationPresent(PathVariable.class)
                    && args[i] instanceof Number) {
                return String.valueOf(args[i]);
            }
        }
        return null;
    }

    /**
     * Spring의 요청 스레드로부터 실시간 HttpServletRequest 객체 반환
     * @return 현재 바인딩된 HTTP 서블릿 요청 객체 포인터
     */
    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }

    /**
     * DB 컬럼의 데이터 글자수 제한에 대응하기 위한 문자열 커팅
     * @param s     대상 문자열
     * @param max   최대 가용 길이 범위
     * @return      절단 가공이 완료된 문자열
     */
    private String truncate(String s, int max) {
        return s != null && s.length() > max ? s.substring(0, max) : s;
    }

    /**
     * 중계 서버 환경을 고려한 실제 클라이언트 원격 IP 판별
     * @param request   서블릿 요청 객체
     * @return          전달 헤더를 대조하여 획득한 물리 클라이언트 IP 주소
     */
    private String resolveClientIp(HttpServletRequest request) {
        String[] headers = {"X-Forwarded-For", "X-Real-IP"};

        // 1. 중계 서버나 게이트웨이를 거치며 유실될 수 있는 원격 IP 헤더 속성들을 순차 검증한다.
        for (String h : headers) {
            String ip = request.getHeader(h);
            if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
                // 2. 여러 개의 중계 IP가 콤마로 연결되어 도달할 경우 첫 번쨰 원본 클라이언트 IP를 추출한다.
                return ip.split(",")[0].trim();
            }
        }
        // 3. 변조 헤더가 없을 시 표준 리모트 네트워크 주소를 획득한다.
        String ip = request.getRemoteAddr();

        // 4. 로컬 호스트 가상 주소로 매핑되어 도달했을 시, 로컬 네트워크 하드웨어의 실제 사설 주소 조회를 시도한다.
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip) || "127.0.0.1".equals(ip)) {
            ip = resolveLocalNetworkIp();
        }
        return ip;
    }

    /**
     * 가상화 루프백 주소 인지 시 서버 장비의 실제 내부 사설 IP 식별 스캔
     * @return 로컬 머신의 활성화된 물리 네트워크 인터페이스 IP 주소 문자열
     */
    private String resolveLocalNetworkIp() {
        try {
            // 1. 시스템 환경에 연결된 모든 네트워크 인터페이스 카드를 탐색한다.
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();

                // 2. 비활성화 상태이거나 가상/루프백 장치는 제외 대상이므로 스킵한다.
                if (!iface.isUp() || iface.isLoopback() || iface.isVirtual()) continue;

                // 3. 할당된 서브넷 아이피 주소록을 파싱한다.
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    // 4. IPv6 대역을 제외한 명확한 IPv4 주소 형태만 필터링하여 문자열 주소를 즉시 반환한다.
                    if (addr instanceof Inet4Address) return addr.getHostAddress();
                }
            }
        } catch (Exception ignored) {} // 5. 자원 스캔 중 발생한 예외는 무시하고 기본 루프백 주소 체계를 차선책으로 반환한다.
        return "127.0.0.1";
    }
}
