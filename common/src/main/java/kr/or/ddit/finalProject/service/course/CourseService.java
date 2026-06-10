package kr.or.ddit.finalProject.service.course;

import java.util.List;

import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.dto.course.AdminCourseSearchCondition;
import kr.or.ddit.finalProject.dto.course.CourseDto;
import kr.or.ddit.finalProject.dto.course.CourseResponseDto;
import kr.or.ddit.finalProject.dto.course.SubjectClassificationDto;
import kr.or.ddit.finalProject.dto.course.SubjectDto;
import kr.or.ddit.finalProject.dto.member.MemberDto;
import kr.or.ddit.finalProject.paging.PaginationInfo;

public interface CourseService {

    List<CourseDto> retrieveCourseByCurriculumId(Long curriculumId);

    List<CourseDto> retrieveCoursesByInstructor(String instrUserId);

    CourseDto retrieveCourseBySn(Long courseSn);

    CourseDto retrieveCourseAdminDetail(Long courseSn);

    void updateCourseAtchFileId(Long courseSn, String atchFileId);

    boolean createCourse(CourseDto courseDto);

    void modifyCourse(CourseDto courseDto, String currentUserId);

    void removeCourse(Long courseSn, String currentUserId);

    List<CourseDto> retrieveCourseList(PaginationInfo<AdminCourseSearchCondition> paginationInfo);

    int retrieveCourseListCount(PaginationInfo<AdminCourseSearchCondition> paginationInfo);

    List<SubjectClassificationDto> retrieveSubjectClassificationList();

    List<SubjectDto> retrieveSubjectsBySubjClId(Long subjClId);

    List<MemberDto> retrieveInstructorsBySubjClId(Long subjClId);

    PageResponse<CourseResponseDto> retrieveCourseListForMain(String category, String keyword,
            int page);

    CourseResponseDto retrieveCourse(Long courseId);
}
