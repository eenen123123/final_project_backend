package kr.or.ddit.finalProject.mapper;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import kr.or.ddit.finalProject.paging.PaginationInfo;
import kr.or.ddit.finalProject.paging.temp.MemberDtoForPagingTest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
public class TestMapperTest {

    @Autowired
    TestMapper testMapper;

    @Test
    void getDateTimeTest() {
        var result = testMapper.getDate();

        assertNotNull(result);

        log.info("조회된 날짜 정보: {}", result);

    }

    /**
     * 페이징 정보를 포함하여 회원 정보를 조회하는 테스트
     */
    @Test
    void selectMemberDtoForPagingTestListTest() {
        var paginationInfo = new PaginationInfo<MemberDtoForPagingTest>(10, 5, 1);
        var result = testMapper.selectMemberDtoForPagingTestList(paginationInfo);
        assertNotNull(result);
        log.info("조회된 회원 정보 리스트 크기: {}", result.size());
        result.forEach(member -> log.info("회원명: {}", member.getMemName()));
    }

    /**
     * 검색 조건과 페이징을 포함하여 회원 정보를 조회하는 테스트
     */
    @Test
    void selectMemberDtoForPagingTestListWithSearchingTest() {
        var memberDtoForPagingTest = new MemberDtoForPagingTest();
        memberDtoForPagingTest.setMemAdd1("대전");
        // memberDtoForPagingTest.setMemName("김");

        var paginationInfo = new PaginationInfo<MemberDtoForPagingTest>(10, 5, 1);
        paginationInfo.setDetailCondition(memberDtoForPagingTest);

        var result = testMapper.selectMemberDtoForPagingTestList(paginationInfo);
        assertNotNull(result);
        log.info("조회된 회원 정보 리스트 크기: {}", result.size());
        result.forEach(member -> log.info("회원명: {}, 주소: {}", member.getMemName(), member.getMemAdd1()));
    }

    /**
     * 정렬 기준과 방향, 검색 조건을 포함하여 회원 정보를 조회하는 테스트
     */
    @Test
    void selectMemberDtoForPagingTestListWithSearchingAndOrderingTest() {
        var memberDtoForPagingTest = new MemberDtoForPagingTest();
        memberDtoForPagingTest.setMemAdd1("대전");
        memberDtoForPagingTest.setMemName("김");

        var paginationInfo = new PaginationInfo<MemberDtoForPagingTest>(10, 5, 1, "mem_id", "asc");
        paginationInfo.setDetailCondition(memberDtoForPagingTest);

        var result = testMapper.selectMemberDtoForPagingTestList(paginationInfo);
        assertNotNull(result);
        log.info("조회된 회원 정보 리스트 크기: {}", result.size());
        result.forEach(member -> log.info("회원 ID: {}, 회원명: {}, 주소: {}", member.getMemId(), member.getMemName(),
                member.getMemAdd1()));
    }
}
