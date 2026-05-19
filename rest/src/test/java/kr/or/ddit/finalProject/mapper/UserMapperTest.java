package kr.or.ddit.finalProject.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import kr.or.ddit.finalProject.dto.user.MemberDto;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
public class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void findByUserIdTest() {
        String userId = "testuser01";

        MemberDto memberDto = userMapper.findByUserId(userId).orElse(null);
        log.info("조회된 회원 정보: {}", memberDto);
    }

    @Test
    void findByUserIdWithRoleTest() {
        String userId = "testuser01";

        MemberDto memberDto = userMapper.findByUserId(userId).orElse(null);
        log.info("조회된 회원 정보: {}", memberDto);
        log.info("회원의 권한 정보: {}", memberDto.getUserRole());
    }

    @Test
    void insertUserTest() {
        MemberDto newUser = MemberDto.builder().userId("newuser01")
                .userEnpswd(passwordEncoder.encode("java")).userNm("사용자1").build();

        int result = userMapper.insertUser(newUser);
        log.info("Inserted user result: {}", result);


    }
}
