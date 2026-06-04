package kr.or.ddit.finalProject.service.exam;

import kr.or.ddit.finalProject.dto.exam.ExamDto;
import kr.or.ddit.finalProject.dto.exam.ExamSaveRequest;
import kr.or.ddit.finalProject.dto.exam.QuestionDto;
import kr.or.ddit.finalProject.dto.exam.QuestionSaveRequest;

import java.util.List;

/**
 * 문항 관리 및 시험 관리 서비스 인터페이스
 *
 * [접근 제어 정책]
 *   instrUserId는 컨트롤러에서 Authentication.getName()으로 추출하여 전달합니다.
 *   요청 파라미터에서 수신하지 않습니다.
 *   문항/시험의 소유권 확인(RGTR_ID / EXAM_CHRG_USER_ID 일치 여부)은
 *   이 서비스의 구현체에서 처리합니다.
 */
public interface ExamService {

    // ──────────────────────────────────────────────────────────────────
    // 문항 관리
    // ──────────────────────────────────────────────────────────────────

    /**
     * 내 문항 목록 조회 (STAT_CD != '99')
     *
     * @param instrUserId 조회할 강사 ID (QUESTION_LIST.RGTR_ID)
     * @return QSTN_CN이 stem/choices로 파싱된 문항 목록
     */
    List<QuestionDto> retrieveMyQuestions(String instrUserId);

    /**
     * 문항 등록
     *
     * @param instrUserId 등록할 강사 ID (RGTR_ID에 저장됨)
     * @param request     폼에서 전달된 문항 저장 요청
     */
    void addQuestion(String instrUserId, QuestionSaveRequest request);

    /**
     * 문항 수정
     * 존재하지 않거나 삭제된 문항, 또는 본인 소유가 아닌 문항은 예외 발생.
     *
     * @param qstnSn      수정할 문항 일련번호
     * @param instrUserId 로그인 강사 ID (소유권 확인)
     * @param request     폼에서 전달된 수정 요청
     */
    void modifyQuestion(Long qstnSn, String instrUserId, QuestionSaveRequest request);

    /**
     * 문항 논리 삭제 (STAT_CD → '99')
     * 존재하지 않거나 삭제된 문항, 또는 본인 소유가 아닌 문항은 예외 발생.
     *
     * @param qstnSn      삭제할 문항 일련번호
     * @param instrUserId 로그인 강사 ID (소유권 확인)
     */
    void removeQuestion(Long qstnSn, String instrUserId);

    // ──────────────────────────────────────────────────────────────────
    // 시험 관리
    // ──────────────────────────────────────────────────────────────────

    /**
     * 내 시험 목록 조회 (EXAM_STAT_CD != '99')
     *
     * @param instrUserId 조회할 강사 ID (EXAM.EXAM_CHRG_USER_ID)
     * @return 시험 목록 (questions 필드 미포함)
     */
    List<ExamDto> retrieveMyExams(String instrUserId);

    /**
     * 시험 상세 조회 (포함 문항 목록 포함)
     * 존재하지 않거나 삭제된 시험, 또는 본인 소유가 아닌 시험은 예외 발생.
     *
     * @param examSn      조회할 시험 일련번호
     * @param instrUserId 로그인 강사 ID (소유권 확인)
     * @return 시험 정보 + questions 필드 포함
     */
    ExamDto retrieveExamDetail(Long examSn, String instrUserId);

    /**
     * 시험 등록 + 문항 배정 (EXAM + EXAM_QUESTION 동시 처리)
     *
     * @param instrUserId 등록할 강사 ID
     * @param request     폼에서 전달된 시험 저장 요청 (qstnSnList 포함)
     */
    void addExam(String instrUserId, ExamSaveRequest request);

    /**
     * 시험 수정 + 문항 재배정 (기존 EXAM_QUESTION 전체 삭제 후 재삽입)
     * 존재하지 않거나 삭제된 시험, 또는 본인 소유가 아닌 시험은 예외 발생.
     *
     * @param examSn      수정할 시험 일련번호
     * @param instrUserId 로그인 강사 ID (소유권 확인)
     * @param request     폼에서 전달된 수정 요청
     */
    void modifyExam(Long examSn, String instrUserId, ExamSaveRequest request);

    /**
     * 시험 논리 삭제 (EXAM_STAT_CD → '99')
     * 존재하지 않거나 삭제된 시험, 또는 본인 소유가 아닌 시험은 예외 발생.
     *
     * @param examSn      삭제할 시험 일련번호
     * @param instrUserId 로그인 강사 ID (소유권 확인)
     */
    void removeExam(Long examSn, String instrUserId);
}
