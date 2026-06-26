package kr.or.ddit.finalProject.service.exam;

import kr.or.ddit.finalProject.dto.exam.QuestionDto;
import kr.or.ddit.finalProject.dto.exam.QuestionSaveRequest;

import java.util.List;

/**
 * 문항 관리 서비스 인터페이스
 *
 * [접근 제어 정책]
 *   instrUserId는 컨트롤러에서 Authentication.getName()으로 추출하여 전달합니다.
 *   문항 소유권 확인(RGTR_ID 일치 여부)은 구현체에서 처리합니다.
 */
public interface QuestionService {

    /**
     * 내 문항 목록 전체 조회 (STAT_CD != '99')
     */
    List<QuestionDto> retrieveMyQuestions(String instrUserId);

    /**
     * 내 문항 목록 중 특정 과목만 조회
     * subjId가 null이면 전체 조회 위임.
     */
    List<QuestionDto> retrieveMyQuestionsBySubjId(String instrUserId, Long subjId);

    /**
     * 필터 + 페이징 적용 문항 목록 조회
     *
     * @param instrUserId 강사 ID
     * @param subjId      과목 ID (null = 전체)
     * @param diffCd      난이도 (null = 전체)
     * @param page        1-based 페이지 번호
     * @param pageSize    페이지 당 건수
     */
    List<QuestionDto> retrieveQuestionPage(String instrUserId, Long subjId, String diffCd,
                                            boolean showDeleted, int page, int pageSize);

    /**
     * 필터 조건 총 문항 건수 (페이징 계산용)
     */
    int countQuestions(String instrUserId, Long subjId, String diffCd, boolean showDeleted);

    /**
     * 문항 단건 조회 (소유권 확인 포함)
     * 없거나 삭제됐거나 타인 소유면 예외 발생.
     */
    QuestionDto retrieveQuestion(Long qstnSn, String instrUserId);

    /**
     * 문항 등록
     */
    void addQuestion(String instrUserId, QuestionSaveRequest request);

    /**
     * 문항 수정
     * 없거나 삭제됐거나 타인 소유면 예외 발생.
     */
    void modifyQuestion(Long qstnSn, String instrUserId, QuestionSaveRequest request);

    /**
     * 문항 논리 삭제 (STAT_CD → '99')
     * 없거나 삭제됐거나 타인 소유면 예외 발생.
     */
    void removeQuestion(Long qstnSn, String instrUserId);
}
