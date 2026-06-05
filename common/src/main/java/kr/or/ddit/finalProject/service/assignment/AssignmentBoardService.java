package kr.or.ddit.finalProject.service.assignment;

import java.math.BigDecimal;
import java.util.List;

import kr.or.ddit.finalProject.dto.assignment.AssignmentBoardDto;
import kr.or.ddit.finalProject.dto.assignment.AssignmentSubmitDto;

public interface AssignmentBoardService {

    List<AssignmentBoardDto> getAssignmentList(Long classSn);

    AssignmentBoardDto getAssignmentDetail(Long asgmtSn);

    int insertAssignment(AssignmentBoardDto dto);

    List<AssignmentSubmitDto> getSubmitList(Long asgmtSn, Long classSn);

    int gradeSubmit(Long sbmtSn, BigDecimal score, String grddUserId);
}
