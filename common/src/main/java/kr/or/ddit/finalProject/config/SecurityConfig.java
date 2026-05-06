package kr.or.ddit.finalProject.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

public class SecurityConfig {

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http.cors(Customizer.withDefaults()) // CORS 설정을 기본값으로 적용
                                .csrf(csrf -> csrf.disable()) // CSRF 보호 비활성화 (API 서버에서는 일반적으로 비활성화)
                                // HTTP 보안 헤더 설정
                                .headers(headers -> headers.frameOptions(frame -> frame.deny())

                                                // X-Content-Type-Options 헤더 설정
                                                .contentTypeOptions(Customizer.withDefaults())

                                // HSTS 헤더 설정 - HTTPS를 사용하는 경우에만 적용
                                // .httpStrictTransportSecurity(
                                // hsts -> hsts.maxAgeInSeconds(31536000).includeSubDomains(true))

                                ).httpBasic(httpBasic -> httpBasic.disable()) // HTTP Basic 인증 비활성화 (토큰 기반 인증 사용 위함)

                                // 세션 관리 설정 - Stateless로 설정하여 서버가 세션을 유지하지 않도록 함
                                .sessionManagement(
                                                session -> session
                                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                                // 권한 설정 - 모든 GET 요청과 루트 경로는 인증 없이 허용
                                .authorizeHttpRequests(auth -> auth.requestMatchers(HttpMethod.GET, "/**")
                                                .permitAll().requestMatchers("/").permitAll()

                                ); // 모든 GET 요청 허용
                return http.build();
        }
}
