package kr.or.ddit.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http.cors(Customizer.withDefaults()).csrf(csrf -> csrf.disable())
                                .headers(headers -> headers.frameOptions(frame -> frame.deny())
                                                .contentTypeOptions(Customizer.withDefaults())
                                                .httpStrictTransportSecurity(hsts -> hsts
                                                                .maxAgeInSeconds(31536000)))
                                .httpBasic(httpBasic -> httpBasic.disable())
                                .sessionManagement(session -> session.sessionCreationPolicy(
                                                SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(HttpMethod.GET, "/**").permitAll()
                                                .requestMatchers(HttpMethod.POST, "/api/test/**").permitAll()
                                                .requestMatchers("/").permitAll()
                                                .anyRequest().authenticated());
                return http.build();
        }
}
