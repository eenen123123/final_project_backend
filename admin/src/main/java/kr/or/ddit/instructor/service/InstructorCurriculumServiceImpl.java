package kr.or.ddit.instructor.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.ddit.dto.instructor.CurriculumDetailDto;
import kr.or.ddit.dto.instructor.CurriculumMasterDto;
import kr.or.ddit.instructor.mapper.InstructorCurriculumMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InstructorCurriculumServiceImpl implements InstructorCurriculumService {

    private final InstructorCurriculumMapper curriculumMapper;

    @Override
    public List<CurriculumMasterDto> retrieveMasterList(String instructorId) {
        return curriculumMapper.selectMasterList(instructorId);
    }

    @Override
    public List<CurriculumDetailDto> retrieveDetailList(Long curriculumId, String instructorId) {
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
    @Transactional
    public boolean createCurriculum(CurriculumMasterDto masterDto, List<CurriculumDetailDto> detailList) {
        int masterResult = curriculumMapper.insertMaster(masterDto);

        if (masterResult > 0 && detailList != null && !detailList.isEmpty()) {
            Long generatedId = masterDto.getCurriculumId();
            String rgtrId = masterDto.getRgtrId();

            for (int i = 0; i < detailList.size(); i++) {
                CurriculumDetailDto d = detailList.get(i);
                curriculumMapper.insertDetail(generatedId, i, d.getWeekInfo(), d.getTopic(), d.getContent(), rgtrId);
            }
            return true;
        }

        return false;
    }

    @Override
    @Transactional
    public void modifyCurriculum(CurriculumMasterDto masterDto, List<CurriculumDetailDto> detailList, String currentUserId) {
        CurriculumMasterDto originalMaster = curriculumMapper.selectMasterById(masterDto.getCurriculumId());

        if (originalMaster == null || !"Y".equals(originalMaster.getUseYn())) {
            throw new IllegalArgumentException("수정하려는 커리큘럼이 존재하지 않습니다.");
        }

        if (!originalMaster.getInstructorId().equals(currentUserId)) {
            throw new SecurityException("본인이 작성한 커리큘럼만 수정할 수 있습니다.");
        }

        masterDto.setLastMdfrId(currentUserId);
        curriculumMapper.updateMaster(masterDto);
        curriculumMapper.deleteDetailsByMasterId(masterDto.getCurriculumId());

        if (detailList != null && !detailList.isEmpty()) {
            for (int i = 0; i < detailList.size(); i++) {
                CurriculumDetailDto d = detailList.get(i);
                curriculumMapper.insertDetail(masterDto.getCurriculumId(), i, d.getWeekInfo(), d.getTopic(), d.getContent(), currentUserId);
            }
        }
    }

    @Override
    @Transactional
    public void removeCurriculumLogically(Long curriculumId, String currentUserId) {
        CurriculumMasterDto originalMaster = curriculumMapper.selectMasterById(curriculumId);

        if (originalMaster == null) {
            throw new IllegalArgumentException("삭제하려는 커리큘럼이 존재하지 않습니다.");
        }

        if (!originalMaster.getInstructorId().equals(currentUserId)) {
            throw new SecurityException("본인이 작성한 커리큘럼만 삭제할 수 있습니다.");
        }

        curriculumMapper.deleteMasterLogically(curriculumId, currentUserId);
    }
}
