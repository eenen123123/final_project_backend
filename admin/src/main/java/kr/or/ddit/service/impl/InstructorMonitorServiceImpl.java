package kr.or.ddit.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.ddit.finalProject.dto.monitoring.ClassroomOverviewDto;
import kr.or.ddit.finalProject.dto.monitoring.InstructorMonitorCardDto;
import kr.or.ddit.mapper.InstructorMonitorMapper;
import kr.or.ddit.service.InstructorMonitorService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InstructorMonitorServiceImpl implements InstructorMonitorService {

    private final InstructorMonitorMapper instructorMonitorMapper;

    @Override
    public List<InstructorMonitorCardDto> getInstructorCards() {
        return instructorMonitorMapper.selectInstructorCards();
    }

    @Override
    public int getActiveClassCount() {
        return instructorMonitorMapper.selectActiveClassCount();
    }

    @Override
    public int getTotalStudentCount() {
        return instructorMonitorMapper.selectTotalStudentCount();
    }

    @Override
    public int getThisMonthJournalCount() {
        return instructorMonitorMapper.selectThisMonthJournalCount();
    }

    @Override
    public List<ClassroomOverviewDto> getActiveClassrooms() {
        return instructorMonitorMapper.selectActiveClassrooms();
    }
}
