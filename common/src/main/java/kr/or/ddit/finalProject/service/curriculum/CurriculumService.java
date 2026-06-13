package kr.or.ddit.finalProject.service.curriculum;

import java.util.List;

import kr.or.ddit.finalProject.dto.course.CourseDto;
import kr.or.ddit.finalProject.dto.curriculum.CurriculumDto;

public interface CurriculumService {

    /** USE_YN = 'Y'인 전체 커리큘럼 목록을 반환한다. */
    List<CurriculumDto> retrieveAllList();

    /** 특정 강사의 커리큘럼 목록을 반환한다. */
    List<CurriculumDto> retrieveList(String instructorId);

    /** curriculumId로 커리큘럼 단건을 조회한다. 존재하지 않으면 null을 반환한다. */
    CurriculumDto retrieveById(Long curriculumId);

    /** 커리큘럼을 생성한다. 성공 시 true를 반환한다. */
    boolean createCurriculum(CurriculumDto curriculumDto);

    /**
     * 커리큘럼을 수정한다.
     * currentUserId가 작성자가 아니면 SecurityException을 던진다.
     */
    void modifyCurriculum(CurriculumDto curriculumDto, String currentUserId);

    /**
     * 커리큘럼을 논리 삭제한다 (USE_YN = 'N').
     * currentUserId가 작성자가 아니면 SecurityException을 던진다.
     */
    void removeCurriculumLogically(Long curriculumId, String currentUserId);

    // ── 커리큘럼-강좌 매핑 ──────────────────────────────────────────

    /** 커리큘럼에 속한 강좌 목록을 SORT_ORD 오름차순으로 반환한다. */
    List<CourseDto> retrieveMappedCourses(Long curriculumId);

    /** 특정 강사의 강좌 중 아직 커리큘럼에 배정되지 않은 강좌 목록을 반환한다. */
    List<CourseDto> retrieveAvailableCourses(String instrUserId);

    /**
     * 강좌를 커리큘럼에 추가한다.
     * 현재 최대 SORT_ORD + 1 위치에 추가된다.
     */
    void addCourseMapping(Long curriculumId, Long courseSn, String currentUserId);

    /**
     * 강좌의 커리큘럼 매핑을 해제한다.
     * 해제 후 빈 순서를 메우기 위해 이후 강좌들의 SORT_ORD를 1씩 감소시킨다.
     */
    void removeCourseMapping(Long curriculumId, Long courseSn, String currentUserId);

    /**
     * 커리큘럼 내 강좌 순서를 courseSnList 순서대로 저장한다.
     * SORT_ORD를 1-based 인덱스로 재부여한다.
     */
    void reorderCourses(Long curriculumId, List<Long> courseSnList, String currentUserId);
}
