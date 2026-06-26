package kr.or.ddit.finalProject.mapper.exam;

import kr.or.ddit.finalProject.dto.exam.ExamDto;
import kr.or.ddit.finalProject.dto.exam.ExamQuestionDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * EXAM, EXAM_QUESTION 테이블 Mapper
 *
 * 시험 소유권 확인(examChrgUserId 일치 여부)은 서비스 레이어에서 처리합니다.
 *
 * [EXAM_STAT_CD 논리 삭제 정책]
 *   EXAM 테이블에 DEL_YN 컬럼이 없습니다.
 *   deleteExam은 EXAM_STAT_CD를 '99'로 변경하여 논리 삭제합니다.
 *
 * [EXAM_QUESTION M:N 관계]
 *   시험 수정 시 기존 EXAM_QUESTION 행을 전부 삭제하고 새로 삽입합니다.
 *   (단순하고 버그 없는 방식)
 */
@Mapper
public interface ExamMapper {

    /**
     * 특정 강사의 시험 전체 조회
     * EXAM_STAT_CD = '99'(삭제)인 항목은 제외합니다.
     * 등록일시(examRegDt) 내림차순 정렬.
     *
     * @param examChrgUserId 조회할 강사 ID
     */
    List<ExamDto> selectMyExams(@Param("examChrgUserId") String examChrgUserId);

    /**
     * 시험 단건 조회 (questions 필드 미포함)
     * 소유권 확인 및 수정/삭제 전 존재 여부 체크에 사용합니다.
     * 논리 삭제된 시험(STAT_CD='99')도 조회됩니다 (서비스에서 판단).
     *
     * @param examSn 조회할 시험 일련번호
     */
    ExamDto selectExamBySn(@Param("examSn") Long examSn);

    /**
     * 특정 시험에 배정된 문항 목록 조회
     * EXAM_QUESTION과 QUESTION_LIST를 조인하여 문항 상세 정보를 반환합니다.
     * QSTN_ORDR 오름차순 정렬.
     *
     * @param examSn 조회할 시험 일련번호
     */
    List<ExamQuestionDto> selectExamQuestions(@Param("examSn") Long examSn);

    /**
     * 시험 등록
     * INSERT 후 생성된 EXAM_SN이 dto.examSn에 자동으로 채워집니다 (useGeneratedKeys).
     * EXAM_REG_DT는 CURRENT_TIMESTAMP로 자동 설정합니다.
     *
     * @param dto 저장할 시험 정보 (examChrgUserId, examRegNm 포함 필수)
     */
    void insertExam(ExamDto dto);

    /**
     * 시험 수정 (시험명·유형·시작/종료일시 변경 가능)
     * 소유권 확인은 서비스에서 합니다.
     *
     * @param dto 수정할 내용 (examSn 포함 필수)
     */
    void updateExam(ExamDto dto);

    /**
     * 시험 논리 삭제 (EXAM_STAT_CD → '99')
     * 소유권 확인은 서비스에서 합니다.
     *
     * @param examSn 삭제할 시험 일련번호
     */
    void deleteExam(@Param("examSn") Long examSn);

    /**
     * 시험-문항 연결 행 단건 삽입 (EXAM_QUESTION)
     * 시험 등록/수정 시 선택된 문항마다 호출합니다.
     *
     * @param dto examSn, qstnSn, qstnOrdr 포함 필수
     */
    void insertExamQuestion(ExamQuestionDto dto);

    /**
     * 특정 시험의 EXAM_QUESTION 전체 삭제
     * 시험 수정 시 기존 문항 배정을 초기화하고 재삽입하기 위해 사용합니다.
     *
     * @param examSn 초기화할 시험 일련번호
     */
    void deleteExamQuestions(@Param("examSn") Long examSn);

    /**
     * 특정 클래스룸의 시험 목록 조회 (EXAM_STAT_CD != '99')
     * 등록일시 내림차순 정렬.
     *
     * @param classSn 클래스룸 일련번호
     */
    List<ExamDto> selectExamsByClassSn(@Param("classSn") Long classSn);

    /**
     * 특정 시험의 응시자 목록 조회 (EXAM_TAKER JOIN MEMBER)
     *
     * @param examSn 시험 일련번호
     */
    List<kr.or.ddit.finalProject.dto.exam.ExamTakerDto> selectTakersByExamSn(@Param("examSn") Long examSn);

    /** 응시자 목록 — 총점·제출일시·채점완료여부 포함 */
    List<kr.or.ddit.finalProject.dto.exam.ExamTakerDto> selectTakersWithScore(@Param("examSn") Long examSn);

    /** 특정 학생의 시험 문항별 답안 + 채점 현황 */
    List<kr.or.ddit.finalProject.dto.exam.StudentAnswerDto> selectStudentAnswers(
            @Param("examSn") Long examSn, @Param("userId") String userId);

    /** 단일 문항 채점 점수 저장 */
    int updateAnswerScore(@Param("sbmtAnswSn") Long sbmtAnswSn,
                          @Param("score") java.math.BigDecimal score,
                          @Param("grdgUserId") String grdgUserId);

    /** EXAM_TAKER 총점 갱신 (채점 완료 후 호출) */
    int updateExamTakerTotalScore(@Param("examSn") Long examSn, @Param("userId") String userId);

    void deleteExamTakers(@Param("examSn") Long examSn);

    List<kr.or.ddit.finalProject.dto.classroom.StudentExamDto> selectExamsByStudent(
            @Param("classSn") Long classSn, @Param("userId") String userId);

    /** 클래스룸 내 시험 채점 대기 건수 (TOT_SCORE IS NULL) */
    int countPendingGradesByClassSn(@Param("classSn") Long classSn);

    /** 특정 학생의 EXAM_TAKER 조회 (중복 제출 확인용) */
    kr.or.ddit.finalProject.dto.exam.ExamTakerDto selectExamTaker(
            @Param("examSn") Long examSn, @Param("userId") String userId);

    /** 학생 응시 등록 (EXAM_TAKER INSERT) */
    void insertExamTaker(@Param("examSn") Long examSn, @Param("userId") String userId);

    /** 답안 저장 (ANSWER_SUBMIT INSERT) */
    void insertAnswer(@Param("examSn") Long examSn, @Param("userId") String userId,
                      @Param("qstnSn") Long qstnSn, @Param("answCn") String answCn);
}
