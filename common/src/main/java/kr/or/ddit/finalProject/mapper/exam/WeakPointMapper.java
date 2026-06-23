package kr.or.ddit.finalProject.mapper.exam;

import kr.or.ddit.finalProject.dto.exam.WeakPointDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WeakPointMapper {

    /**
     * 클래스 수강생의 과목별 평균 득점률 조회 (낮은 순 정렬)
     * 제출 수 5건 미만 과목은 제외합니다.
     */
    List<WeakPointDto> selectWeakPointsByClassSn(@Param("classSn") Long classSn);
}
