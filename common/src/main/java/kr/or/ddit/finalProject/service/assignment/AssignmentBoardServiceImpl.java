package kr.or.ddit.finalProject.service.assignment;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.ddit.finalProject.dto.assignment.AssignmentBoardDto;
import kr.or.ddit.finalProject.dto.assignment.AssignmentSubmitDto;
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
    public List<AssignmentBoardDto> getAssignmentList(Long classSn) {
        return assignmentBoardMapper.selectAssignmentList(classSn);
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
    public List<AssignmentSubmitDto> getSubmitList(Long asgmtSn, Long classSn) {
        return assignmentSubmitMapper.selectSubmitList(asgmtSn, classSn);
    }

    @Override
    @Transactional
    public int gradeSubmit(Long sbmtSn, BigDecimal score, String grddUserId) {
        return assignmentSubmitMapper.updateGrade(sbmtSn, score, grddUserId);
    }

    @Override
    public int getPendingGradeCount(Long classSn) {
        return assignmentSubmitMapper.selectPendingGradeCount(classSn);
    }

    @Override
    public List<AssignmentSubmitDto> getRecentSubmits(Long classSn, int limit) {
        return assignmentSubmitMapper.selectRecentSubmitsByClass(classSn, limit);
    }
}
