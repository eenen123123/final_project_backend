package kr.or.ddit.finalProject.mapper.exam;

import kr.or.ddit.finalProject.dto.exam.QuestionDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * QUESTION_LIST 테이블 CRUD Mapper
 *
 * 문항 소유권 확인(rgtrId 일치 여부)은 서비스 레이어에서 처리하므로
 * 이 Mapper는 순수 DB 조작만 담당합니다.
 *
 * [STAT_CD 논리 삭제 정책]
 *   QUESTION_LIST에 DEL_YN 컬럼이 없습니다.
 *   deleteQuestion은 STAT_CD를 '99'로 변경하여 논리 삭제합니다.
 *   조회 메서드는 STAT_CD != '99' 조건으로 삭제된 문항을 제외합니다.
 */
@Mapper
public interface QuestionMapper {

    /**
     * 특정 강사의 문항 전체 조회
     * STAT_CD = '99'(삭제)인 항목은 제외합니다.
     * 유형(qstnTypeCd) → 등록일시(regDt) 내림차순 정렬.
     *
     * @param rgtrId 조회할 강사 ID (QUESTION_LIST.RGTR_ID)
     */
    List<QuestionDto> selectMyQuestions(@Param("rgtrId") String rgtrId);

    /**
     * 필터 조건 + 페이징 적용 문항 목록 조회
     *
     * @param rgtrId  강사 ID
     * @param subjId  과목 ID (null = 전체)
     * @param diffCd  난이도 코드 (null = 전체)
     * @param offset  건너뛸 행 수 (0-based)
     * @param limit   조회할 최대 행 수
     */
    List<QuestionDto> selectQuestionPage(@Param("rgtrId") String rgtrId,
                                         @Param("subjId") Long subjId,
                                         @Param("diffCd") String diffCd,
                                         @Param("offset") int offset,
                                         @Param("limit") int limit);

    /**
     * 필터 조건 적용 문항 총 건수 (페이징 계산용)
     *
     * @param rgtrId  강사 ID
     * @param subjId  과목 ID (null = 전체)
     * @param diffCd  난이도 코드 (null = 전체)
     */
    int countQuestions(@Param("rgtrId") String rgtrId,
                       @Param("subjId") Long subjId,
                       @Param("diffCd") String diffCd);

    /**
     * 문항 단건 조회
     * 소유권 확인 및 수정/삭제 전 존재 여부 체크에 사용합니다.
     * 논리 삭제된 항목(STAT_CD='99')도 조회됩니다 (서비스에서 판단).
     *
     * @param qstnSn 조회할 문항 일련번호
     */
    QuestionDto selectQuestionBySn(@Param("qstnSn") Long qstnSn);

    /**
     * 문항 등록
     * INSERT 후 생성된 QSTN_SN이 dto.qstnSn에 자동으로 채워집니다 (useGeneratedKeys).
     * RGTR_ID(등록자)와 REG_DT(등록일시)를 함께 저장합니다.
     *
     * @param dto 저장할 문항 (rgtrId, qstnTypeCd, qstnCn 포함 필수)
     */
    void insertQuestion(QuestionDto dto);

    /**
     * 문항 수정 (내용·유형·배점·정답·해설 변경 가능)
     * LAST_MDFR_ID(수정자)와 FIELD5(수정일시, MDFCN_DT 추정)를 함께 갱신합니다.
     * 소유권 확인은 서비스에서 합니다.
     *
     * @param dto 수정할 내용 (qstnSn, lastMdfrId 포함 필수)
     */
    void updateQuestion(QuestionDto dto);

    /**
     * 문항 논리 삭제 (STAT_CD → '99')
     * QUESTION_LIST에 DEL_YN 컬럼이 없으므로 STAT_CD로 삭제를 처리합니다.
     * EXAM_QUESTION에서 이 문항을 참조 중이더라도 DB 제약 없이 삭제됩니다.
     * 소유권 확인은 서비스에서 합니다.
     *
     * @param qstnSn 삭제할 문항 일련번호
     */
    void deleteQuestion(@Param("qstnSn") Long qstnSn);
}
