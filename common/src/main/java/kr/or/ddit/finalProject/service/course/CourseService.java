package kr.or.ddit.finalProject.service.course;

import java.util.List;

import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.dto.course.AdminCourseSearchCondition;
import kr.or.ddit.finalProject.dto.course.CourseDetailResponse;
import kr.or.ddit.finalProject.dto.course.CourseDto;
import kr.or.ddit.finalProject.dto.course.CourseResponseDto;
import kr.or.ddit.finalProject.dto.instructor.profile.InstructorPublicCourseResponse;
import kr.or.ddit.finalProject.dto.member.MemberDto;
import kr.or.ddit.finalProject.dto.subject.SubjectClassificationDto;
import kr.or.ddit.finalProject.dto.subject.SubjectDto;
import kr.or.ddit.finalProject.paging.PaginationInfo;

public interface CourseService {

    /**
     * 커리큘럼 ID에 속한 강좌 목록을 반환한다.
     */
    List<CourseDto> retrieveCourseByCurriculumId(Long curriculumId);

    /**
     * 특정 강사가 담당하는 강좌 목록을 반환한다.
     */
    List<CourseDto> retrieveCoursesByInstructor(String instrUserId);

    /**
     * 강좌 일련번호로 강좌 정보를 단건 조회한다.
     */
    CourseDto retrieveCourseBySn(Long courseSn);

    /**
     * 관리자 상세 페이지용 강좌 정보를 조회한다 (추가 정보 포함).
     */
    CourseDto retrieveCourseAdminDetail(Long courseSn);

    /**
     * 강좌의 첨부파일 ID를 갱신한다.
     */
    void updateCourseAtchFileId(Long courseSn, String atchFileId);

    /**
     * 강좌를 등록한다.
     *
     * @return 등록 성공 여부
     */
    boolean createCourse(CourseDto courseDto);

    /**
     * 강좌 정보를 수정한다. currentUserId는 최종 수정자 ID로 기록된다.
     */
    void modifyCourse(CourseDto courseDto, String currentUserId);

    /**
     * 강좌를 삭제(소프트 딜리트)한다. currentUserId는 최종 수정자 ID로 기록된다.
     */
    void removeCourse(Long courseSn, String currentUserId);

    /**
     * 관리자 강좌 목록을 페이징·검색 조건에 따라 조회한다.
     */
    List<CourseDto> retrieveCourseList(PaginationInfo<AdminCourseSearchCondition> paginationInfo);

    /**
     * 관리자 강좌 목록의 전체 건수를 반환한다.
     */
    int retrieveCourseListCount(PaginationInfo<AdminCourseSearchCondition> paginationInfo);

    /**
     * 과목 분류 전체 목록을 반환한다.
     */
    List<SubjectClassificationDto> retrieveSubjectClassificationList();

    /**
     * 과목 분류 ID에 속한 과목 목록을 반환한다.
     */
    List<SubjectDto> retrieveSubjectsBySubjClId(Long subjClId);

    /**
     * 과목 분류 ID에 해당하는 강사 회원 목록을 반환한다.
     */
    List<MemberDto> retrieveInstructorsBySubjClId(Long subjClId);

    /**
     * 메인 페이지용 강좌 목록을 카테고리·키워드·페이지 조건으로 조회한다.
     */
    PageResponse<CourseResponseDto> retrieveCourseListForMain(String category, String keyword,
            int page);

    /**
     * 강좌 ID로 메인 페이지용 강좌 상세 정보를 조회한다.
     */
    CourseResponseDto retrieveCourse(Long courseId);

    /**
     * 강사 UUID로 공개 강좌 목록을 조회한다.
     */
    List<InstructorPublicCourseResponse> retrievePublicCoursesByInstructor(String instrUuid);

    /**
     * 강사 UUID와 강좌 일련번호로 공개 강좌 상세 정보(강의 목록 포함)를 조회한다.
     */
    CourseDetailResponse retrievePublicCourseDetail(String instrUuid, Long courseSn);
}
