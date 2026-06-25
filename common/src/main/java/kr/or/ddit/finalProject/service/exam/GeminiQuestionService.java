package kr.or.ddit.finalProject.service.exam;

import kr.or.ddit.finalProject.dto.exam.DifficultyStatsDto;
import kr.or.ddit.finalProject.dto.exam.ExamTrendDto;
import kr.or.ddit.finalProject.dto.exam.GeminiQuestionRequest;
import kr.or.ddit.finalProject.dto.exam.QuestionDto;
import kr.or.ddit.finalProject.dto.exam.WeakPointDto;

import java.util.List;

public interface GeminiQuestionService {

    QuestionDto generateQuestion(GeminiQuestionRequest request);

    List<WeakPointDto> retrieveWeakPoints(Long classSn);

    List<DifficultyStatsDto> retrieveDifficultyStats(Long classSn);

    List<ExamTrendDto> retrieveExamTrend(Long classSn);
}
