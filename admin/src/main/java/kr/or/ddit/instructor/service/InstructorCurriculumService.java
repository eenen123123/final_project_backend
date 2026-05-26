package kr.or.ddit.instructor.service;

import java.util.List;

import kr.or.ddit.dto.instructor.CurriculumDetailDto;
import kr.or.ddit.dto.instructor.CurriculumMasterDto;

public interface InstructorCurriculumService {

    List<CurriculumMasterDto> retrieveMasterList(String instructorId);

    List<CurriculumDetailDto> retrieveDetailList(Long curriculumId, String instructorId);

    boolean createCurriculum(CurriculumMasterDto masterDto, List<CurriculumDetailDto> detailList);

    void modifyCurriculum(CurriculumMasterDto masterDto, List<CurriculumDetailDto> detailList, String currentUserId);

    void removeCurriculumLogically(Long curriculumId, String currentUserId);
}
