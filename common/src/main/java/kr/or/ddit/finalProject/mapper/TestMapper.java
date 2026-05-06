package kr.or.ddit.finalProject.mapper;

import org.apache.ibatis.annotations.Mapper;
import kr.or.ddit.finalProject.dto.example.ExampleDto;

@Mapper
public interface TestMapper {

    /**
     * DB에서 날짜를 조회하는 예시 메서드
     * 
     * @return 조회된 날짜 정보를 담은 ExampleDto 객체
     */
    ExampleDto getDate();
}
