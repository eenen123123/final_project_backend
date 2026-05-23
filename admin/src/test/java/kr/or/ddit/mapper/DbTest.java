package kr.or.ddit.mapper;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import kr.or.ddit.finalProject.dto.member.MemberDto;
import kr.or.ddit.finalProject.mapper.MemberMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
public class DbTest {

    @Autowired
    MemberMapper memberMapper;

    @Test
    void readAllUsersTest() {
        List<MemberDto> members = memberMapper.findAllMembers();
        log.info("회원 목록: {}", members);
    }

}
