package kr.or.ddit.finalProject.mapper.exam;

import kr.or.ddit.finalProject.dto.exam.DifficultyStatsDto;
import kr.or.ddit.finalProject.dto.exam.ExamTrendDto;
import kr.or.ddit.finalProject.dto.exam.WeakPointDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WeakPointMapper {

    List<WeakPointDto> selectWeakPointsByClassSn(@Param("classSn") Long classSn);

    List<DifficultyStatsDto> selectDifficultyStatsByClassSn(@Param("classSn") Long classSn);

    List<ExamTrendDto> selectExamTrendByClassSn(@Param("classSn") Long classSn);
}
