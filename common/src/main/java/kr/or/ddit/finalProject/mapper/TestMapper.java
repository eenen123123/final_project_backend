package kr.or.ddit.finalProject.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import kr.or.ddit.finalProject.dto.example.ExampleDto;
import kr.or.ddit.finalProject.paging.PaginationInfo;
import kr.or.ddit.finalProject.paging.temp.MemberDtoForPagingTest;

@Mapper
public interface TestMapper {

    /**
     * DB에서 날짜를 조회하는 예시 메서드
     * 
     * @return 조회된 날짜 정보를 담은 ExampleDto 객체
     */
    ExampleDto getDate();

    /**
     * 페이징 처리를 위한 테스트 메서드
     * 
     * @param paginationInfo 페이징 정보를 담고 있는 객체
     * @return 조회된 회원 정보를 담은 리스트
     */
    List<MemberDtoForPagingTest> selectMemberDtoForPagingTestList(
            PaginationInfo<MemberDtoForPagingTest> paginationInfo);

}
