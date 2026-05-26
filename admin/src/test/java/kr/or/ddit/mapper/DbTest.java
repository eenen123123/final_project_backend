package kr.or.ddit.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

    // 회원 목록 조회 테스트
    @Test
    void readAllUsersTest() {
        List<MemberDto> members = memberMapper.findAllMembers();
        log.info("회원 목록: {}", members);
    }

    // 회원 존재 여부 테스트
    @Test
    void existUsersTest() {
        List<MemberDto> members = memberMapper.findAllMembers();
        log.info("회원 목록: {}", members);
        List<String> userIds = members.stream().map(MemberDto::getUserId).toList();
        int existingCount = memberMapper.isAllExistUsers(userIds);
        log.info("존재하는 사용자 수: {}", existingCount);

        assertEquals(userIds.size(), existingCount);
    }

}
