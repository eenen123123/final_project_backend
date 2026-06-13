package kr.or.ddit.finalProject.mapper.curriculum;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.course.CourseDto;
import kr.or.ddit.finalProject.dto.curriculum.CurriculumDto;

@Mapper
public interface CurriculumMapper {

    /** USE_YN = 'Y'인 전체 커리큘럼 목록을 등록일 내림차순으로 반환한다. */
    List<CurriculumDto> selectAllList();

    /** 특정 강사의 커리큘럼 목록을 등록일 내림차순으로 반환한다. */
    List<CurriculumDto> selectList(@Param("instructorId") String instructorId);

    /** curriculumId로 커리큘럼 단건을 조회한다. USE_YN = 'Y'인 경우에만 반환한다. */
    CurriculumDto selectById(@Param("curriculumId") Long curriculumId);

    /** 커리큘럼을 INSERT한다. SEQ_CURRICULUM.NEXTVAL로 PK를 채운다. */
    int insert(CurriculumDto curriculumDto);

    /** 커리큘럼 제목·기간·설명·최종수정자를 UPDATE한다. */
    int update(CurriculumDto curriculumDto);

    /** 커리큘럼을 논리 삭제한다 (USE_YN = 'N', 최종수정자 갱신). */
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

    /** 커리큘럼에 속한 강좌의 SORT_ORD를 반환한다. 해당 커리큘럼 소속이 아니면 null. */
    Integer selectCourseSortOrd(@Param("courseSn") Long courseSn,
            @Param("curriculumId") Long curriculumId);

    /**
     * 강좌의 SORT_ORD를 갱신한다. CURRICULUM_ID 조건을 함께 검사하므로,
     * 해당 커리큘럼 소속이 아니면 0을 반환한다.
     */
    int updateCourseSortOrd(@Param("courseSn") Long courseSn,
            @Param("curriculumId") Long curriculumId,
            @Param("sortOrd") int sortOrd);

    /** 커리큘럼에 속한 강좌 목록을 SORT_ORD 오름차순으로 반환한다. */
    List<CourseDto> selectMappedCourses(@Param("curriculumId") Long curriculumId);

    /** 특정 강사의 강좌 중 아직 커리큘럼에 배정되지 않은 강좌 목록을 반환한다. */
    List<CourseDto> selectAvailableCourses(@Param("instrUserId") String instrUserId);

    /** 해당 강좌가 특정 강사 소유인지 확인한다. 소유이면 1, 아니면 0을 반환한다. */
    int countCourseBySnAndInstr(@Param("courseSn") Long courseSn,
            @Param("instrUserId") String instrUserId);

    /** 커리큘럼 내 모든 강좌의 SORT_ORD에 +10000 오프셋을 적용한다. 재정렬 전 유니크 제약 충돌 방지용. */
    int clearCourseSortOrds(@Param("curriculumId") Long curriculumId);
}
