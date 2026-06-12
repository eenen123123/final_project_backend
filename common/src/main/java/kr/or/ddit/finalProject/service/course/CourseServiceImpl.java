package kr.or.ddit.finalProject.service.course;

import java.util.List;
import java.util.Objects;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.dto.course.AdminCourseSearchCondition;
import kr.or.ddit.finalProject.dto.course.CourseDetailResponse;
import kr.or.ddit.finalProject.dto.course.CourseDto;
import kr.or.ddit.finalProject.dto.course.CourseResponseDto;
import kr.or.ddit.finalProject.dto.course.CourseSearchCondition;
import kr.or.ddit.finalProject.dto.instructor.profile.InstructorPublicCourseResponse;
import kr.or.ddit.finalProject.dto.member.MemberDto;
import kr.or.ddit.finalProject.dto.subject.SubjectClassificationDto;
import kr.or.ddit.finalProject.dto.subject.SubjectDto;
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
    public CourseDto retrieveCourseAdminDetail(Long courseSn) {
        return courseMapper.selectCourseAdminDetail(courseSn);
    }

    @Override
    @Transactional
    public void updateCourseAtchFileId(Long courseSn, String atchFileId) {
        courseMapper.updateCourseAtchFileId(courseSn, atchFileId);
    }

    @Override
    @Transactional
    public boolean createCourse(CourseDto courseDto, String currentUserId) {
        courseDto.setInstrUserId(currentUserId);
        courseDto.setRgtrId(currentUserId);
        courseDto.setLastMdfrId(currentUserId);
        assignNextSortOrd(courseDto);
        try {
            return courseMapper.insertCourse(courseDto) > 0;
        } catch (DuplicateKeyException e) {
            // Oracle은 constraint 위반 시 statement만 롤백하고 트랜잭션을 유지하므로 재시도 가능.
            // 다른 DB(PostgreSQL 등)에서는 트랜잭션 자체가 aborted되어 재시도가 실패함.
            assignNextSortOrd(courseDto);
            return courseMapper.insertCourse(courseDto) > 0;
        }
    }

    @Override
    @Transactional
    public void modifyCourse(CourseDto courseDto, String currentUserId) {
        CourseDto original = courseMapper.selectCourseBySn(courseDto.getCourseSn());
        if (original == null) {
            throw new IllegalArgumentException("존재하지 않는 강좌입니다.");
        }
        // TODO: 강좌 관리를 강사 전용 페이지로 이관 시 소유권 체크 활성화
        // if (!currentUserId.equals(original.getInstrUserId())) throw new SecurityException(...)

        Long oldCurriculumId = original.getCurriculumId();
        Long newCurriculumId = courseDto.getCurriculumId();

        courseDto.setLastMdfrId(currentUserId);

        if (!Objects.equals(oldCurriculumId, newCurriculumId)) {
            Integer oldSortOrd = original.getSortOrd();
            if (newCurriculumId != null) {
                courseDto.setSortOrd(courseMapper.selectMaxSortOrdByCurriculumId(newCurriculumId) + 1);
            } else {
                courseDto.setSortOrd(null);
            }
            try {
                courseMapper.updateCourse(courseDto);
            } catch (DuplicateKeyException e) {
                // Oracle statement-level rollback 특성을 이용한 재시도 (createCourse 참고).
                courseDto.setSortOrd(courseMapper.selectMaxSortOrdByCurriculumId(newCurriculumId) + 1);
                courseMapper.updateCourse(courseDto);
            }
            if (oldCurriculumId != null && oldSortOrd != null && oldSortOrd > 0) {
                courseMapper.resequenceSortOrd(oldCurriculumId, oldSortOrd);
            }
        } else {
            courseDto.setSortOrd(original.getSortOrd());
            courseMapper.updateCourse(courseDto);
        }
    }

    private void assignNextSortOrd(CourseDto courseDto) {
        if (courseDto.getCurriculumId() != null) {
            courseDto.setSortOrd(courseMapper.selectMaxSortOrdByCurriculumId(courseDto.getCurriculumId()) + 1);
        }
    }

    @Override
    @Transactional
    public void removeCourse(Long courseSn, String currentUserId) {
        CourseDto original = courseMapper.selectCourseBySn(courseSn);
        if (original == null) {
            throw new IllegalArgumentException("존재하지 않는 강좌입니다.");
        }
        // TODO: 강좌 관리를 강사 전용 페이지로 이관 시 소유권 체크 추가
        // if (!currentUserId.equals(original.getInstrUserId())) throw new SecurityException(...)
        int lectureCount = courseMapper.countLectureByCourse(courseSn);
        if (lectureCount > 0) {
            throw new IllegalArgumentException("강의가 존재하는 강좌는 삭제할 수 없습니다. 강의를 먼저 삭제해 주세요.");
        }
        courseMapper.deleteCourse(courseSn);
        Long curriculumId = original.getCurriculumId();
        Integer sortOrd = original.getSortOrd();
        if (curriculumId != null && sortOrd != null && sortOrd > 0) {
            courseMapper.resequenceSortOrd(curriculumId, sortOrd);
        }
    }

    @Override
    public List<CourseDto> retrieveCourseList(PaginationInfo<AdminCourseSearchCondition> paginationInfo) {
        return courseMapper.selectCourseList(paginationInfo);
    }

    @Override
    public int retrieveCourseListCount(PaginationInfo<AdminCourseSearchCondition> paginationInfo) {
        return courseMapper.selectCourseListCount(paginationInfo);
    }

    @Override
    public List<SubjectClassificationDto> retrieveSubjectClassificationList() {
        return courseMapper.selectSubjectClassificationList();
    }

    @Override
    public List<SubjectDto> retrieveSubjectsBySubjClId(Long subjClId) {
        return courseMapper.selectSubjectsBySubjClId(subjClId);
    }

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
            case "instructor" -> searchCondition.setInstructorName(keyword);
            case "subject"    -> searchCondition.setSubjectName(keyword);
            case "courseName" -> searchCondition.setCourseName(keyword);
            default           -> {}
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

    @Override
    public List<InstructorPublicCourseResponse> retrievePublicCoursesByInstructor(String instrUuid) {
        return courseMapper.selectCoursesByInstrUuid(instrUuid);
    }

    @Override
    public CourseDetailResponse retrievePublicCourseDetail(String instrUuid, Long courseSn) {
        CourseDetailResponse detail = courseMapper.selectCourseDetailByUuidAndSn(instrUuid, courseSn);
        if (detail != null) {
            detail.setLectures(courseMapper.selectPublicCourseLectures(courseSn));
        }
        return detail;
    }

}
