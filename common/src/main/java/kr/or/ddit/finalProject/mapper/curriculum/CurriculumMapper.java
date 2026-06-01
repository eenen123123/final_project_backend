package kr.or.ddit.finalProject.mapper.curriculum;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.curriculum.CurriculumDto;

@Mapper
public interface CurriculumMapper {

    List<CurriculumDto> selectList(@Param("instructorId") String instructorId);

    CurriculumDto selectById(@Param("curriculumId") Long curriculumId);

    int insert(CurriculumDto curriculumDto);

    int update(CurriculumDto curriculumDto);

    int deleteLogically(@Param("curriculumId") Long curriculumId,
            @Param("lastMdfrId") String lastMdfrId);
}
