package kr.or.ddit.instructor.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.dto.instructor.CurriculumDetailDto;
import kr.or.ddit.dto.instructor.CurriculumMasterDto;

@Mapper
public interface InstructorCurriculumMapper {

    List<CurriculumMasterDto> selectMasterList(@Param("instructorId") String instructorId);

    CurriculumMasterDto selectMasterById(@Param("curriculumId") Long curriculumId);

    List<CurriculumDetailDto> selectDetailList(@Param("curriculumId") Long curriculumId);

    int insertMaster(CurriculumMasterDto masterDto);

    int insertDetail(@Param("curriculumId") Long curriculumId,
            @Param("rowOrder") int rowOrder,
            @Param("weekInfo") String weekInfo,
            @Param("topic") String topic,
            @Param("content") String content,
            @Param("rgtrId") String rgtrId);

    int updateMaster(CurriculumMasterDto masterDto);

    int deleteMasterLogically(@Param("curriculumId") Long curriculumId,
            @Param("lastMdfrId") String lastMdfrId);

    int deleteDetailsByMasterId(@Param("curriculumId") Long curriculumId);
}
