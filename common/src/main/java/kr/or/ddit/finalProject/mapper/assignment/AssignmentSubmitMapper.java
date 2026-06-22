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
}
