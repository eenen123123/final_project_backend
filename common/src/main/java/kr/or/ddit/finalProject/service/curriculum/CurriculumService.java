package kr.or.ddit.finalProject.service.curriculum;

import java.util.List;

import kr.or.ddit.finalProject.dto.curriculum.CurriculumDto;

public interface CurriculumService {

    List<CurriculumDto> retrieveList(String instructorId);

    boolean createCurriculum(CurriculumDto curriculumDto);

    void modifyCurriculum(CurriculumDto curriculumDto, String currentUserId);

    void removeCurriculumLogically(Long curriculumId, String currentUserId);
}
