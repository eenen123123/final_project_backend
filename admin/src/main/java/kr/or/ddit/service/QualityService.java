package kr.or.ddit.service;

import java.util.List;
import java.util.Map;

import kr.or.ddit.finalProject.dto.instructor.InstructorQnaStatsDto;
import kr.or.ddit.finalProject.dto.instructor.OverdueQnaDto;

public interface QualityService {

    List<InstructorQnaStatsDto> getInstructorQnaStats(String period);

    Map<String, Object> getQnaSummary(String period);

    List<OverdueQnaDto> getOverdueQnaList();
}
