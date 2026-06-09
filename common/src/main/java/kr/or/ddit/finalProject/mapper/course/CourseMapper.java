package kr.or.ddit.finalProject.mapper.course;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.course.CourseDto;
import kr.or.ddit.finalProject.dto.course.CourseListDto;
import kr.or.ddit.finalProject.dto.course.SubjectClassificationDto;
import kr.or.ddit.finalProject.dto.instructor.CourseLectureItem;
import kr.or.ddit.finalProject.dto.instructor.CourseDetailResponse;
import kr.or.ddit.finalProject.dto.instructor.InstructorPublicCourseResponse;
import kr.or.ddit.finalProject.dto.member.MemberDto;
import kr.or.ddit.finalProject.paging.PaginationInfo;

@Mapper
public interface CourseMapper {

    List<CourseDto> selectCourseByCurriculumId(@Param("curriculumId") Long curriculumId);

    List<CourseDto> selectCoursesByInstructor(@Param("instrUserId") String instrUserId);

    CourseDto selectCourseBySn(@Param("courseSn") Long courseSn);

    int insertCourse(CourseDto courseDto);

    int updateCourse(CourseDto courseDto);

    int deleteCourse(@Param("courseSn") Long courseSn);

    int countLectureByCourse(@Param("courseSn") Long courseSn);

    int updateCourseAtchFileId(@Param("courseSn") Long courseSn,
            @Param("atchFileId") String atchFileId);

    // 전체 강좌 조회 (페이징 + 검색)
    List<CourseListDto> selectCourseList(PaginationInfo<CourseListDto> paginationInfo);

    int selectCourseListCount(PaginationInfo<CourseListDto> paginationInfo);

    // 과목 분류 목록 조회
    List<SubjectClassificationDto> selectSubjectClassificationList();

    List<MemberDto> selectInstructorsBySubjClId(@Param("subjClId") Long subjClId);

    List<InstructorPublicCourseResponse> selectCoursesByInstrUuid(@Param("instrUuid") String instrUuid);

    CourseDetailResponse selectCourseDetailByUuidAndSn(@Param("instrUuid") String instrUuid,
                                                       @Param("courseSn") Long courseSn);

    List<CourseLectureItem> selectPublicCourseLectures(@Param("courseSn") Long courseSn);

}
