package kr.or.ddit.finalProject.service.assignment;

import java.math.BigDecimal;
import java.util.List;

import kr.or.ddit.finalProject.dto.assignment.AssignmentBoardDto;
import kr.or.ddit.finalProject.dto.assignment.AssignmentSubmitDto;
import kr.or.ddit.finalProject.dto.common.PageResponse;

public interface AssignmentBoardService {

    PageResponse<AssignmentBoardDto> getAssignmentList(Long classSn, int page, int pageSize);

    AssignmentBoardDto getAssignmentDetail(Long asgmtSn);

    int insertAssignment(AssignmentBoardDto dto);

    int updateAssignment(AssignmentBoardDto dto);

    int deleteAssignment(Long asgmtSn, Long classSn);

    List<AssignmentSubmitDto> getSubmitList(Long asgmtSn, Long classSn);

    int gradeSubmit(Long sbmtSn, Long asgmtSn, BigDecimal score, String grddUserId);

    int getPendingGradeCount(Long classSn);

    /** 클래스룸 전체 최근 제출 최대 N건 */
    List<AssignmentSubmitDto> getRecentSubmits(Long classSn, int limit);
}
