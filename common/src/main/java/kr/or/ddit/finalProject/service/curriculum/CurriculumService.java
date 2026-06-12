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

    void moveCourseUp(Long curriculumId, Long courseSn, String currentUserId);

    void moveCourseDown(Long curriculumId, Long courseSn, String currentUserId);
}
