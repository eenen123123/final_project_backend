package kr.or.ddit.instructor;

import java.util.List;

import kr.or.ddit.dto.CurriculumDetailDto;
import kr.or.ddit.dto.CurriculumMasterDto;

public interface InstructorCurriculumService {

    /**
     * 로그인한 강사의 커리큘럼 마스터 목록 조회
     *
     * @param instructorId 로그인한 강사 ID
     * @return 커리큘럼 마스터 목록
     */
    List<CurriculumMasterDto> retrieveMasterList(String instructorId);

    /**
     * 특정 커리큘럼의 마스터 정보 및 AG Grid 상세 데이터 전체 조회
     *
     * @param curriculumId 커리큘럼 마스터 ID
     * @param instructorId 로그인한 강사 ID (권한 확인용)
     * @return 커리큘럼 상세 행 목록
     */
    List<CurriculumDetailDto> retrieveDetailList(Long curriculumId, String instructorId);

    /**
     * 신규 커리큘럼 마스터 및 상세(AG Grid) 데이터 일괄 등록
     *
     * @param masterDto 커리큘럼 마스터 데이터
     * @param detailList AG Grid에서 넘어온 상세 행 목록
     * @return 등록 성공 여부 (성공 시 true)
     */
    boolean createCurriculum(CurriculumMasterDto masterDto, List<CurriculumDetailDto> detailList);

    /**
     * 기존 커리큘럼 수정 (마스터 제목 갱신 및 상세 데이터 교체)
     *
     * @param masterDto 수정할 마스터 데이터
     * @param detailList 새로 편집된 AG Grid 상세 행 목록
     * @param currentUserId 현재 로그인한 강사 ID (권한 확인용)
     */
    void modifyCurriculum(CurriculumMasterDto masterDto, List<CurriculumDetailDto> detailList, String currentUserId);

    /**
     * 커리큘럼 삭제 (USE_YN = 'N' 상태 변경 처리)
     *
     * @param curriculumId 삭제할 커리큘럼 ID
     * @param currentUserId 현재 로그인한 강사 ID (권한 확인용)
     */
    void removeCurriculumLogically(Long curriculumId, String currentUserId);
}
