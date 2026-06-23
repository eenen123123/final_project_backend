package kr.or.ddit.finalProject.service.exam;

import kr.or.ddit.finalProject.dto.exam.GeminiQuestionRequest;
import kr.or.ddit.finalProject.dto.exam.QuestionDto;
import kr.or.ddit.finalProject.dto.exam.WeakPointDto;

import java.util.List;

public interface GeminiQuestionService {

    /** Gemini API를 호출해 문항 1개를 생성합니다. */
    QuestionDto generateQuestion(GeminiQuestionRequest request);

    /** 클래스 약점 과목 목록 조회 (데이터 없으면 빈 리스트) */
    List<WeakPointDto> retrieveWeakPoints(Long classSn);
}
