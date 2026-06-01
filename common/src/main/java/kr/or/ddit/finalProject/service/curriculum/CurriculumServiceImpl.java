package kr.or.ddit.finalProject.service.curriculum;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.ddit.finalProject.dto.curriculum.CurriculumMasterDto;
import kr.or.ddit.finalProject.mapper.curriculum.CurriculumMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CurriculumServiceImpl implements CurriculumService {

    private final CurriculumMapper curriculumMapper;

    @Override
    public List<CurriculumMasterDto> retrieveMasterList(String instructorId) {
        return curriculumMapper.selectMasterList(instructorId);
    }

    @Override
    @Transactional
    public boolean createCurriculum(CurriculumMasterDto masterDto) {
        return curriculumMapper.insertMaster(masterDto) > 0;
    }

    @Override
    @Transactional
    public void modifyCurriculum(CurriculumMasterDto masterDto, String currentUserId) {
        CurriculumMasterDto original = curriculumMapper.selectMasterById(masterDto.getCurriculumId());

        if (original == null || !"Y".equals(original.getUseYn())) {
            throw new IllegalArgumentException("수정하려는 커리큘럼이 존재하지 않습니다.");
        }

        if (!original.getInstructorId().equals(currentUserId)) {
            throw new SecurityException("본인이 작성한 커리큘럼만 수정할 수 있습니다.");
        }

        masterDto.setLastMdfrId(currentUserId);
        curriculumMapper.updateMaster(masterDto);
    }

    @Override
    @Transactional
    public void removeCurriculumLogically(Long curriculumId, String currentUserId) {
        CurriculumMasterDto original = curriculumMapper.selectMasterById(curriculumId);

        if (original == null) {
            throw new IllegalArgumentException("삭제하려는 커리큘럼이 존재하지 않습니다.");
        }

        if (!original.getInstructorId().equals(currentUserId)) {
            throw new SecurityException("본인이 작성한 커리큘럼만 삭제할 수 있습니다.");
        }

        curriculumMapper.deleteMasterLogically(curriculumId, currentUserId);
    }
}
