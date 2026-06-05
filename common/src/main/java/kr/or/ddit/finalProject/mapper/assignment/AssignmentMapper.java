package kr.or.ddit.finalProject.mapper.assignment;

import java.math.BigDecimal;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.assignment.AssignmentBoardDto;
import kr.or.ddit.finalProject.dto.assignment.AssignmentSubmitDto;

@Mapper
public interface AssignmentMapper {

    List<AssignmentBoardDto> selectAssignmentList(@Param("classSn") Long classSn);

    AssignmentBoardDto selectAssignmentDetail(@Param("asgmtSn") Long asgmtSn
    );

    int insertAssignment(AssignmentBoardDto dto);

    /**
     * 클래스룸 전체 수강생 기준 제출 현황 (미제출자 포함)
     */
    List<AssignmentSubmitDto> selectSubmitList(@Param("asgmtSn") Long asgmtSn, @Param("classSn") Long classSn);

    int updateGrade(@Param("sbmtSn") Long sbmtSn, @Param("score") BigDecimal score, @Param("grddUserId") String grddUserId);
}
