package kr.or.ddit.finalProject.service.curriculum;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.ddit.finalProject.dto.course.CourseDto;
import kr.or.ddit.finalProject.dto.curriculum.CurriculumDto;
import kr.or.ddit.finalProject.mapper.course.CourseMapper;
import kr.or.ddit.finalProject.mapper.curriculum.CurriculumMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CurriculumServiceImpl implements CurriculumService {

    private final CurriculumMapper curriculumMapper;
    private final CourseMapper courseMapper;

    @Override
    public List<CurriculumDto> retrieveAllList() {
        return curriculumMapper.selectAllList();
    }

    @Override
    public List<CurriculumDto> retrieveList(String instructorId) {
        return curriculumMapper.selectList(instructorId);
    }

    @Override
    @Transactional
    public boolean createCurriculum(CurriculumDto curriculumDto) {
        return curriculumMapper.insert(curriculumDto) > 0;
    }

    @Override
    @Transactional
    public void modifyCurriculum(CurriculumDto curriculumDto, String currentUserId) {
        CurriculumDto original = curriculumMapper.selectById(curriculumDto.getCurriculumId());

        if (original == null || !"Y".equals(original.getUseYn())) {
            throw new IllegalArgumentException("수정하려는 커리큘럼이 존재하지 않습니다.");
        }

        if (!original.getInstructorId().equals(currentUserId)) {
            throw new SecurityException("본인이 작성한 커리큘럼만 수정할 수 있습니다.");
        }

        curriculumDto.setLastMdfrId(currentUserId);
        curriculumMapper.update(curriculumDto);
    }

    @Override
    @Transactional
    public void removeCurriculumLogically(Long curriculumId, String currentUserId) {
        CurriculumDto original = curriculumMapper.selectById(curriculumId);

        if (original == null || !"Y".equals(original.getUseYn())) {
            throw new IllegalArgumentException("삭제하려는 커리큘럼이 존재하지 않습니다.");
        }

        if (!original.getInstructorId().equals(currentUserId)) {
            throw new SecurityException("본인이 작성한 커리큘럼만 삭제할 수 있습니다.");
        }

        curriculumMapper.deleteLogically(curriculumId, currentUserId);
    }

    // ── 커리큘럼-강좌 매핑 ──────────────────────────────────────────

    @Override
    public List<CourseDto> retrieveAvailableCourses() {
        return curriculumMapper.selectAvailableCourses();
    }

    @Override
    @Transactional
    public void addCourseMapping(Long curriculumId, Long courseSn, String currentUserId) {
        CurriculumDto curriculum = curriculumMapper.selectById(curriculumId);
        if (curriculum == null || !"Y".equals(curriculum.getUseYn())) {
            throw new IllegalArgumentException("존재하지 않는 커리큘럼입니다.");
        }
        if (!curriculum.getInstructorId().equals(currentUserId)) {
            throw new SecurityException("본인이 작성한 커리큘럼에만 강좌를 추가할 수 있습니다.");
        }
        int nextSortOrd = curriculumMapper.selectMaxCourseSortOrd(curriculumId) + 1;
        curriculumMapper.mapCourseToCurriculum(courseSn, curriculumId, nextSortOrd);
    }

    @Override
    @Transactional
    public void removeCourseMapping(Long curriculumId, Long courseSn, String currentUserId) {
        CurriculumDto curriculum = curriculumMapper.selectById(curriculumId);
        if (curriculum == null || !"Y".equals(curriculum.getUseYn())) {
            throw new IllegalArgumentException("존재하지 않는 커리큘럼입니다.");
        }
        if (!curriculum.getInstructorId().equals(currentUserId)) {
            throw new SecurityException("본인이 작성한 커리큘럼에서만 강좌를 제거할 수 있습니다.");
        }
        CourseDto course = courseMapper.selectCourseBySn(courseSn);
        if (course == null) {
            throw new IllegalArgumentException("존재하지 않는 강좌입니다.");
        }
        Integer sortOrd = course.getSortOrd();
        curriculumMapper.unmapCourseFromCurriculum(courseSn);
        if (sortOrd != null && sortOrd > 0) {
            curriculumMapper.resequenceCoursesSortOrd(curriculumId, sortOrd);
        }
    }

    @Override
    @Transactional
    public void moveCourseUp(Long curriculumId, Long courseSn, String currentUserId) {
        verifyCurriculumOwner(curriculumId, currentUserId);
        List<CourseDto> courses = courseMapper.selectCourseByCurriculumId(curriculumId);
        for (int i = 1; i < courses.size(); i++) {
            if (courses.get(i).getCourseSn().equals(courseSn)) {
                swapSortOrd(courses.get(i), courses.get(i - 1));
                return;
            }
        }
    }

    @Override
    @Transactional
    public void moveCourseDown(Long curriculumId, Long courseSn, String currentUserId) {
        verifyCurriculumOwner(curriculumId, currentUserId);
        List<CourseDto> courses = courseMapper.selectCourseByCurriculumId(curriculumId);
        for (int i = 0; i < courses.size() - 1; i++) {
            if (courses.get(i).getCourseSn().equals(courseSn)) {
                swapSortOrd(courses.get(i), courses.get(i + 1));
                return;
            }
        }
    }

    private void verifyCurriculumOwner(Long curriculumId, String currentUserId) {
        CurriculumDto curriculum = curriculumMapper.selectById(curriculumId);
        if (curriculum == null || !"Y".equals(curriculum.getUseYn())) {
            throw new IllegalArgumentException("존재하지 않는 커리큘럼입니다.");
        }
        if (!curriculum.getInstructorId().equals(currentUserId)) {
            throw new SecurityException("본인이 작성한 커리큘럼의 강좌만 순서 변경이 가능합니다.");
        }
    }

    private void swapSortOrd(CourseDto a, CourseDto b) {
        int sortA = a.getSortOrd();
        int sortB = b.getSortOrd();
        curriculumMapper.updateCourseSortOrd(a.getCourseSn(), sortB);
        curriculumMapper.updateCourseSortOrd(b.getCourseSn(), sortA);
    }
}
