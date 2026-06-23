package kr.or.ddit.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import kr.or.ddit.finalProject.dto.monitoring.ClassroomOverviewDto;

@Mapper
public interface MonitoringMapper {
    List<ClassroomOverviewDto> selectClassroomOverview();
}
