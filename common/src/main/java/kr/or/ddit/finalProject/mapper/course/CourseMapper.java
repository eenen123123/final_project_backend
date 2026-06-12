package kr.or.ddit.finalProject.mapper.course;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.course.AdminCourseSearchCondition;
import kr.or.ddit.finalProject.dto.course.CourseDetailResponse;
import kr.or.ddit.finalProject.dto.course.CourseDto;
import kr.or.ddit.finalProject.dto.course.CourseLectureItem;
import kr.or.ddit.finalProject.dto.course.CourseResponseDto;
import kr.or.ddit.finalProject.dto.course.CourseSearchCondition;
import kr.or.ddit.finalProject.dto.instructor.profile.InstructorPublicCourseResponse;
import kr.or.ddit.finalProject.dto.member.MemberDto;
import kr.or.ddit.finalProject.dto.subject.SubjectClassificationDto;
import kr.or.ddit.finalProject.dto.subject.SubjectDto;
import kr.or.ddit.finalProject.paging.PaginationInfo;

@Mapper
public interface CourseMapper {

    /**
     * 커리큘럼 ID에 속한 강좌 목록을 조회한다.
     */
    List<CourseDto> selectCourseByCurriculumId(@Param("curriculumId") Long curriculumId);

    /**
     * 강사 ID가 담당하는 강좌 목록을 조회한다.
     */
    List<CourseDto> selectCoursesByInstructor(@Param("instrUserId") String instrUserId);

    /**
     * 강좌 일련번호로 강좌 기본 정보를 단건 조회한다.
     */
    CourseDto selectCourseBySn(@Param("courseSn") Long courseSn);

    /**
     * 관리자 상세 페이지용 강좌 정보를 조회한다 (강사·과목 등 JOIN 포함).
     */
    CourseDto selectCourseAdminDetail(@Param("courseSn") Long courseSn);

    /**
     * 강좌를 등록한다.
     */
    int insertCourse(CourseDto courseDto);

    /**
     * 강좌 정보를 수정한다.
     */
    int updateCourse(CourseDto courseDto);

    /**
     * 강좌를 삭제(소프트 딜리트)한다.
     */
    int deleteCourse(@Param("courseSn") Long courseSn);

    /**
     * 해당 강좌에 등록된 강의(LECTURE) 수를 반환한다. 강좌 삭제 가능 여부 판단에 사용된다.
     */
    int countLectureByCourse(@Param("courseSn") Long courseSn);

    /**
     * 강좌의 첨부파일 ID를 갱신한다.
     */
    int updateCourseAtchFileId(@Param("courseSn") Long courseSn,
            @Param("atchFileId") String atchFileId);

    /**
     * 관리자 강좌 목록을 페이징·검색 조건에 따라 조회한다.
     */
    List<CourseDto> selectCourseList(PaginationInfo<AdminCourseSearchCondition> paginationInfo);

    /**
     * 관리자 강좌 목록의 전체 건수를 반환한다.
     */
    int selectCourseListCount(PaginationInfo<AdminCourseSearchCondition> paginationInfo);

    /**
     * 과목 분류 전체 목록을 조회한다.
     */
    List<SubjectClassificationDto> selectSubjectClassificationList();

    /**
     * 과목 분류 ID에 속한 과목 목록을 조회한다.
     */
    List<SubjectDto> selectSubjectsBySubjClId(@Param("subjClId") Long subjClId);

    /**
     * 과목 분류 ID에 해당하는 강사 회원 목록을 조회한다.
     */
    List<MemberDto> selectInstructorsBySubjClId(@Param("subjClId") Long subjClId);

    /**
     * 강사 UUID로 해당 강사의 공개 강좌 목록을 조회한다.
     */
    List<InstructorPublicCourseResponse> selectCoursesByInstrUuid(
            @Param("instrUuid") String instrUuid);

    /**
     * 강사 UUID와 강좌 일련번호로 강좌 상세 정보를 조회한다.
     */
    CourseDetailResponse selectCourseDetailByUuidAndSn(@Param("instrUuid") String instrUuid,
            @Param("courseSn") Long courseSn);

    /**
     * 강좌의 공개 강의 목록을 조회한다.
     */
    List<CourseLectureItem> selectPublicCourseLectures(@Param("courseSn") Long courseSn);

    /**
     * 메인 페이지용 강좌 목록을 카테고리·키워드·페이지 조건으로 조회한다.
     */
    List<CourseResponseDto> selectCourseListForMain(
            @Param("paginationInfo") PaginationInfo<CourseSearchCondition> paginationInfo);

    /**
     * 메인 페이지용 강좌 목록의 전체 건수를 반환한다.
     */
    int selectCourseListCountForMain(
            @Param("paginationInfo") PaginationInfo<CourseSearchCondition> paginationInfo);

    /**
     * 강좌 ID로 메인 페이지용 강좌 상세 정보를 조회한다.
     */
    CourseResponseDto selectCourseById(@Param("courseId") Long courseId);
}
