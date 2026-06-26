package kr.or.ddit.finalProject.service.assignment;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.ddit.finalProject.dto.assignment.AssignmentBoardDto;
import kr.or.ddit.finalProject.dto.assignment.AssignmentSubmitDto;
import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.mapper.assignment.AssignmentBoardMapper;
import kr.or.ddit.finalProject.mapper.assignment.AssignmentSubmitMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssignmentBoardServiceImpl implements AssignmentBoardService {

    private final AssignmentBoardMapper assignmentBoardMapper;
    private final AssignmentSubmitMapper assignmentSubmitMapper;

    @Override
    public PageResponse<AssignmentBoardDto> getAssignmentList(Long classSn, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<AssignmentBoardDto> items = assignmentBoardMapper.selectAssignmentList(classSn, offset, pageSize);
        int totalCount = assignmentBoardMapper.countAssignmentList(classSn);
        return new PageResponse<>(items, totalCount);
    }

    @Override
    public AssignmentBoardDto getAssignmentDetail(Long asgmtSn) {
        return assignmentBoardMapper.selectAssignmentDetail(asgmtSn);
    }

    @Override
    @Transactional
    public int insertAssignment(AssignmentBoardDto dto) {
        return assignmentBoardMapper.insertAssignment(dto);
    }

    @Override
    @Transactional
    public int updateAssignment(AssignmentBoardDto dto) {
        return assignmentBoardMapper.updateAssignment(dto);
    }

    @Override
    @Transactional
    public int deleteAssignment(Long asgmtSn, Long classSn) {
        assignmentSubmitMapper.deleteSubmitsByAsgmtSn(asgmtSn);
        return assignmentBoardMapper.deleteAssignment(asgmtSn, classSn);
    }

    @Override
    public List<AssignmentSubmitDto> getSubmitList(Long asgmtSn, Long classSn) {
        return assignmentSubmitMapper.selectSubmitList(asgmtSn, classSn);
    }

    @Override
    public AssignmentSubmitDto getSubmitBySn(Long sbmtSn) {
        return assignmentSubmitMapper.selectSubmitBySn(sbmtSn);
    }

    @Override
    @Transactional
    public int gradeSubmit(Long sbmtSn, Long asgmtSn, BigDecimal score, String grddUserId) {
        return assignmentSubmitMapper.updateGrade(sbmtSn, asgmtSn, score, grddUserId);
    }

    @Override
    public int getPendingGradeCount(Long classSn) {
        return assignmentSubmitMapper.selectPendingGradeCount(classSn);
    }

    @Override
    public List<AssignmentSubmitDto> getRecentSubmits(Long classSn, int limit) {
        return assignmentSubmitMapper.selectRecentSubmitsByClass(classSn, limit);
    }

    @Override
    @Transactional
    public int toggleResubmitAllow(Long asgmtSn, Long classSn) {
        return assignmentBoardMapper.toggleResubmitAllow(asgmtSn, classSn);
    }

    @Override
    public List<kr.or.ddit.finalProject.dto.classroom.StudentAssignmentDto> getAssignmentsByStudent(Long classSn, String userId) {
        return assignmentBoardMapper.selectAssignmentsByStudent(classSn, userId);
    }
}
