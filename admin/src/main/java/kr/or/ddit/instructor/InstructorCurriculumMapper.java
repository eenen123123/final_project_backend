package kr.or.ddit.instructor;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.dto.CurriculumDetailDto;
import kr.or.ddit.dto.CurriculumMasterDto;

@Mapper
public interface InstructorCurriculumMapper {
// ==========================================
    // 1. 조회 (Select)
    // ==========================================

    // 로그인한 강사의 본인 커리큘럼 마스터 목록 조회
    List<CurriculumMasterDto> selectMasterList(@Param("instructorId") String instructorId);

    // 권한 체크용: 특정 커리큘럼 마스터 단건 조회
    CurriculumMasterDto selectMasterById(@Param("curriculumId") Long curriculumId);

    // 특정 커리큘럼의 AG Grid 표 데이터(상세) 전체 조회
    List<CurriculumDetailDto> selectDetailList(@Param("curriculumId") Long curriculumId);

    // ==========================================
    // 2. 등록 (Insert)
    // ==========================================
    // 커리큘럼 마스터 등록 (등록 후 생성된 시퀀스 ID가 마스터 DTO에 담김)
    int insertMaster(CurriculumMasterDto masterDto);

    // 커리큘럼 상세(Grid 행) 단건 등록 - 서비스 루프에서 행마다 호출
    int insertDetail(@Param("curriculumId") Long curriculumId,
            @Param("rowOrder") int rowOrder,
            @Param("weekInfo") String weekInfo,
            @Param("topic") String topic,
            @Param("content") String content,
            @Param("rgtrId") String rgtrId);

    // ==========================================
    // 3. 수정 및 삭제 (Update / Delete)
    // ==========================================
    // 커리큘럼 마스터 정보 수정 (제목, 최종수정자 등)
    int updateMaster(CurriculumMasterDto masterDto);

    // 커리큘럼 마스터 삭제 (실제로는 USE_YN = 'N' 처리하는 Soft Delete)
    int deleteMasterLogically(@Param("curriculumId") Long curriculumId,
            @Param("lastMdfrId") String lastMdfrId);

    // 커리큘럼 수정 시 활용: 기존 상세 데이터 물리적 전체 삭제
    int deleteDetailsByMasterId(@Param("curriculumId") Long curriculumId);
}
