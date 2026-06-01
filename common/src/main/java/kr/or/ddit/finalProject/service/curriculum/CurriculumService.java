package kr.or.ddit.finalProject.service.curriculum;

import java.util.List;

import kr.or.ddit.finalProject.dto.curriculum.CurriculumMasterDto;

public interface CurriculumService {

    List<CurriculumMasterDto> retrieveMasterList(String instructorId);

    boolean createCurriculum(CurriculumMasterDto masterDto);

    void modifyCurriculum(CurriculumMasterDto masterDto, String currentUserId);

    void removeCurriculumLogically(Long curriculumId, String currentUserId);
}
