package kr.or.ddit.finalProject.mapper.learning;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.learning.LearningOverviewDto;

@Mapper
public interface LearningOverviewMapper {

    List<LearningOverviewDto> selectOverviewByInstructor(@Param("instrUserId") String instrUserId);
}
