package kr.or.ddit.config;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class AdminSecurityConfig {

    private final MenuPermissionAuthorizationManager menuPermissionManager;

    // 뷰 리졸버 작성 후 아래 코드를 사용하면 보안 관련 설정이 먹통이 되는 문제가 발생했음.
    // @Bean
    // public WebSecurityCustomizer webSecurityCustomizer() {
    // // 정적 리소스에 대한 보안 설정을 무시하도록 구성
    // return (web) -> web.ignoring().requestMatchers(
    // PathRequest.toStaticResources().atCommonLocations());
    // }
    // @Bean
    // public SecurityFilterChain securityFilterChain(HttpSecurity http) throws
    // Exception {
    // http.csrf(AbstractHttpConfigurer::disable).authorizeHttpRequests(requests
    // -> requests.requestMatchers("/",
    // "/login").permitAll().requestMatchers("/admin/**")
    // .hasRole("ADMIN"))
    // .formLogin(auth -> auth.loginPage("/login")
    // .loginProcessingUrl("/login")
    // .defaultSuccessUrl("/"))
    // .logout(logout -> logout.permitAll());
    // return http.build();
    // }
    // HttpSessionDestroyedEvent 발행에 필요 — 없으면 로그아웃/세션만료 이벤트가 터지지 않음
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    //@formatter:off
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(requests -> requests
                // 뷰어(직급 Z002): 읽기 전용 계정 — 반드시 다른 규칙보다 먼저 평가
                //   · GET 이 아닌 요청(저장/수정/삭제/승인 등)은 무조건 거부
                //   · GET 요청은 전체 메뉴 조회를 위해 전부 허용
                .requestMatchers(viewerWriteRequest()).denyAll()
                .requestMatchers(viewerReadRequest()).permitAll()

                // 정적 리소스에 대한 보안 설정을 무시하도록 구성
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()

                .requestMatchers("/api/temp/accounts").permitAll() // 개발용 API, 실제 서비스에서는 제거 예정

                // 로그인 페이지만 개방
                .requestMatchers("/login").permitAll()
                .requestMatchers("/static/**").permitAll()

                .requestMatchers(HttpMethod.GET, "/admin/approval/template/view").permitAll()

                // 드롭다운 데이터 조회 API — 관리자 전체 허용 (읽기 전용)
                .requestMatchers(HttpMethod.GET, "/instructor/questions/ai/subjects",
                                               "/instructor/questions/ai/weak-points").hasRole("ADMIN")

                // 공통코드 드롭다운 옵션 조회 — 읽기 전용이므로 ADMIN 전체 허용
                // (관리 화면/CRUD는 아래 권한 매트릭스 유지)
                .requestMatchers(HttpMethod.GET, "/admin/common-codes/options/**").hasRole("ADMIN")

                // PD 전용 (D200) — 권한 매트릭스 외 별도 관리
                .requestMatchers("/admin/media/**").hasAuthority("D200")

                // 학생 승인 — 원장(D400)은 menuPermissionManager가 처리, 명시적으로 앞에 선언
                .requestMatchers("/admin/students/approve/**").hasAuthority("D400")

                // 직급별 세부 권한이 필요한 모든 경로 — DB 기반 권한 매트릭스 적용
                .requestMatchers(
                        "/admin/monitoring/**", "/admin/system/**", "/admin/finance/**",
                        "/admin/quality/**", "/admin/settings/permissions/**",
                        "/admin/instructors/monitor/**", "/admin/consultation/**", "/admin/retention/**",
                        "/admin/students/**", "/admin/org/**", "/admin/blacklist/**",
                        "/admin/billing/**", "/admin/attendance/**", "/admin/textbook/**",
                        "/admin/logistics/**", "/admin/featured/**", "/admin/coupon/**",
                        "/admin/expenses/**", "/admin/salary/**", "/admin/hr/**",
                        "/admin/parent/**", "/admin/subject/**", "/admin/common-codes/**",
                        "/admin/certificates/**",
                        "/instructor/**", "/classroom/**"
                ).access(menuPermissionManager)

                // 알림·결재·일정·강좌조회 — ADMIN 전체 허용 (공통 업무)
                .requestMatchers("/admin/notifications/**", "/admin/messenger/**",
                        "/admin/approval/**", "/admin/schedule/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/admin/course/list", "/admin/course/detail",
                        "/admin/course/subjects", "/admin/course/insert", "/admin/course/edit").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/admin/course/insert", "/admin/course/edit",
                        "/admin/course/delete").hasAuthority("D300")

                // 로그인 페이지를 제외한 모든 페이지는 관리자 권한을 가진 사용자만 접근 가능
                .anyRequest().hasRole("ADMIN"))
                .formLogin(auth -> auth.loginPage("/login").loginProcessingUrl("/login")
                        .defaultSuccessUrl("/") // 로그인 성공하면 원래 가려던 루트(/)로 이동
                ).logout(logout -> logout.permitAll());

        return http.build();
    }
    //@formatter:on

    // 뷰어(읽기 전용) 계정 여부 — 직급 권한이 Z002 이면 뷰어
    private boolean isViewer() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && auth.getAuthorities().stream()
                .anyMatch(a -> "Z002".equals(a.getAuthority()));
    }

    // 뷰어의 쓰기 요청(GET 이외): 저장/수정/삭제/승인 등 → 차단 대상
    private RequestMatcher viewerWriteRequest() {
        return request -> isViewer() && !HttpMethod.GET.matches(request.getMethod());
    }

    // 뷰어의 읽기 요청(GET): 전체 메뉴 조회 허용 대상
    private RequestMatcher viewerReadRequest() {
        return request -> isViewer() && HttpMethod.GET.matches(request.getMethod());
    }
}
