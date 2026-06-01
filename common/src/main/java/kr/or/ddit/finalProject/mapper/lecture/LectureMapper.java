package kr.or.ddit.finalProject.mapper.lecture;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.lecture.LectureListResponse;

@Mapper
public interface LectureMapper {

    List<LectureListResponse> selectLecturesByClassSn(@Param("classSn") Long classSn);

}
