package kr.or.ddit.finalProject.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
public class MemberMapperTest {

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    MemberMapper memberMapper;

    // @Test
    // void insertMemberTest() {
    //     MemberDto newMember = MemberDto.builder().userId("testuser01")
    //             .userEnpswd(passwordEncoder.encode("java")).userName("새로운 회원").build();
    //     memberMapper.insertMember(newMember);
    // }
}
