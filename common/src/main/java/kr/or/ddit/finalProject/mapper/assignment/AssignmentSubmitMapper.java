package kr.or.ddit.finalProject.mapper.assignment;

import java.math.BigDecimal;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.assignment.AssignmentSubmitDto;

@Mapper
public interface AssignmentSubmitMapper {

    /** 클래스룸 전체 수강생 기준 제출 현황 (미제출자 포함) */
    List<AssignmentSubmitDto> selectSubmitList(@Param("asgmtSn") Long asgmtSn, @Param("classSn") Long classSn);

    int updateGrade(@Param("sbmtSn") Long sbmtSn, @Param("asgmtSn") Long asgmtSn, @Param("score") BigDecimal score, @Param("grddUserId") String grddUserId);

    int selectPendingGradeCount(@Param("classSn") Long classSn);

    List<AssignmentSubmitDto> selectRecentSubmitsByClass(@Param("classSn") Long classSn, @Param("limit") int limit);

    int deleteSubmitsByAsgmtSn(@Param("asgmtSn") Long asgmtSn);

    /** 특정 학생의 과제 제출 내용 단건 조회 */
    AssignmentSubmitDto selectMySubmit(@Param("asgmtSn") Long asgmtSn, @Param("userId") String userId);

    /** 과제 제출 등록 */
    int insertSubmit(AssignmentSubmitDto dto);

    /** 과제 재제출 (본문 내용 및 첨부파일 갱신) */
    int updateMySubmit(@Param("asgmtSn") Long asgmtSn, @Param("userId") String userId, @Param("sbmtCn") String sbmtCn, @Param("atchFileId") Long atchFileId);

    /** 제출 단건 조회 (채점 상세 페이지용) */
    AssignmentSubmitDto selectSubmitBySn(@Param("sbmtSn") Long sbmtSn);
}
