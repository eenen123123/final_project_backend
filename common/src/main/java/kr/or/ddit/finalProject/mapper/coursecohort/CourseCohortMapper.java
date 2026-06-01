package kr.or.ddit.finalProject.mapper.coursecohort;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.coursecohort.CourseCohortListResponse;

@Mapper
public interface CourseCohortMapper {

    List<CourseCohortListResponse> selectCohortsByClassSn(@Param("classSn") Long classSn);

}
