package kr.or.ddit.service;

import java.util.List;

import kr.or.ddit.finalProject.dto.monitoring.ClassroomOverviewDto;

public interface MonitoringService {
    List<ClassroomOverviewDto> getClassroomOverview();
}
