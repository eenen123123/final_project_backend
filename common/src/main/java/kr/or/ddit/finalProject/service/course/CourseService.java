package kr.or.ddit.finalProject.service.course;

import java.util.List;

import kr.or.ddit.finalProject.dto.course.CourseDto;
import kr.or.ddit.finalProject.dto.course.CourseListDto;
import kr.or.ddit.finalProject.dto.course.SubjectClassificationDto;
import kr.or.ddit.finalProject.dto.course.SubjectDto;
import kr.or.ddit.finalProject.dto.member.MemberDto;
import kr.or.ddit.finalProject.paging.PaginationInfo;

public interface CourseService {

    List<CourseDto> retrieveCourseByCurriculumId(Long curriculumId);

    List<CourseDto> retrieveCoursesByInstructor(String instrUserId);

    CourseDto retrieveCourseBySn(Long courseSn);

    void updateCourseAtchFileId(Long courseSn, String atchFileId);

    boolean createCourse(CourseDto courseDto);

    void modifyCourse(CourseDto courseDto, String currentUserId);

    void removeCourse(Long courseSn, String currentUserId);

    // 전체 강좌 조회 (페이징 + 검색)
    List<CourseListDto> retrieveCourseList(PaginationInfo<CourseListDto> paginationInfo);

    int retrieveCourseListCount(PaginationInfo<CourseListDto> paginationInfo);

    // 과목 분류 목록 조회 (전체강좌 필터링용)
    List<SubjectClassificationDto> retrieveSubjectClassificationList();

    // 대분류별 소분류 목록 조회
    List<SubjectDto> retrieveSubjectsBySubjClId(Long subjClId);

    // 과목 분류별 강사 목록 조회
    List<MemberDto> retrieveInstructorsBySubjClId(Long subjClId);

}
