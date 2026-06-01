package kr.or.ddit.finalProject.mapper.lecture;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.lecture.LectureDto;

@Mapper
public interface LectureMapper {

    List<LectureDto> selectLectureByCourseSn(@Param("courseSn") Long courseSn);

    LectureDto selectLectureBySn(@Param("lectureSn") Long lectureSn);

    int insertLecture(LectureDto lectureDto);

    int updateLecture(LectureDto lectureDto);

    int deleteLecture(@Param("lectureSn") Long lectureSn);

}
