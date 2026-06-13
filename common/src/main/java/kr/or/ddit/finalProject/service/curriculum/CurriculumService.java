package kr.or.ddit.finalProject.service.curriculum;

import java.util.List;

import kr.or.ddit.finalProject.dto.course.CourseDto;
import kr.or.ddit.finalProject.dto.curriculum.CurriculumDto;

public interface CurriculumService {

    List<CurriculumDto> retrieveAllList();

    List<CurriculumDto> retrieveList(String instructorId);

    boolean createCurriculum(CurriculumDto curriculumDto);

    void modifyCurriculum(CurriculumDto curriculumDto, String currentUserId);

    void removeCurriculumLogically(Long curriculumId, String currentUserId);

    // ── 커리큘럼-강좌 매핑 ──────────────────────────────────────────

    List<CourseDto> retrieveAvailableCourses();

    void addCourseMapping(Long curriculumId, Long courseSn, String currentUserId);

    void removeCourseMapping(Long curriculumId, Long courseSn, String currentUserId);

    /**
     * 커리큘럼 내 강좌 순서를 courseSnList 순서대로 저장한다.
     * SORT_ORD를 1-based 인덱스로 재부여한다.
     */
    void reorderCourses(Long curriculumId, List<Long> courseSnList, String currentUserId);
}
