package kr.or.ddit.finalProject.service.curriculum;

import java.util.List;

import kr.or.ddit.finalProject.dto.curriculum.CurriculumDetailDto;
import kr.or.ddit.finalProject.dto.curriculum.CurriculumMasterDto;

public interface CurriculumService {

    List<CurriculumMasterDto> retrieveMasterList(String instructorId);

    List<CurriculumDetailDto> retrieveDetailList(Long curriculumId, String instructorId);

    boolean createCurriculum(CurriculumMasterDto masterDto, List<CurriculumDetailDto> detailList);

    void modifyCurriculum(CurriculumMasterDto masterDto, List<CurriculumDetailDto> detailList, String currentUserId);

    void removeCurriculumLogically(Long curriculumId, String currentUserId);
}
