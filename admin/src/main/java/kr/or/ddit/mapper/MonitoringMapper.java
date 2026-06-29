package kr.or.ddit.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import kr.or.ddit.finalProject.dto.monitoring.ClassroomGradeStatsDto;
import kr.or.ddit.finalProject.dto.monitoring.ClassroomOverviewDto;
import kr.or.ddit.finalProject.dto.monitoring.ExamScheduleDto;
import kr.or.ddit.finalProject.dto.monitoring.ProgressTrendDto;

@Mapper
public interface MonitoringMapper {
    List<ClassroomOverviewDto> selectClassroomOverview();
    List<ProgressTrendDto> selectProgressTrend();
    List<ExamScheduleDto> selectUpcomingExams();
    List<ExamScheduleDto> selectRecentCompletedExams();
    List<ClassroomGradeStatsDto> selectClassroomGradeStats();
    Map<String, Object> selectGradeDistribution();
}
