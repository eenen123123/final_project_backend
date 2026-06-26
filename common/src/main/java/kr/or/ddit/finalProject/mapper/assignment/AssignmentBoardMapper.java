package kr.or.ddit.finalProject.mapper.assignment;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.assignment.AssignmentBoardDto;
import kr.or.ddit.finalProject.dto.classroom.StudentAssignmentDto;

@Mapper
public interface AssignmentBoardMapper {

    List<AssignmentBoardDto> selectAssignmentList(
            @Param("classSn") Long classSn,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize);

    int countAssignmentList(@Param("classSn") Long classSn);

    AssignmentBoardDto selectAssignmentDetail(@Param("asgmtSn") Long asgmtSn);

    int insertAssignment(AssignmentBoardDto dto);

    int updateAssignment(AssignmentBoardDto dto);

    int deleteAssignment(@Param("asgmtSn") Long asgmtSn, @Param("classSn") Long classSn);

    int toggleResubmitAllow(@Param("asgmtSn") Long asgmtSn, @Param("classSn") Long classSn);

    List<StudentAssignmentDto> selectAssignmentsByStudent(@Param("classSn") Long classSn, @Param("userId") String userId);
}
