package kr.or.ddit.finalProject.service.learning;

import java.util.List;

import org.springframework.stereotype.Service;

import kr.or.ddit.finalProject.dto.learning.LearningOverviewDto;
import kr.or.ddit.finalProject.mapper.learning.LearningOverviewMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LearningOverviewServiceImpl implements LearningOverviewService {

    private final LearningOverviewMapper learningOverviewMapper;

    @Override
    public List<LearningOverviewDto> retrieveOverviewByInstructor(String instrUserId) {
        return learningOverviewMapper.selectOverviewByInstructor(instrUserId);
    }
}
