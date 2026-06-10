package kr.or.ddit.finalProject.service.course;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.dto.course.CourseDto;
import kr.or.ddit.finalProject.dto.course.CourseListDto;
import kr.or.ddit.finalProject.dto.course.CourseResponseDto;
import kr.or.ddit.finalProject.dto.course.CourseSearchCondition;
import kr.or.ddit.finalProject.dto.course.SubjectClassificationDto;
import kr.or.ddit.finalProject.dto.course.SubjectDto;
import kr.or.ddit.finalProject.dto.member.MemberDto;
import kr.or.ddit.finalProject.mapper.course.CourseMapper;
import kr.or.ddit.finalProject.paging.PaginationInfo;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseServiceImpl implements CourseService {

    private final CourseMapper courseMapper;

    @Override
    public List<CourseDto> retrieveCourseByCurriculumId(Long curriculumId) {
        return courseMapper.selectCourseByCurriculumId(curriculumId);
    }

    @Override
    public List<CourseDto> retrieveCoursesByInstructor(String instrUserId) {
        return courseMapper.selectCoursesByInstructor(instrUserId);
    }

    @Override
    public CourseDto retrieveCourseBySn(Long courseSn) {
        return courseMapper.selectCourseBySn(courseSn);
    }

    @Override
    @Transactional
    public void updateCourseAtchFileId(Long courseSn, String atchFileId) {
        courseMapper.updateCourseAtchFileId(courseSn, atchFileId);
    }

    @Override
    @Transactional
    public boolean createCourse(CourseDto courseDto) {
        return courseMapper.insertCourse(courseDto) > 0;
    }

    @Override
    @Transactional
    public void modifyCourse(CourseDto courseDto, String currentUserId) {
        CourseDto original = courseMapper.selectCourseBySn(courseDto.getCourseSn());
        if (original == null) {
            throw new IllegalArgumentException("존재하지 않는 강좌입니다.");
        }
        if (!currentUserId.equals(original.getInstrUserId())) {
            throw new SecurityException("본인이 작성한 강좌만 수정할 수 있습니다.");
        }
        courseDto.setLastMdfrId(currentUserId);
        courseMapper.updateCourse(courseDto);
    }

    @Override
    @Transactional
    public void removeCourse(Long courseSn, String currentUserId) {
        CourseDto original = courseMapper.selectCourseBySn(courseSn);
        if (original == null) {
            throw new IllegalArgumentException("존재하지 않는 강좌입니다.");
        }
        if (!currentUserId.equals(original.getInstrUserId())) {
            throw new SecurityException("본인이 작성한 강좌만 삭제할 수 있습니다.");
        }
        int lectureCount = courseMapper.countLectureByCourse(courseSn);
        if (lectureCount > 0) {
            throw new IllegalArgumentException("강의가 존재하는 강좌는 삭제할 수 없습니다. 강의를 먼저 삭제해 주세요.");
        }
        courseMapper.deleteCourse(courseSn);
    }

    // 전체 강좌 조회 (페이징 + 검색)
    @Override
    public List<CourseListDto> retrieveCourseList(PaginationInfo<CourseListDto> paginationInfo) {
        return courseMapper.selectCourseList(paginationInfo);
    }

    @Override
    public int retrieveCourseListCount(PaginationInfo<CourseListDto> paginationInfo) {
        return courseMapper.selectCourseListCount(paginationInfo);
    }

    // 과목 분류 목록 조회
    @Override
    public List<SubjectClassificationDto> retrieveSubjectClassificationList() {
        return courseMapper.selectSubjectClassificationList();
    }

    // 대분류별 소분류 목록 조회
    @Override
    public List<SubjectDto> retrieveSubjectsBySubjClId(Long subjClId) {
        return courseMapper.selectSubjectsBySubjClId(subjClId);
    }

    // 과목 분류별 강사 조회
    @Override
    public List<MemberDto> retrieveInstructorsBySubjClId(Long subjClId) {
        return courseMapper.selectInstructorsBySubjClId(subjClId);
    }

    @Override
    public PageResponse<CourseResponseDto> retrieveCourseListForMain(String category,
            String keyword, int page) {

        PaginationInfo<CourseSearchCondition> paginationInfo = new PaginationInfo<>(10, page);
        CourseSearchCondition searchCondition = new CourseSearchCondition();
        switch (category) {
            case "instructor":
                searchCondition.setInstructorName(keyword);
                break;
            case "subject":
                searchCondition.setSubjectName(keyword);
                break;
            case "courseName":
                searchCondition.setCourseName(keyword);
                break;
            default:
                break;
        }
        paginationInfo.setDetailCondition(searchCondition);

        int totalCount = courseMapper.selectCourseListCountForMain(paginationInfo);
        List<CourseResponseDto> items = courseMapper.selectCourseListForMain(paginationInfo);
        return new PageResponse<>(items, totalCount);



    }

    @Override
    public CourseResponseDto retrieveCourse(Long courseId) {
        return courseMapper.selectCourseById(courseId);
    }



}
