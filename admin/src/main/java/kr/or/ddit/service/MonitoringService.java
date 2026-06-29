package kr.or.ddit.service;

import java.util.List;
import java.util.Map;

import kr.or.ddit.finalProject.dto.monitoring.ClassroomGradeStatsDto;
import kr.or.ddit.finalProject.dto.monitoring.ClassroomOverviewDto;
import kr.or.ddit.finalProject.dto.monitoring.ExamScheduleDto;
import kr.or.ddit.finalProject.dto.monitoring.ProgressTrendDto;

public interface MonitoringService {
    List<ClassroomOverviewDto> getClassroomOverview();
    List<ProgressTrendDto> getProgressTrend();
    List<ExamScheduleDto> getUpcomingExams();
    List<ExamScheduleDto> getRecentCompletedExams();
    List<ClassroomGradeStatsDto> getClassroomGradeStats();
    Map<String, Object> getGradeDistribution();
}
