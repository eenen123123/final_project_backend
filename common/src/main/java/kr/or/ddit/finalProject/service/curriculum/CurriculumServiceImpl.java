package kr.or.ddit.finalProject.service.curriculum;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.ddit.finalProject.dto.curriculum.CurriculumDto;
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
    public List<CurriculumDto> retrieveList(String instructorId) {
        return curriculumMapper.selectList(instructorId);
    }

    @Override
    @Transactional
    public boolean createCurriculum(CurriculumDto curriculumDto) {
        return curriculumMapper.insert(curriculumDto) > 0;
    }

    @Override
    @Transactional
    public void modifyCurriculum(CurriculumDto curriculumDto, String currentUserId) {
        CurriculumDto original = curriculumMapper.selectById(curriculumDto.getCurriculumId());

        if (original == null || !"Y".equals(original.getUseYn())) {
            throw new IllegalArgumentException("수정하려는 커리큘럼이 존재하지 않습니다.");
        }

        if (!original.getInstructorId().equals(currentUserId)) {
            throw new SecurityException("본인이 작성한 커리큘럼만 수정할 수 있습니다.");
        }

        curriculumDto.setLastMdfrId(currentUserId);
        curriculumMapper.update(curriculumDto);
    }

    @Override
    @Transactional
    public void removeCurriculumLogically(Long curriculumId, String currentUserId) {
        CurriculumDto original = curriculumMapper.selectById(curriculumId);

        if (original == null || !"Y".equals(original.getUseYn())) {
            throw new IllegalArgumentException("삭제하려는 커리큘럼이 존재하지 않습니다.");
        }

        if (!original.getInstructorId().equals(currentUserId)) {
            throw new SecurityException("본인이 작성한 커리큘럼만 삭제할 수 있습니다.");
        }

        curriculumMapper.deleteLogically(curriculumId, currentUserId);
    }
}
