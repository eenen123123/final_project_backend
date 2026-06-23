package kr.or.ddit.mapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.instructor.InstructorQnaStatsDto;
import kr.or.ddit.finalProject.dto.instructor.OverdueQnaDto;

@Mapper
public interface QualityMapper {

    List<InstructorQnaStatsDto> selectInstructorQnaStats(
            @Param("startDate") LocalDate startDate,
            @Param("endDate")   LocalDate endDate);

    Map<String, Object> selectQnaSummary(
            @Param("startDate") LocalDate startDate,
            @Param("endDate")   LocalDate endDate);

    List<OverdueQnaDto> selectOverdueQnaList();
}
