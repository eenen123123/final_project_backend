package kr.or.ddit.finalProject.mapper.curriculum;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.curriculum.CurriculumMasterDto;

@Mapper
public interface CurriculumMapper {

    List<CurriculumMasterDto> selectMasterList(@Param("instructorId") String instructorId);

    CurriculumMasterDto selectMasterById(@Param("curriculumId") Long curriculumId);

    int insertMaster(CurriculumMasterDto masterDto);

    int updateMaster(CurriculumMasterDto masterDto);

    int deleteMasterLogically(@Param("curriculumId") Long curriculumId,
            @Param("lastMdfrId") String lastMdfrId);
}
