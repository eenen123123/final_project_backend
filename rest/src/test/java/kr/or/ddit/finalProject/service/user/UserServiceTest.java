package kr.or.ddit.finalProject.service.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
public class UserServiceTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void encTest() {
        String rawPassword = "java";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        log.info("원본 비밀번호: {}", rawPassword);
        log.info("인코딩된 비밀번호: {}", encodedPassword);
    }

    @Test
    void verifyTest() {
        String rawPassword = "java";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);
        log.info("비밀번호 일치 여부: {}", matches);
    }

}
