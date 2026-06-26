package kr.or.ddit.finalProject.mapper.lecture;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import kr.or.ddit.finalProject.dto.course.CourseDto;
import kr.or.ddit.finalProject.dto.lecture.LectureDto;
import kr.or.ddit.finalProject.dto.lecture.ClassroomLectureResponse;
import kr.or.ddit.finalProject.dto.lecture.LectureResponseDto;
import kr.or.ddit.finalProject.dto.lecture.LectureProgressDetailResponse;
import kr.or.ddit.finalProject.dto.lecture.StudentLectureProgressResponse;

@Mapper
public interface LectureMapper {

    List<ClassroomLectureResponse> selectLecturesWithProgress(@Param("classSn") Long classSn);

    List<LectureDto> selectLectureByCourseSn(@Param("courseSn") Long courseSn);

    LectureDto selectLectureBySn(@Param("lectureSn") Long lectureSn);

    int insertLecture(LectureDto lectureDto);

    int updateLecture(LectureDto lectureDto);

    int deleteLecture(@Param("lectureSn") Long lectureSn);

    List<LectureResponseDto> selectLectureListByCourseSn(@Param("courseSn") Long courseSn,
            @Param("userId") String userId);

    void updateLectureProgress(@Param("lectureId") Long lectureId, @Param("courseId") Long courseId,
            @Param("progress") Integer progress, @Param("userId") String userId);

    CourseDto findCourseIdByFileServerId(@Param("fileServerId") long fileServerId);

    /** 특정 강의를 수강한 전체 수강생의 완료 여부 조회 (강의 상세 페이지용) */
    List<StudentLectureProgressResponse> selectStudentProgressByLecture(
            @Param("classSn") Long classSn,
            @Param("lectureSn") Long lectureSn);

    /** 특정 수강생의 클래스 내 전체 공개 강의별 완료 여부 조회 (수강생 진도 상세 페이지용) */
    List<LectureProgressDetailResponse> selectLectureProgressByStudent(
            @Param("classSn") Long classSn,
            @Param("userId") String userId);

    int updateOpnnYn(@Param("lectureSn") Long lectureSn,
                     @Param("opnnYn") String opnnYn,
                     @Param("userId") String userId);

    // 과목 분류별 시청 시간 집계 (수강 리포트 레이더 차트용)
    List<java.util.Map<String, Object>> selectSubjectProgress(@Param("userId") String userId);

    // 강사별 시청 시간 집계 (수강 리포트 선생님 집중도 랭킹용)
    List<java.util.Map<String, Object>> selectInstructorRanking(@Param("userId") String userId);

    int updateLockYn(@Param("lectureSn") Long lectureSn,
                     @Param("lockYn") String lockYn,
                     @Param("userId") String userId);
}
