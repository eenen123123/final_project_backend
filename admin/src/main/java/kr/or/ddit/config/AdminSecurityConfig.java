package kr.or.ddit.config;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorityAuthorizationManager;
import org.springframework.security.authorization.AuthorizationManagers;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
public class AdminSecurityConfig {

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
                // 정적 리소스에 대한 보안 설정을 무시하도록 구성
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()

                .requestMatchers("/api/temp/accounts").permitAll() // 개발용 API, 실제 서비스에서는 제거 예정

                // 로그인 페이지만 개방
                .requestMatchers("/login").permitAll()
                .requestMatchers("/static/**").permitAll()

                .requestMatchers(HttpMethod.GET, "/admin/approval/template/view").permitAll()

                // 강사 전용 (D300)
                .requestMatchers("/instructor/**", "/classroom/**").hasAuthority("D300")

                // 강좌 목록/상세/과목목록은 전체 관리자 허용
                .requestMatchers(HttpMethod.GET, "/admin/course/list", "/admin/course/detail", "/admin/course/subjects").hasRole("ADMIN")
                // 강좌 등록/수정/삭제는 강사(D300) 전용
                .requestMatchers(HttpMethod.GET, "/admin/course/insert", "/admin/course/edit").hasAuthority("D300")
                .requestMatchers(HttpMethod.POST, "/admin/course/insert", "/admin/course/edit", "/admin/course/delete").hasAuthority("D300")

                // PD 전용 (D200)
                .requestMatchers("/admin/media/**").hasAuthority("D200")

                // 행정팀장 전용 (D100 + A001)
                .requestMatchers(
                        "/admin/consultation", "/admin/instructors/monitor",
                        "/admin/retention", "/admin/settings/manager-permissions",
                        "/admin/approval/request"
                ).access(AuthorizationManagers.allOf(
                        AuthorityAuthorizationManager.hasAuthority("D100"),
                        AuthorityAuthorizationManager.hasAuthority("A001")
                ))

                // 공통코드 관리 — 행정(D100) 메뉴이나 원장(D400)도 접근 가능
                .requestMatchers("/admin/common-codes/**").hasAnyAuthority("D100", "D400")

                // 알림은 모든 관리자 공통 기능
                .requestMatchers("/admin/notifications/**").hasRole("ADMIN")

                // 행정 전용 (D100)
                .requestMatchers(
                        "/admin/attendance/**", "/admin/billing/**", "/admin/blacklist/**",
                        "/admin/certificates/**", "/admin/coupon/**",
                        "/admin/employees/**", "/admin/expenses/**", "/admin/facilities/**",
                        "/admin/featured/**", "/admin/hr/**", "/admin/logistics/**",
                          "/admin/org/**",
                        "/admin/parent/**", "/admin/salary/**",
                        "/admin/subject/**", "/admin/textbook/**"
                ).hasAuthority("D100")

                // 학생 승인 — 원장(D400) 전용, 학생 수정/퇴직은 행정(D100)이므로 경로를 구체적으로 지정
                .requestMatchers("/admin/students/approve/**").hasAuthority("D400")
                .requestMatchers("/admin/students/**").hasAuthority("D100")

                // 원장 전용 (D400)
                .requestMatchers(
                        "/admin/finance/**", "/admin/monitoring/**", "/admin/payments/approve/**",
                        "/admin/quality/**", "/admin/settings/permissions/**",
                        "/admin/system/**"
                ).hasAuthority("D400")

                // 로그인 페이지를 제외한 모든 페이지는 관리자 권한을 가진 사용자만 접근 가능
                .anyRequest().hasRole("ADMIN"))
                .formLogin(auth -> auth.loginPage("/login").loginProcessingUrl("/login")
                        .defaultSuccessUrl("/") // 로그인 성공하면 원래 가려던 루트(/)로 이동
                ).logout(logout -> logout.permitAll());

        return http.build();
    }
    //@formatter:on
}
