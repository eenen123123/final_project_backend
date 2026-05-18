package kr.or.ddit.config;

import org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties.Web;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class AdminSecurityConfig {

        @Bean
        public WebSecurityCustomizer webSecurityCustomizer() {
                // 정적 리소스에 대한 보안 설정을 무시하도록 구성
                return (web) -> web.ignoring().requestMatchers(
                                PathRequest.toStaticResources().atCommonLocations());
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http.csrf(AbstractHttpConfigurer::disable).authorizeHttpRequests(requests ->


                requests.requestMatchers("/", "/login").permitAll().requestMatchers("/admin/**")
                                .hasRole("ADMIN"))
                                .formLogin(auth -> auth.loginPage("/login")
                                                .loginProcessingUrl("/login")
                                                .defaultSuccessUrl("/"))
                                .logout(logout -> logout.permitAll());
                return http.build();

        }
}
