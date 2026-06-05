package kr.or.ddit.finalProject.mapper.assignment;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.assignment.AssignmentBoardDto;

@Mapper
public interface AssignmentBoardMapper {

    List<AssignmentBoardDto> selectAssignmentList(@Param("classSn") Long classSn);

    AssignmentBoardDto selectAssignmentDetail(@Param("asgmtSn") Long asgmtSn);

    int insertAssignment(AssignmentBoardDto dto);
}
