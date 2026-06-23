package kr.or.ddit.finalProject.service.exam;

import kr.or.ddit.finalProject.dto.exam.ExamDto;
import kr.or.ddit.finalProject.dto.exam.ExamSaveRequest;

import java.util.List;

/**
 * 시험 관리 서비스 인터페이스
 *
 * [접근 제어 정책]
 *   instrUserId는 컨트롤러에서 Authentication.getName()으로 추출하여 전달합니다.
 *   시험 소유권 확인(EXAM_CHRG_USER_ID 일치 여부)은 구현체에서 처리합니다.
 */
public interface ExamService {

    /**
     * 내 시험 목록 조회 (EXAM_STAT_CD != '99')
     */
    List<ExamDto> retrieveMyExams(String instrUserId);

    /**
     * 시험 상세 조회 (포함 문항 목록 포함)
     * 없거나 삭제됐거나 타인 소유면 예외 발생.
     */
    ExamDto retrieveExamDetail(Long examSn, String instrUserId);

    /**
     * 시험 등록 + 문항 배정 (EXAM + EXAM_QUESTION 동시 처리)
     */
    void addExam(String instrUserId, ExamSaveRequest request);

    /**
     * 시험 수정 + 문항 재배정 (기존 EXAM_QUESTION 전체 삭제 후 재삽입)
     * 없거나 삭제됐거나 타인 소유면 예외 발생.
     */
    void modifyExam(Long examSn, String instrUserId, ExamSaveRequest request);

    /**
     * 시험 논리 삭제 (EXAM_STAT_CD → '99')
     * 없거나 삭제됐거나 타인 소유면 예외 발생.
     */
    void removeExam(Long examSn, String instrUserId);
}
