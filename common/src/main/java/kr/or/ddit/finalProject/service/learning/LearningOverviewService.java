package kr.or.ddit.finalProject.service.learning;

import java.util.List;

import kr.or.ddit.finalProject.dto.learning.LearningOverviewDto;

public interface LearningOverviewService {

    List<LearningOverviewDto> retrieveOverviewByInstructor(String instrUserId);
}
