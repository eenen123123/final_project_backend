package kr.or.ddit.finalProject.service.course;

import java.util.List;

import kr.or.ddit.finalProject.dto.course.CourseDto;
import kr.or.ddit.finalProject.dto.course.CourseSearchCondition;
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

    List<CourseDto> retrieveCourseList(PaginationInfo<CourseSearchCondition> paginationInfo);

    int retrieveCourseListCount(PaginationInfo<CourseSearchCondition> paginationInfo);

    List<SubjectClassificationDto> retrieveSubjectClassificationList();

    List<SubjectDto> retrieveSubjectsBySubjClId(Long subjClId);

    List<MemberDto> retrieveInstructorsBySubjClId(Long subjClId);

}
