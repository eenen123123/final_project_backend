package kr.or.ddit.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.ddit.finalProject.dto.monitoring.ClassroomGradeStatsDto;
import kr.or.ddit.finalProject.dto.monitoring.ClassroomOverviewDto;
import kr.or.ddit.finalProject.dto.monitoring.ExamScheduleDto;
import kr.or.ddit.finalProject.dto.monitoring.ProgressTrendDto;
import kr.or.ddit.mapper.MonitoringMapper;
import kr.or.ddit.service.MonitoringService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MonitoringServiceImpl implements MonitoringService {

    private final MonitoringMapper monitoringMapper;

    @Override
    public List<ClassroomOverviewDto> getClassroomOverview() {
        List<ClassroomOverviewDto> list = monitoringMapper.selectClassroomOverview();
        for (ClassroomOverviewDto dto : list) {
            if (dto.getLectureCnt() > 0) {
                dto.setCompletedLectureAvg(
                    (int) Math.round(dto.getAvgProgressRate() * dto.getLectureCnt() / 100.0)
                );
            }
        }
        return list;
    }

    @Override
    public List<ProgressTrendDto> getProgressTrend() {
        return monitoringMapper.selectProgressTrend();
    }

    @Override
    public List<ExamScheduleDto> getUpcomingExams() {
        return monitoringMapper.selectUpcomingExams();
    }

    @Override
    public List<ExamScheduleDto> getRecentCompletedExams() {
        return monitoringMapper.selectRecentCompletedExams();
    }

    @Override
    public List<ClassroomGradeStatsDto> getClassroomGradeStats() {
        return monitoringMapper.selectClassroomGradeStats();
    }

    @Override
    public Map<String, Object> getGradeDistribution() {
        return monitoringMapper.selectGradeDistribution();
    }
}
