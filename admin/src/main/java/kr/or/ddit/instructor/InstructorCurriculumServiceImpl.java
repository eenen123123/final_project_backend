package kr.or.ddit.instructor;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import kr.or.ddit.dto.CurriculumMasterDto;
import kr.or.ddit.dto.CurriculumDetailDto;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본적으로 읽기 전용으로 설정하여 성능 최적화
public class InstructorCurriculumServiceImpl implements InstructorCurriculumService {

    private final InstructorCurriculumMapper curriculumMapper;

    @Override
    public List<CurriculumMasterDto> retrieveMasterList(String instructorId) {
        return curriculumMapper.selectMasterList(instructorId);
    }

    @Override
    public List<CurriculumDetailDto> retrieveDetailList(Long curriculumId, String instructorId) {
        // [보안 검증] 상세 페이지 진입 시에도 해당 커리큘럼이 로그인한 강사의 것이 맞는지 확인
        CurriculumMasterDto master = curriculumMapper.selectMasterById(curriculumId);

        if (master == null || !"Y".equals(master.getUseYn())) {
            throw new IllegalArgumentException("존재하지 않거나 삭제된 커리큘럼입니다.");
        }

        if (!master.getInstructorId().equals(instructorId)) {
            throw new SecurityException("해당 커리큘럼을 조회할 권한이 없습니다.");
        }

        return curriculumMapper.selectDetailList(curriculumId);
    }

    @Override
    @Transactional // 등록/수정/삭제 작업이 일어나므로 트랜잭션 보장 필수
    public boolean createCurriculum(CurriculumMasterDto masterDto, List<CurriculumDetailDto> detailList) {

        // 1. 커리큘럼 마스터 등록 (XML의 selectKey 덕분에 실행 후 masterDto에 curriculumId가 채워집니다)
        int masterResult = curriculumMapper.insertMaster(masterDto);

        if (masterResult > 0 && detailList != null && !detailList.isEmpty()) {
            Long generatedId = masterDto.getCurriculumId();
            String rgtrId = masterDto.getRgtrId();

            // 2. AG Grid 상세 데이터 리스트 일괄 등록
            curriculumMapper.insertDetailList(detailList, generatedId, rgtrId);
            return true;
        }

        return false;
    }

    @Override
    @Transactional
    public void modifyCurriculum(CurriculumMasterDto masterDto, List<CurriculumDetailDto> detailList, String currentUserId) {

        // 1. [보안 검증] DB에서 기존 원본 데이터를 조회해 소유권 확인
        CurriculumMasterDto originalMaster = curriculumMapper.selectMasterById(masterDto.getCurriculumId());

        if (originalMaster == null || !"Y".equals(originalMaster.getUseYn())) {
            throw new IllegalArgumentException("수정하려는 커리큘럼이 존재하지 않습니다.");
        }

        // 원본 작성 강사 ID와 현재 로그인한 사용자의 ID 비교
        if (!originalMaster.getInstructorId().equals(currentUserId)) {
            throw new SecurityException("본인이 작성한 커리큘럼만 수정할 수 있습니다.");
        }

        // 2. 마스터 테이블 수정 (제목, 최종수정자, 수정일시 업데이트)
        masterDto.setLastMdfrId(currentUserId);
        curriculumMapper.updateMaster(masterDto);

        // 3. [동적 표 수정 전략] 기존에 존재하던 상세(Detail) 데이터 싹 밀어버리기
        curriculumMapper.deleteDetailsByMasterId(masterDto.getCurriculumId());

        // 4. AG Grid 화면에서 새로 넘어온 편집본으로 다시 싹 밀어넣기
        if (detailList != null && !detailList.isEmpty()) {
            curriculumMapper.insertDetailList(detailList, masterDto.getCurriculumId(), currentUserId);
        }
    }

    @Override
    @Transactional
    public void removeCurriculumLogically(Long curriculumId, String currentUserId) {

        // 1. [보안 검증] DB에서 원본을 조회해 소유권 확인
        CurriculumMasterDto originalMaster = curriculumMapper.selectMasterById(curriculumId);

        if (originalMaster == null) {
            throw new IllegalArgumentException("삭제하려는 커리큘럼이 존재하지 않습니다.");
        }

        if (!originalMaster.getInstructorId().equals(currentUserId)) {
            throw new SecurityException("본인이 작성한 커리큘럼만 삭제할 수 있습니다.");
        }

        // 2. 소프트 딜리트 수행 (USE_YN = 'N', 최종수정자 정보 주입)
        curriculumMapper.deleteMasterLogically(curriculumId, currentUserId);
    }
}
