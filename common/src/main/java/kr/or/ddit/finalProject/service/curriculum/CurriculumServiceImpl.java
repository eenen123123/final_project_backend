package kr.or.ddit.finalProject.service.curriculum;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.ddit.finalProject.dto.course.CourseDto;
import kr.or.ddit.finalProject.dto.curriculum.CurriculumDto;
import kr.or.ddit.finalProject.mapper.curriculum.CurriculumMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CurriculumServiceImpl implements CurriculumService {

    private final CurriculumMapper curriculumMapper;

    /** USE_YN = 'Y'인 전체 커리큘럼 목록 반환 */
    @Override
    public List<CurriculumDto> retrieveAllList() {
        return curriculumMapper.selectAllList();
    }

    /** 특정 강사의 커리큘럼 목록 반환 */
    @Override
    public List<CurriculumDto> retrieveList(String instructorId) {
        return curriculumMapper.selectList(instructorId);
    }

    /** curriculumId로 커리큘럼 단건 조회 */
    @Override
    public CurriculumDto retrieveById(Long curriculumId) {
        return curriculumMapper.selectById(curriculumId);
    }

    /** 커리큘럼 생성. SEQ_CURRICULUM.NEXTVAL로 PK를 채운 뒤 INSERT한다. */
    @Override
    @Transactional
    public boolean createCurriculum(CurriculumDto curriculumDto) {
        return curriculumMapper.insert(curriculumDto) > 0;
    }

    /** 커리큘럼 수정. 존재 여부 및 작성자 일치 여부를 먼저 검증한다. */
    @Override
    @Transactional
    public void modifyCurriculum(CurriculumDto curriculumDto, String currentUserId) {
        CurriculumDto original = curriculumMapper.selectById(curriculumDto.getCurriculumId());
        if (original == null) {
            throw new IllegalArgumentException("수정하려는 커리큘럼이 존재하지 않습니다.");
        }
        if (!original.getInstructorId().equals(currentUserId)) {
            throw new SecurityException("본인이 작성한 커리큘럼만 수정할 수 있습니다.");
        }
        curriculumDto.setLastMdfrId(currentUserId);
        curriculumMapper.update(curriculumDto);
    }

    /** 커리큘럼 논리 삭제 (USE_YN = 'N'). 작성자 일치 여부를 먼저 검증한다. */
    @Override
    @Transactional
    public void removeCurriculumLogically(Long curriculumId, String currentUserId) {
        CurriculumDto original = curriculumMapper.selectById(curriculumId);
        if (original == null) {
            throw new IllegalArgumentException("삭제하려는 커리큘럼이 존재하지 않습니다.");
        }
        if (!original.getInstructorId().equals(currentUserId)) {
            throw new SecurityException("본인이 작성한 커리큘럼만 삭제할 수 있습니다.");
        }
        curriculumMapper.deleteLogically(curriculumId, currentUserId);
    }

    // ── 커리큘럼-강좌 매핑 ──────────────────────────────────────────

    /** 커리큘럼에 속한 강좌 목록을 SORT_ORD 오름차순으로 반환 */
    @Override
    public List<CourseDto> retrieveMappedCourses(Long curriculumId) {
        return curriculumMapper.selectMappedCourses(curriculumId);
    }

    /** 특정 강사의 강좌 중 아직 커리큘럼에 배정되지 않은 강좌 목록 반환 */
    @Override
    public List<CourseDto> retrieveAvailableCourses(String instrUserId) {
        return curriculumMapper.selectAvailableCourses(instrUserId);
    }

    /**
     * 강좌를 커리큘럼 맨 끝에 추가한다.
     * 커리큘럼 소유자 확인 후 강좌가 본인 소유인지도 검증한다.
     * 현재 최대 SORT_ORD + 1을 새 순서로 부여한다.
     */
    @Override
    @Transactional
    public void addCourseMapping(Long curriculumId, Long courseSn, String currentUserId) {
        verifyCurriculumOwner(curriculumId, currentUserId);
        if (curriculumMapper.countCourseBySnAndInstr(courseSn, currentUserId) == 0) {
            throw new SecurityException("본인의 강좌만 커리큘럼에 추가할 수 있습니다.");
        }
        int nextSortOrd = curriculumMapper.selectMaxCourseSortOrd(curriculumId) + 1;
        int affected = curriculumMapper.mapCourseToCurriculum(courseSn, curriculumId, nextSortOrd);
        if (affected == 0) {
            throw new IllegalArgumentException("이미 다른 커리큘럼에 배정된 강좌입니다.");
        }
    }

    /**
     * 강좌의 커리큘럼 매핑을 해제한다.
     * 해제된 강좌의 SORT_ORD보다 큰 강좌들을 1씩 당겨 순서 공백을 메운다.
     */
    @Override
    @Transactional
    public void removeCourseMapping(Long curriculumId, Long courseSn, String currentUserId) {
        verifyCurriculumOwner(curriculumId, currentUserId);
        Integer sortOrd = curriculumMapper.selectCourseSortOrd(courseSn, curriculumId);
        if (sortOrd == null) {
            throw new IllegalArgumentException("해당 커리큘럼에 속하지 않는 강좌입니다.");
        }
        curriculumMapper.unmapCourseFromCurriculum(courseSn);
        if (sortOrd > 0) {
            curriculumMapper.resequenceCoursesSortOrd(curriculumId, sortOrd);
        }
    }

    /**
     * 커리큘럼 내 강좌 순서를 재배치한다.
     * courseSnList 인덱스 순서대로 SORT_ORD를 1-based로 재부여한다.
     * UPDATE 전에 courseSnList가 현재 매핑된 강좌 집합과 일치하는지 미리 검증한다.
     */
    @Override
    @Transactional
    public void reorderCourses(Long curriculumId, List<Long> courseSnList, String currentUserId) {
        verifyCurriculumOwner(curriculumId, currentUserId);

        Set<Long> mapped = curriculumMapper.selectMappedCourses(curriculumId).stream()
                .map(CourseDto::getCourseSn)
                .collect(Collectors.toSet());
        Set<Long> requested = new HashSet<>(courseSnList);
        if (!mapped.equals(requested)) {
            throw new IllegalArgumentException("전달된 강좌 목록이 커리큘럼의 현재 강좌 목록과 일치하지 않습니다.");
        }

        /* 유니크 제약(UQ_COURSE_CURRICULUM_SORT) 충돌 방지: +10000 오프셋으로 이동 후 1-based 재부여 */
        curriculumMapper.clearCourseSortOrds(curriculumId);
        for (int i = 0; i < courseSnList.size(); i++) {
            curriculumMapper.updateCourseSortOrd(courseSnList.get(i), curriculumId, i + 1);
        }
    }

    /** curriculumId의 소유자가 currentUserId인지 검증한다. 불일치 시 예외를 던진다. */
    private void verifyCurriculumOwner(Long curriculumId, String currentUserId) {
        CurriculumDto curriculum = curriculumMapper.selectById(curriculumId);
        if (curriculum == null) {
            throw new IllegalArgumentException("존재하지 않는 커리큘럼입니다.");
        }
        if (!curriculum.getInstructorId().equals(currentUserId)) {
            throw new SecurityException("본인이 작성한 커리큘럼만 변경할 수 있습니다.");
        }
    }
}
