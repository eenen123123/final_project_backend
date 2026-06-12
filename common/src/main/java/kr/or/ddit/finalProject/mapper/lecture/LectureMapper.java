package kr.or.ddit.finalProject.mapper.lecture;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.lecture.LectureDto;
import kr.or.ddit.finalProject.dto.lecture.LectureProgressDto;
import kr.or.ddit.finalProject.dto.lecture.LectureResponseDto;

@Mapper
public interface LectureMapper {

    List<LectureProgressDto> selectLecturesWithProgress(@Param("classSn") Long classSn);

    List<LectureDto> selectLectureByCourseSn(@Param("courseSn") Long courseSn);

    LectureDto selectLectureBySn(@Param("lectureSn") Long lectureSn);

    int insertLecture(LectureDto lectureDto);

    int updateLecture(LectureDto lectureDto);

    int deleteLecture(@Param("lectureSn") Long lectureSn);

    List<LectureResponseDto> selectLectureListByCourseSn(@Param("courseSn") Long courseSn, @Param("userId") String userId);

    void updateLectureProgress(@Param("lectureId") Long lectureId, @Param("courseId") Long courseId,
            @Param("progress") Integer progress, @Param("userId") String userId);
}
