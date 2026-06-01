package kr.or.ddit.config;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class AdminSecurityConfig {

    // 뷰 리졸버 작성 후 아래 코드를 사용하면 보안 관련 설정이 먹통이 되는 문제가 발생했음.
    // @Bean
    // public WebSecurityCustomizer webSecurityCustomizer() {
    //     // 정적 리소스에 대한 보안 설정을 무시하도록 구성
    //     return (web) -> web.ignoring().requestMatchers(
    //             PathRequest.toStaticResources().atCommonLocations());
    // }
    // @Bean
    // public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    //     http.csrf(AbstractHttpConfigurer::disable).authorizeHttpRequests(requests
    //             -> requests.requestMatchers("/", "/login").permitAll().requestMatchers("/admin/**")
    //                     .hasRole("ADMIN"))
    //             .formLogin(auth -> auth.loginPage("/login")
    //             .loginProcessingUrl("/login")
    //             .defaultSuccessUrl("/"))
    //             .logout(logout -> logout.permitAll());
    //     return http.build();
    // }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable).authorizeHttpRequests(requests -> requests
                // 정적 리소스에 대한 보안 설정을 무시하도록 구성
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                // 로그인 페이지만 개방
                .requestMatchers("/login").permitAll()

                .requestMatchers(HttpMethod.GET, "/admin/approval/template/view").permitAll()

                // 로그인 페이지를 제외한 모든 페이지는 관리자 권한을 가진 사용자만 접근 가능
                .anyRequest().hasRole("ADMIN"))
                .formLogin(auth -> auth.loginPage("/login").loginProcessingUrl("/login")
                        .defaultSuccessUrl("/") // 로그인 성공하면 원래 가려던 루트(/)로 이동
                ).logout(logout -> logout.permitAll());

        return http.build();
    }
}
