package kr.or.ddit.finalProject.mapper.curriculum;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.course.CourseDto;
import kr.or.ddit.finalProject.dto.curriculum.CurriculumDto;

@Mapper
public interface CurriculumMapper {

    List<CurriculumDto> selectAllList();

    List<CurriculumDto> selectList(@Param("instructorId") String instructorId);

    CurriculumDto selectById(@Param("curriculumId") Long curriculumId);

    int insert(CurriculumDto curriculumDto);

    int update(CurriculumDto curriculumDto);

    int deleteLogically(@Param("curriculumId") Long curriculumId,
            @Param("lastMdfrId") String lastMdfrId);

    // ── 커리큘럼-강좌 매핑 ──────────────────────────────────────────

    /** 커리큘럼에 등록된 강좌의 최대 SORT_ORD를 반환한다. */
    int selectMaxCourseSortOrd(@Param("curriculumId") Long curriculumId);

    /** 강좌를 커리큘럼에 매핑한다 (CURRICULUM_ID, SORT_ORD 설정). */
    int mapCourseToCurriculum(@Param("courseSn") Long courseSn,
            @Param("curriculumId") Long curriculumId,
            @Param("sortOrd") int sortOrd);

    /** 강좌의 커리큘럼 매핑을 해제한다 (CURRICULUM_ID, SORT_ORD = NULL). */
    int unmapCourseFromCurriculum(@Param("courseSn") Long courseSn);

    /** 커리큘럼 내 특정 순서 이후 강좌들의 SORT_ORD를 1씩 감소시킨다. */
    int resequenceCoursesSortOrd(@Param("curriculumId") Long curriculumId,
            @Param("sortOrd") int sortOrd);

    /** 강좌의 SORT_ORD를 갱신한다 (순서 변경 시 사용). */
    int updateCourseSortOrd(@Param("courseSn") Long courseSn,
            @Param("sortOrd") int sortOrd);

    /** 아직 커리큘럼에 등록되지 않은 강좌 목록을 반환한다 (매핑 추가 시 선택 목록용). */
    List<CourseDto> selectAvailableCourses();
}
